package com.rowyerboat.rendering;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.gameobjects.Boat;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;

/**
 * Renderer containing and managing all relevant task to render the game to the screen.
 * Contains a GameUI instance and updates/calls its rendering function.
 * 
 * @author Roman Lamsal
 *
 */
public class GameRenderer {
	
	private GameWorld world;
	private Boat boat;
	private Location target;
    protected Array<Location> locations;
	private Vector2[][] currentGrid;
	
	private GameUI gameUI;

	protected PerspectiveCamera camera;
    public float camDistNear = 30f * Settings.boatScale;
    public float camDistFar = 130f * Settings.boatScale;
    public float camOrthoDist = 8000f;
    private Viewport viewport3D;
	private OrthographicCamera hudCam;

	private OrthographicCamera fboCam;
	private FitViewport fboViewport;
	private FrameBuffer fbo;
	private SpriteBatch fboBatch;

	private Vector2 cameraDirV2;
	private Vector3 cameraLookAt;
	private Vector3 cameraPos;
	private Vector2 boatToTarget;
	private Vector2 boatDir;
	private Vector2 boatMid;
	private Vector3 boatMidV3;
	private Vector2 targetPos;
    
	private SpriteBatch batch;
	private SpriteBatch staticBatch;
	protected ShapeRenderer shaper;
	private ShapeRenderer staticShaper;
	
	private BitmapFont font;

	private Polygon boatBox;
	public CameraMode cameraMode = CameraMode.dynamicNear;

    protected ModelBatch modelBatch;
    protected SimpleWaterShader waterShader;
    /** used in the {@link GameRendererOpenGL} version of the game; is now used to switch between
     * using a FBO (shaderID = 0) or non FBO rendering of the game
     * */
    protected int shaderID;
    
    protected Environment environment;
    private Model paddleModel;
    private Model arrowModel;
    private Model boatScene;
    protected ModelInstance boatInstance;
    protected Array<ModelInstance> waterInstances;
	protected Array<ModelInstance> islandInstances;
	private ModelInstance paddleInstance;
    private ModelInstance arrowInstance;
    private AnimationController paddleAnimation;
    private AnimationListener paddleSwingListener;
    private AnimationListener paddleStopListener;
    public boolean renderPaddle = false;
    private boolean renderPaddleSwing = false;
    private boolean renderPaddleStop = false;
    
    private float time = 0f;

    private float boatScale;
    public float islandScale = 1f;

    public GameRenderer() {
    }
    
