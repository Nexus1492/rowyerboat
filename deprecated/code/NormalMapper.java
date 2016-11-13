package deprecated;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.rendering.SimpleWaterShader;
import com.rowyerboat.rendering.WaterShader;

public class NormalMapper implements Screen {
	
	private SimpleWaterShader shader;
	private float time;
	private PerspectiveCamera camera;
	private CameraInputController camController;
	
	private SpriteBatch batch;
	private BitmapFont font;
	private ShapeRenderer shaper;
	
	private FrameBuffer fbo;
	
	private ModelInstance waterInst;
	private ModelBatch modelBatch;

	public NormalMapper() {
		shader = new SimpleWaterShader();
		shader.init();
		Texture tex1 = new Texture(Gdx.files.internal("noise0.png"), true);
		Gdx.app.log("min + mag", ""+tex1.getMinFilter() + ", " + tex1.getMagFilter());
		tex1.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		tex1.bind(2);
		tex1.bind(3);
		tex1.bind(4);
		tex1.bind(5);
		time = 0;
		
		camera = new PerspectiveCamera(67, Settings.width, Settings.height);
        camera.near = 1f;
        camera.far = 800f;
        //camera.position.set(0f, 2f, 2f);
        camera.position.set(0f, -512f, 0f);
        camera.lookAt(0, 0, 0);
        camera.up.set(0, 0, 1);
        camera.update();
		camController = new CameraInputController(camera);
		camController.scrollFactor *= 20;
		camController.autoUpdate = true;
		camController.pinchZoomFactor *= 20;
		Gdx.input.setInputProcessor(camController);
		
		batch = new SpriteBatch();
		modelBatch = new ModelBatch();
		shaper = new ShapeRenderer();
		
		batch.setShader(new ShaderProgram(Gdx.files.internal("Shaders/defaultVertex.glsl"),
				Gdx.files.internal("Shaders/grayscaleFragment.glsl")));
		System.out.println(batch.getShader().getLog());
		
		//font = new BitmapFont();
		
		waterInst = new ModelInstance(buildWater());
		
		fbo = new FrameBuffer(Format.RGBA8888, Settings.width, Settings.height, false);
		
	}

	@Override
	public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        
		camController.update();
		shader.update(delta);
		BoundingBox box = new BoundingBox();
		box = waterInst.calculateBoundingBox(box);
		
		boolean useFBO = true;
		//useFBO = false;
		if (useFBO) fbo.begin();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        OrthographicCamera cam = new OrthographicCamera(Settings.width, Settings.height);
        cam.setToOrtho(true, Settings.width, Settings.height);
        shaper.setProjectionMatrix(cam.combined);
        shaper.begin(ShapeType.Line);
        shaper.rect(0, 0, Settings.width, Settings.height);
        shaper.end();

        modelBatch.begin(camera);
        modelBatch.render(waterInst, shader);
        modelBatch.end();
		if (useFBO) fbo.end();
        
        batch.setProjectionMatrix(cam.combined);
		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 0, 0);
		batch.end();
	}
	
	private Model buildWater() {
		float dim = 512f;
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		builder.part("water", GL30.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates, new Material())
			.rect(new Vector3(-dim/2, -dim/2, 0),
					new Vector3(dim/2, -dim/2, 0),
					new Vector3(dim/2, dim/2, 0),
					new Vector3(-dim/2, dim/2, 0),
					new Vector3());;
		return builder.end();
	}
	
	@Override
	public void show() {
		
	}


	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
}
