package deprecated;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.UBJsonReader;
import com.rowyerboat.rendering.WaterShader;

public class WaterSurfaceTest implements Screen{

	private PerspectiveCamera camera;
	private WaterShader shader;
	private WaterShader wShader;
	private Mesh water;
	private Model cubeModel;
	private Model waterModel;
	private ModelInstance cubeInstance;
	private ModelInstance wInst;
	private ModelBatch modelBatch;
	private Environment environment;
	private Renderable renderable;
	private CameraInputController camController;
	private RenderContext renderContext;
	
	private float offset;
	
	public WaterSurfaceTest() {
		wShader = new WaterShader();
		//shader = wShader.shader;
		offset = 0;
		
		camera = new PerspectiveCamera();
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 800f;
        //camera.position.set(0f, 2f, 2f);
        camera.position.set(0f, -250f, 250f);
        camera.lookAt(0, 0, 0);
        camera.up.set(0, 0, 1);
        camera.update();
		camController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(camController);
        
        ModelBuilder modelBuilder = new ModelBuilder();
        cubeModel = modelBuilder.createSphere(200f, 200f, 2f, 20, 20,
        //cubeModel = modelBuilder.createBox(400f, 300f, 0f,
            new Material(),
            Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        cubeInstance = new ModelInstance(cubeModel);
        
        NodePart blockPart = cubeInstance.nodes.get(0).parts.get(0);
        
        // TESTING
	    G3dModelLoader modelLoader = new G3dModelLoader(new UBJsonReader());
	    waterModel = modelLoader.loadModel(Gdx.files.internal("fbx-conv/water.g3db"));
	    wInst = new ModelInstance(waterModel);
	    wInst.transform.scale(10f, 10f, 1);
	    wInst.transform.rotate(1, 0, 0, 90);
	    
	    ModelInstance water2 = new ModelInstance(buildWater());
	    NodePart waterPart = water2.nodes.get(0).parts.get(0);
	    
        renderable = new Renderable();
        waterPart.setRenderable(renderable);
        renderable.environment = null;
        //renderable.meshPart.primitiveType = GL30.GL_POINTS;
        renderable.worldTransform.idt();

        renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
        shader = new WaterShader();
        shader.init();
        
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight,0.8f,0.8f,0.8f,1.0f));
        environment.set(new BlendingAttribute(true, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        
        renderable.environment = environment;
        
		modelBatch = new ModelBatch();
	}

	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        camController.update();
        renderContext.begin();
        shader.begin(camera, renderContext, offset++/100f);
        shader.render(renderable);
        shader.end();
        renderContext.end();
        
        shader.update(offset++/100f, camera, null);
        modelBatch.begin(camera);
		//modelBatch.render(wInst, shader);
		modelBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		
	}	
	private Model buildWater() {
		float waterTilesX = 20f;
		float waterTilesY = 20f;
		float width = 200f;
		float height = 200f;
		
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder meshPBuilder = modelBuilder.part("water1", GL30.GL_TRIANGLES, Usage.Position, new Material());
		for (short i = 0; i < width / waterTilesX - 1; ++i) {
			for (short j = 0; j < height / waterTilesY - 1; ++j) {
				meshPBuilder.rect(new Vector3(i * waterTilesX, j * waterTilesY, 0),
						new Vector3(i * waterTilesX + waterTilesX, j * waterTilesY, 0),
						new Vector3(i * waterTilesX + waterTilesX, j * waterTilesY + waterTilesY, 0),
						new Vector3(i * waterTilesX, j * waterTilesY + waterTilesY, 0),
						new Vector3(0, 0, 1));
			}
		}
		Model water = modelBuilder.end();
		return water;
	}
}