	public GameRenderer(GameWorld world) {
		this.world = world;
		world.setRenderer(this);
		this.boat = world.getBoat();
		this.target = world.getTarget();
        this.locations = world.getLocations();
		this.boatBox = boat.getHitbox();
		target.getHitbox();
		this.currentGrid = world.getCurrentGrid();
		MathUtils.random.setSeed(world.getSeed());
        boatDir = boat.getDir();
        boatMid = boat.getMid();

    	modelBatch = new ModelBatch();
		
    	hudCam = new OrthographicCamera();
    	hudCam.setToOrtho(false, Settings.width, Settings.height);
    	
		fbo = new FrameBuffer(Format.RGBA8888, Settings.width, Settings.height, true);
		fboCam = new OrthographicCamera();
    	fboViewport = new FitViewport(Settings.width, Settings.height, fboCam);
    	fboBatch = new SpriteBatch();
    	
        createModels();

        // Environment + Camera
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight,0.8f,0.8f,0.8f,1.0f));
        environment.set(new BlendingAttribute(true, 2f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        environment.set(new IntAttribute(IntAttribute.CullFace, GL30.GL_BACK));
        
        //camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera = new PerspectiveCamera();
		viewport3D = new FitViewport(Settings.width, Settings.height, camera);
        camera.near = 1f;
        camera.far = 9000f;
    	cameraLookAt = new Vector3();
    	cameraPos = new Vector3();
    	boatToTarget = new Vector2();
    	cameraDirV2 = boat.getDir().cpy();
    	camera.lookAt(0f, 0f, 0f);
    	camera.position.set(0f, 0f, 0f);
    	camera.update();
    	
    	batch = new SpriteBatch();
    	batch.setProjectionMatrix(camera.combined);
    	staticBatch = new SpriteBatch();
    	staticBatch.setProjectionMatrix(hudCam.combined);
    	
    	shaper = new ShapeRenderer();
    	shaper.setProjectionMatrix(camera.combined);
    	staticShaper = new ShapeRenderer();
    	staticShaper.setProjectionMatrix(hudCam.combined);
    	
    	font = AssetLoader.getFont();

		this.gameUI = new GameUI(boat, camera, staticBatch);
	}

	// ***************************************** RENDER **************************************************
    
	public void render(float delta) {
		renderPaddleStop = boat.stopping;
		renderPaddle = renderPaddleStop || renderPaddleSwing;
		
    	Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		
		target = world.getTarget();
        targetPos = target.getPos();
        boatDir = boat.getDir();
        boatMid = boat.getMid();
        boatMidV3 = new Vector3(boatMid.x, boatMid.y, 0f);
    	boatToTarget = targetPos.cpy().sub(boatMid);

		time += 0.01f;

		shaderID = Settings.shaderID;

		if (shaderID == 0) {
			fbo.begin();
	    	Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		}
		
		processCamera();
		
		// Fading Background
    	if (cameraMode != CameraMode.ortho) {
	        staticShaper.begin(ShapeType.Filled);
	        staticShaper.rect(0, 0, viewport3D.getWorldWidth(), viewport3D.getWorldHeight(),
	        		Color.BLUE, Color.BLUE, Color.WHITE, Color.WHITE);
	        staticShaper.end();
    	}
    	
		// Transform instances
		boatInstance.transform.setToRotation(0, 0, 1, boat.getRotation());
        boatInstance.transform.rotate(1, 0, 0, 90f);
        
		boatInstance.transform.setTranslation(boatMid.x, boatMid.y, 0f)
			.scale(boatScale, boatScale, boatScale);
		
		islandInstances.get(0).transform.scale(1f, islandScale, 1f);
		islandScale = 1f;
		arrowInstance.transform.setToTranslation(targetPos.x, targetPos.y,
				30f + 10f * (MathUtils.sin(time * 10f) * 0.5f + 0.5f)).rotate(1, 0, 0, -90f);
		
		// Render 3D Models
		waterShader.update(delta);
		modelBatch.begin(camera);
		modelBatch.render(waterInstances.get(0), waterShader);
		modelBatch.render(boatInstance, environment);
		if (renderPaddle) {
			paddleInstance.transform.setToRotation(0, 0, 1, boat.getRotation());
	        paddleInstance.transform.rotate(1, 0, 0, 90f);
	        paddleInstance.transform.setTranslation(boatMidV3).scale(boatScale, boatScale, boatScale);
			paddleAnimation.update(Gdx.graphics.getDeltaTime() * 3f);
			modelBatch.render(paddleInstance, environment);
		}
		modelBatch.render(arrowInstance, environment);
		for (int i = 0; i < islandInstances.size; ++i)
			modelBatch.render(islandInstances.get(i), environment);
		modelBatch.end();
		


		// ************************************************** DEBUG STUFF ******************************************
		if (Settings.debug) {
			shaper.setProjectionMatrix(camera.combined);
			shaper.begin(ShapeType.Line);
			shaper.setColor(Color.BLACK);
			shaper.rect(0, 0, world.width, world.height);
			shaper.end();

			debugCurrentGrid();
			debugHitboxes();

		}
		
		if (shaderID == 0) {
			fbo.end();
			
			fboViewport.apply(true);
			fboCam.setToOrtho(true);
			fboCam.update();
			
			fboBatch.setProjectionMatrix(fboCam.combined);
			fboBatch.begin();
			fboBatch.draw(fbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			fboBatch.end();
		}
		
		// HUD
		gameUI.render(delta, boatToTarget, cameraDirV2);
	}
	
	// ********************************** UTILITY **********************************

	public void resize(int width, int height) {
		Gdx.app.log("New Resolution", "" + width + " x " + height);
		viewport3D.update(width, height);
		fboViewport.update(width, height, true);
		gameUI.resize(width, height);
	}

	private void createModels() {
		ModelBuilder modelBuilder = new ModelBuilder();
        arrowModel = modelBuilder.createArrow(new Vector3(0f, 0f, 25f), new Vector3(0f, 0f, 0f),
        		new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position);
        arrowModel = modelBuilder.createCone(20f, 50f, 20f, 15, new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position);
        arrowInstance = new ModelInstance(arrowModel);

        G3dModelLoader modelLoader = new G3dModelLoader(new UBJsonReader());
        G3dModelLoader modelLoader2 = new G3dModelLoader(new JsonReader());
        
        // Models
        paddleModel = modelLoader2.loadModel(Gdx.files.getFileHandle("models/paddle.g3dj", Files.FileType.Internal));
        boatScene = modelLoader.loadModel(Gdx.files.getFileHandle("models/boatScene.g3db", Files.FileType.Internal));
        
        // Instances
        boatInstance = new ModelInstance(boatScene, "Boat");
		boatScale = boat.getScale();
		
        paddleInstance = new ModelInstance(paddleModel);
        
        
        // ********************************** TODO ISLANDS **************************
        
        islandInstances = Settings.map.islandInstances;
	    
        /* not yet implemented properly 
         * will fill the instances array with the proper modelinstances (based on order)
         */
        /*
        Model islandModel = modelLoader.loadModel(Gdx.files.getFileHandle("models/lesserAntillesDemo.g3db",
        		Files.FileType.Internal));
		for (int i = 0; i < islands.size; ++i) {
		        ModelInstance islandInstance = new ModelInstance(islandModel, islands.get(i).name");
		        islandInstance.transform.setToRotation(1, 0, 0, 90f).scale(1f, 0.5f, 1f);
		        islandInstances.add(islandInstance);
	        }	
        	);
        */
        
        // Animations
        paddleAnimation = new AnimationController(paddleInstance);
        paddleSwingListener = new AnimationListener() {
        	@Override
			public void onEnd(AnimationController.AnimationDesc animation) {
        		renderPaddleSwing = false;
        		paddleAnimation = new AnimationController(paddleInstance);
        	}
			@Override
			public void onLoop(AnimationDesc animation) {
			}
        };
        paddleStopListener = new AnimationListener() {
			@Override
			public void onEnd(AnimationDesc animation) {
			}
			@Override
			public void onLoop(AnimationDesc animation) {
			}
        };
		
		// WATER - TESTING
		waterInstances = new Array<ModelInstance>();
		
		waterShader = new SimpleWaterShader();
		waterShader.init();
		Texture tex1 = AssetLoader.noise0,
				tex2 = AssetLoader.noise1,
				tex3 = AssetLoader.noise2,
				tex4 = AssetLoader.noise3;
		tex1.bind(2);
		tex2.bind(3);
		tex3.bind(4);
		tex4.bind(5);
    	Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
    	Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
		
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		MeshPartBuilder meshPBuilder = builder.part("ocean", GL30.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.ColorUnpacked | Usage.TextureCoordinates,
				new Material(ColorAttribute.createDiffuse(0.5f, 0, 0.5f, 0.5f)));
		meshPBuilder.rect(
						new Vector3(0, 0, 0),
						new Vector3(world.width, 0, 0),
						new Vector3(world.width, world.height, 0),
						new Vector3(0, world.height, 0),
						new Vector3(0, 0, 1));
		Model model = builder.end();
		
		waterInstances.add(new ModelInstance(model));
	}

	public void leftSwing() {
		paddleAnimation.setAnimation("Paddle|paddle_leftswing", 1, paddleSwingListener);
		renderPaddleSwing = true;
	}
	
	public void rightSwing() {
		paddleAnimation.setAnimation("Paddle|paddle_rightswing", 1, paddleSwingListener);
		renderPaddleSwing = true;
	}
	
	public void stopLeft() {
		paddleAnimation.setAnimation("Paddle|paddle_leftstop", -1, paddleStopListener);
		renderPaddleStop = true;
	}
	
	public void stopRight() {
		paddleAnimation.setAnimation("Paddle|paddle_rightstop", -1, paddleStopListener);
		renderPaddleStop = true;
	}

	public void dispose() {
		staticBatch.dispose();
		shaper.dispose();
		batch.dispose();
		staticShaper.dispose();
		gameUI.dispose();
	}

	// ********************************** CAMERA **********************************
	private void processCamera() {
		switch(cameraMode) {
    	case toTarget:
    	default:
    		cameraDirV2.set(boatToTarget);
        	cameraUpdate();
    		break;
    		
    	case toTargetNear:
    		cameraDirV2.set(boatToTarget);
    		cameraUpdateNear();
    		break;
    		
    	case boatDir:
    		cameraDirV2.set(boatDir);
        	cameraUpdate();
    		break;
    		
    	case boatDirNear:
    		cameraDirV2.set(boatDir);
        	cameraUpdateNear();
    		break;

    	case dynamic:
    		float degreePerFrame = 0.1f;
    		float angle = boatDir.angle(cameraDirV2);
    		if (Math.abs(angle) > 30) {
    			if (Math.abs(angle) < 60)
    				degreePerFrame *= 5;
    			else
    				degreePerFrame *= 15;
    		}
			if (angle < 1)
				cameraDirV2.rotate(degreePerFrame);
			else if (angle > 1)
				cameraDirV2.rotate(-degreePerFrame);	
    		cameraUpdate();
    		break;
    		
    	case dynamicNear:
    		degreePerFrame = 0.1f;
    		angle = cameraDirV2.angle(boatDir) * 3;
    		if (Math.abs(angle) > 30) {
    			if (Math.abs(angle) < 60)
    				degreePerFrame *= 5;
    			else
    				degreePerFrame *= 10;
    			
    			degreePerFrame = (float) Math.pow(Math.abs(angle)/180f, 1);
    		}
			if (angle < -5)
				cameraDirV2.rotate(-degreePerFrame);
			else if (angle > 5)
				cameraDirV2.rotate(degreePerFrame);
			cameraUpdateNear();
    		break;


    	case dynamicTarget:
    		float angleBreakpoint = 65f;
    		if (liesInside(boatDir, boatToTarget, angleBreakpoint))
    			cameraDirV2.set(boatToTarget);
    		else {
    			if (boatDir.angle(boatToTarget) < 0)
    				cameraDirV2.set(boatDir.cpy().rotate(-angleBreakpoint));
    			else
    				cameraDirV2.set(boatDir.cpy().rotate(angleBreakpoint));
    		}
        	cameraUpdate();
    		break;
    		
    	case dynamicTargetNear:
    		float angleBreakpointNear = 65f;
    		if (liesInside(boatDir, boatToTarget, angleBreakpointNear))
    			cameraDirV2.set(boatToTarget);
    		else {
    			if (boatDir.angle(boatToTarget) < 0)
    				cameraDirV2.set(boatDir.cpy().rotate(-angleBreakpointNear));
    			else
    				cameraDirV2.set(boatDir.cpy().rotate(angleBreakpointNear));
    		}
        	cameraUpdateNear();
    		break;
    		
    	case ortho:
    		cameraOrtho();
    		camera.update();
    		break;
    	}
	}

	
	private void cameraUpdate() {
		float ratio = 1.154f;
        cameraPos.set(boatMid.cpy().sub(cameraDirV2.setLength(camDistFar * ratio)), camDistFar);
        camera.position.set(cameraPos);
        cameraLookAt.set(boatMid.cpy().add(cameraDirV2.setLength(camDistFar * ratio + 20f)), 0f);
        camera.lookAt(cameraLookAt);
        camera.up.set(0f, 0f, 1f);
        camera.update();
	}
	
	private void cameraUpdateNear() {
		float ratio = 1.83f;
        cameraPos.set(boatMid.cpy().sub(cameraDirV2.setLength(camDistNear * ratio)), camDistNear);
        camera.position.set(cameraPos);
        cameraLookAt.set(boatMid.cpy().add(cameraDirV2.setLength(camDistNear * ratio + 20f)), 0f);
        camera.lookAt(cameraLookAt);
        camera.up.set(0f, 0f, 1f);
        camera.update();
	}
	
	private void cameraOrtho() {
		cameraDirV2.set(boatDir.cpy());
		
		cameraPos.set(world.width / 2, world.height / 2, camOrthoDist);
		cameraLookAt.set(world.width / 2, world.height / 2, 0f);
		
		camera.position.set(cameraPos);
		camera.lookAt(cameraLookAt);
		camera.up.set(0, 1, 0);
		camera.update();
	}
	
	public boolean liesInside(Vector2 vec1, Vector2 vec2, float maxDegree) {
		return vec1.angle(vec2) < maxDegree	&& vec1.angle(vec2) > -maxDegree;
	}
	
	public static enum CameraMode {
		toTarget, toTargetNear, boatDir, boatDirNear, dynamic, dynamicNear,
		dynamicTarget, dynamicTargetNear, ortho;
		
		public static CameraMode[] vals = values();
			
		public CameraMode next() {
			if (this.ordinal() + 2 < vals.length)
				return vals[(this.ordinal() + 2) % vals.length];
			else
				return vals[(this.ordinal() + 2) % 2];
		}
		
		public CameraMode changeDistance() {
			if (this.ordinal() != vals.length - 1) {
				if (this.ordinal() % 2 == 0)
					return vals[this.ordinal() + 1];
				else
					return vals[this.ordinal() - 1];
			}
			else
				return this;
		}
		
		public CameraMode previous() {
			if (this.ordinal() - 2 < 0)
				return vals[vals.length - this.ordinal() % 2 - 1];
			else
				return vals[this.ordinal() - 2];
		}
	}

	// ********************************** DEBUG **********************************
	
	protected void debugCurrentGrid() {
		shaper.begin(ShapeType.Filled);
		shaper.setColor(Color.RED);
		for (int i = 0; i < (currentGrid != null ? currentGrid.length : 0); ++i)
			for (int j = 0; j < currentGrid[0].length; ++j) {
				shaper.circle(i * 100f, j * 100f, 2.5f);
				// Draw lines for vectors
				shaper.line(i * 100f, j * 100f, i * 100f + currentGrid[i][j].x * 100f, j * 100f + currentGrid[i][j].y * 100f);
			}
		shaper.end();
	}
	
	protected void debugHitboxes() {
		// Boat und Target Hitbox / Boat.Mid
		shaper.begin(ShapeType.Line);
		shaper.setAutoShapeType(true);
		shaper.identity();
		shaper.setColor(Color.RED);
		shaper.polygon(boatBox.getTransformedVertices());
		shaper.setColor(Color.FIREBRICK);
		for (int i = 0; i < locations.size; ++i) {
			shaper.polygon(locations.get(i).getHitbox().getTransformedVertices());
		}
		shaper.circle(boatMid.x, boatMid.y, 5);
		shaper.circle(boatMid.x, boatMid.y, 1);
		if (cameraMode == CameraMode.ortho) {
			shaper.set(ShapeType.Filled);
			shaper.circle(boatMid.x, boatMid.y, 100);
			shaper.set(ShapeType.Line);
		}
		Vector2 displace = boat.getDirOverGround();
		shaper.line(boat.getPos().x + displace.x, boat.getPos().y + displace.y,
				boat.getPos().x, boat.getPos().y, Color.RED, Color.BLACK);
		displace = boat.getDir();
		shaper.line(boat.getPos().x + displace.x, boat.getPos().y + displace.y,
				boat.getPos().x, boat.getPos().y, Color.RED, Color.BLACK);
		shaper.end();
	}
	
	public enum ShaderIDName {
		choppyWave, simpleWave, flatWave, choppy, choppyFlat, defaultFlat;
	}

	public void resetBoat(Boat boat) {
		this.boat = boat;
		this.cameraDirV2 = boat.getDir();
		gameUI.boat = boat;
	}
}