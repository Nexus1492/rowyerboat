package com.rowyerboat.rendering;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.UBJsonReader;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.gameobjects.Boat;
import com.rowyerboat.gameobjects.Current;
import com.rowyerboat.gameobjects.Island;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.BoatFloater;
import com.rowyerboat.helper.SVGReader;
import com.rowyerboat.helper.Settings;

public class GameRendererOpenGL extends GameRenderer{
	
	private GameWorld world;
	private Boat boat;
	private Location target;
    private Array<Location> locations;
	private Array<Current> currents;
	private Vector2[][] currentGrid;
	
	private GameUI gameUI;
	
	private PerspectiveCamera camera;
    public float camDistNear = 30f * Settings.boatScale;
    public float camDistFar = 130f * Settings.boatScale;
    public float camOrthoDist = 8000f;
    private Viewport viewport3D;
    
	private OrthographicCamera hudCam;
	private Viewport viewportHUD;
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
	private ShapeRenderer shaper;
	private ShapeRenderer staticShaper;
	
	private BitmapFont font;

	private Texture arrowImage;
	private Texture cloudImage;
	
	private Polygon boatBox;
	private Polygon targetBox;
	
	public CameraMode cameraMode = CameraMode.dynamicNear;

    private ModelBatch modelBatch;
    private WaterShader waterShader;
    private int shaderID;
    
    private Environment environment;
    private Model boatModel;
	private Model waterModel;
	private Model paddleModel;
    private Model arrowModel;
    private Model boatScene;
    private ModelInstance boatInstance;
	private Array<ModelInstance> islandInstances;
	private ModelInstance waterInstance;
	private Array<ModelInstance> waterInstances;
	private ModelInstance paddleInstance;
    private ModelInstance arrowInstance;
    private AnimationController paddleAnimation;
    private AnimationListener paddleAnimListener;

	private float waterTilesX;
	private float waterTilesY;

	public boolean renderPaddle = false;
    
    private float time = 0f;
    private BoatFloater boatFloater;
    
    private float boatScale;
    public float islandScale = 1f;
    
    
	public GameRendererOpenGL(GameWorld world) {
		this.world = world;
		world.setRenderer(this);
		this.boat = world.getBoat();
		this.target = world.getTarget();
        this.locations = world.getLocations();
		this.boatBox = boat.getHitbox();
		this.targetBox = target.getHitbox();
		this.currents = world.getCurrents();
		this.currentGrid = world.getCurrentGrid();
		MathUtils.random.setSeed(world.getSeed());
        boatDir = boat.getDir();
        boatMid = boat.getMid();

    	waterTilesX = world.width / 200f;
    	waterTilesY = world.height / 200f;

        modelBatch = new ModelBatch();
        waterShader = new WaterShader();
        waterShader.init();
		
    	hudCam = new OrthographicCamera();
    	hudCam.setToOrtho(false, Settings.width, Settings.height);

        ModelBuilder modelBuilder = new ModelBuilder();
        arrowModel = modelBuilder.createArrow(new Vector3(0f, 0f, 25f), new Vector3(0f, 0f, 0f),
        		new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position);
        arrowModel = modelBuilder.createCone(20f, 50f, 20f, 15, new Material(ColorAttribute.createDiffuse(Color.RED)), Usage.Position);
        arrowInstance = new ModelInstance(arrowModel);

        G3dModelLoader modelLoader = new G3dModelLoader(new UBJsonReader());
        
        // Models
        paddleModel = modelLoader.loadModel(Gdx.files.getFileHandle("models/paddle.g3db", Files.FileType.Internal));
        boatScene = modelLoader.loadModel(Gdx.files.getFileHandle("models/boatScene.g3db", Files.FileType.Internal));
        
        // Instances
        boatInstance = new ModelInstance(boatScene, "Boat");
		boatScale = boat.getScale();
		
        paddleInstance = new ModelInstance(paddleModel);
        
        waterInstance = new ModelInstance(buildWater());
        waterInstances = new Array<ModelInstance>(); // TODO
        for (int i = 0; i < world.width; i += 100)
        	for (int j = 0; j < world.height; j += 100) {
        		ModelInstance inst = new ModelInstance(waterModel);
        		inst.transform.setToTranslation(i, j, 0f);
        		waterInstances.add(inst);
        	}
        
        // ********************************** TODO ISLANDS **************************
        
        islandInstances = new Array<ModelInstance>();
        if (currentGrid != null) {
	        //DemoModel
        	Model islandModel = modelLoader.loadModel(Gdx.files.getFileHandle("models/lesserAntillesDemo.g3db", Files.FileType.Internal));
	        ModelInstance islandInstance = new ModelInstance(islandModel, "all");
	        islandInstance.transform.setToRotation(1, 0, 0, 90f).scale(1f, 0.5f, 1f);
	        islandInstances.add(islandInstance);
        } else if (currents != null) {
	        islandInstances.add(new ModelInstance(boatScene, "Island"));
	        islandInstances.get(0).transform.setToScaling(2f, 2f, 2f)
	        	.rotate(1, 0, 0, 90f)
	        	.setTranslation(targetPos.x, targetPos.y, 0f);
        }
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
        paddleAnimListener = new AnimationListener() {
        	@Override
			public void onEnd(AnimationController.AnimationDesc animation) {
        		renderPaddle = false;
        		paddleAnimation = new AnimationController(paddleInstance);
        	}
			@Override
			public void onLoop(AnimationDesc animation) {
			}
        };

        // Environment + Camera
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight,0.8f,0.8f,0.8f,1.0f));
        environment.set(new BlendingAttribute(true, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        
        //camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera = new PerspectiveCamera();
        viewport3D = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
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
    	
    	font = new BitmapFont();
    	
    	initAssets();
    	gerstnerWaving();

		this.gameUI = new GameUI(boat, camera, staticBatch, font);
	}

	// ***************************************** RENDER **************************************************
    
	public void render(float delta) {
    	//Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    	Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
    	
		target = world.getTarget();
        targetPos = target.getPos();
        
        boatDir = boat.getDir();
        boatMid = boat.getMid();
        boatMidV3 = new Vector3(boatMid.x, boatMid.y, 0f);
    	boatToTarget = targetPos.cpy().sub(boatMid);

    	boatFloater.update(time);
		waterShader.update(time, camera, world);
		time += 0.01f;

		shaderID = Settings.shaderID;
    	processCamera();

		// Fading Background
    	if (waterInstance.model.meshParts.get(0).primitiveType != GL30.GL_POINTS && cameraMode != CameraMode.ortho) {
	        staticShaper.begin(ShapeType.Filled);
	        staticShaper.rect(0, 0, viewport3D.getWorldWidth(), viewport3D.getWorldHeight(),
	        		Color.BLUE, Color.BLUE, Color.WHITE, Color.WHITE);
	        staticShaper.end();
    	}
        
        // Move the Clouds
        if (cameraMode != CameraMode.ortho);
        	//moveClouds();
        
        Gdx.gl.glEnable(GL30.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		// Model the Boat - including wave-riding
		boatInstance.transform.setToRotation(0, 0, 1, boat.getRotation());
        boatInstance.transform.rotate(1, 0, 0, 90f);
        
		Vector3 boatFloatNorm = new Vector3(0, 0, 1);
        Vector3 boatFloatPos = boatFloater.getHeight(boatMid.x, boatMid.y, boatFloatNorm);
        float newHeight = (boatFloater.getHeight(boat.bowPoint.x, boat.bowPoint.y, null).z 
        		- boatFloater.getHeight(boat.sternPoint.x, boat.sternPoint.y, null).z);
		boatInstance.transform.setTranslation(boatMid.x, boatMid.y, (shaderID < 2 ? (2f + newHeight) * boatScale : 0f))
			.rotate(0, 0, 1, shaderID < 2 ? newHeight / 2f : 0f)
			.scale(boatScale, boatScale, boatScale);
        //boatInstance.transform.scale(boatScale, boatScale, boatScale);


		// Render stuff
		modelBatch.begin(camera);
		modelBatch.render(boatInstance, environment);
		
		// ************************************* SHADERMODE FOR WATER **************************************
		switch(shaderID) {
		default:
		case 0:
			waterShader.program.begin();
			waterShader.program.setUniformf("u_kabbelWater", 1);
			waterShader.program.setUniformf("u_renderHeight", 1);
			waterShader.program.setUniformf("u_waveWater", 1);
			waterShader.program.end();
			modelBatch.render(waterInstance, environment, waterShader);
			break;

		case 1:
			waterShader.program.begin();
			waterShader.program.setUniformf("u_kabbelWater", 0);
			waterShader.program.setUniformf("u_renderHeight", 1);
			waterShader.program.setUniformf("u_waveWater", 1);
			waterShader.program.end();
			modelBatch.render(waterInstance, environment, waterShader);
			break;

		case 2:
			waterShader.program.begin();
			waterShader.program.setUniformf("u_kabbelWater", 0);
			waterShader.program.setUniformf("u_renderHeight", 0);
			waterShader.program.setUniformf("u_waveWater", 1);
			waterShader.program.end();
			modelBatch.render(waterInstance, environment, waterShader);
			break;
			
		case 3:
			waterShader.program.begin();
			waterShader.program.setUniformf("u_kabbelWater", 1);
			waterShader.program.setUniformf("u_renderHeight", 1);
			waterShader.program.setUniformf("u_waveWater", 0);
			waterShader.program.end();
			modelBatch.render(waterInstance, environment, waterShader);
			break;
		
		case 4:
			waterShader.program.begin();
			waterShader.program.setUniformf("u_kabbelWater", 1);
			waterShader.program.setUniformf("u_renderHeight", 0);
			waterShader.program.setUniformf("u_waveWater", 0);
			waterShader.program.end();
			modelBatch.render(waterInstance, environment, waterShader);
			break;
			
		case 5:
			modelBatch.render(waterInstance, environment);
			break;
		}
		
		// Model the Paddle, if necessery
		if (renderPaddle) {
			paddleInstance.transform.setToRotation(0, 0, 1, boat.getRotation());
	        paddleInstance.transform.rotate(1, 0, 0, 90f).setTranslation(boatMidV3).scale(boatScale, boatScale, boatScale);
			paddleAnimation.update(Gdx.graphics.getDeltaTime() * 3f);
			modelBatch.render(paddleInstance, environment);
		}
		modelBatch.end();
		
    	/*Gdx.gl.glColorMask(false, false, false, false); // Interessant: colorMask ausschalten, der "Deckel" des Bootes wird "fest" gerendert
		modelBatch.begin(camera);
		modelBatch.render(boatInstance, environment);
		modelBatch.end();
		Gdx.gl.glColorMask(true, true, true, true);*/

		// Scale for debugging
		islandInstances.get(0).transform.scale(1f, islandScale, 1f);
		islandScale = 1f;
		
		arrowInstance.transform.setToTranslation(targetPos.x, targetPos.y,
				30f + 10f * (MathUtils.sin(time * 10f) * 0.5f + 0.5f)).rotate(1, 0, 0, -90f);
		
		// Render the islands and targetarrow
		modelBatch.begin(camera);
		if (currentGrid != null)
			modelBatch.render(arrowInstance, environment);
		for (int i = 0; i < islandInstances.size; ++i)
			modelBatch.render(islandInstances.get(i), environment);
		modelBatch.end();
		Gdx.gl.glCullFace(GL30.GL_BACK);


		// ************************************************** DEBUG STUFF ******************************************
		if (Settings.debug) {
			shaper.setProjectionMatrix(camera.combined);
			
			/*shaper.begin(ShapeType.Filled);
			shaper.setColor(Color.BLACK);
			shaper.point(boatMid.x + 19f, boatMid.y, 0f);
			shaper.point(boatFloatPos.x, boatFloatPos.y, boatFloatPos.z + 2);
			Vector3 helpVec = boatFloatPos.cpy().add(boatFloatNorm.cpy().setLength(10f));
			shaper.point(helpVec.x, helpVec.y, helpVec.z);
			
			shaper.setColor(Color.RED);
			helpVec = boatFloater.getHeight(waterTilesX*13, waterTilesY*13, null);
			shaper.point(helpVec.x, helpVec.y, helpVec.z);
			
			Vector2 backP = new Vector2(boat.getHitbox().getTransformedVertices()[2], boat.getHitbox().getTransformedVertices()[3]);
			Vector2 frontP = new Vector2(boat.getHitbox().getTransformedVertices()[8], boat.getHitbox().getTransformedVertices()[9]);
			backP = boat.sternPoint;
			frontP = boat.bowPoint;
			
			shaper.setColor(Color.WHITE);
			shaper.point(frontP.x, frontP.y, boatFloater.getHeight(boat.bowPoint.x, boat.bowPoint.y, null).z);
			shaper.setColor(Color.ORANGE);
			shaper.point(backP.x, backP.y, boatFloater.getHeight(boat.sternPoint.x, boat.sternPoint.y, null).z);
			
			shaper.end();*/
			
			shaper.setProjectionMatrix(camera.combined);
			shaper.begin(ShapeType.Line);
			shaper.setColor(Color.BLACK);
			shaper.rect(0, 0, world.width, world.height);
			shaper.end();

			if (currents != null)
				debugCurrents();
			if (currentGrid != null)
				debugCurrentGrid();
			debugHitboxes();
			
		}

		// HUD
		gameUI.render(delta, boatToTarget, cameraDirV2);
	}
	
	// ********************************** UTILITY **********************************
	
	private boolean gerstnerWaving() {
		int numWaves = 16;
		float[] amp = new float[numWaves];
		float[] waveLen = new float[numWaves];
		float[] speed = new float[numWaves];
		float[] steepness = new float[numWaves];
		Array<Vector2> dir = new Array<Vector2>();
		Vector2 windDir = world.windDir.scl(-1f);
		
		for (int i = 0; i < numWaves; ++i) {
			dir.add(windDir.cpy().rotate(MathUtils.random() * 60f - 30f).nor());
			amp[i] = MathUtils.random() * 5f * boatScale;
			waveLen[i] = 220 + MathUtils.random() * 100f;
			speed[i] = 220 + MathUtils.random() * 100f;
			steepness[i] = MathUtils.random() * 1f;
			
			/*amp[i] = 3f;
			waveLen[i] = 220;
			speed[i] = 220;
			steepness[i] = 0f;*/
		}
		
		boatFloater = new BoatFloater(amp, waveLen, speed, steepness, dir);
		waterShader.updateGerstner(amp, waveLen, speed, steepness, dir);
		
		return true;
	}
	
	private Model buildWater() {
		int indexer = 0;
		int counter = 0;
		int ueberhang = 0;
		int numXTiles = (int) ((world.width) / waterTilesX + 1 + 2*ueberhang),
				numYTiles = (int) ((world.height) / waterTilesY + 1 + 2*ueberhang);
		float timeStart = TimeUtils.nanoTime();
		Material material = new Material(ColorAttribute.createDiffuse(new Color(0f, 0f, 0.8f, 0.5f)));

		Array<Float> verts2 = new Array<Float>();
		ShortArray shorts = new ShortArray();
		int primType = GL30.GL_TRIANGLES;
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder meshPBuilder = modelBuilder.part("water" + counter++, primType, Usage.Position, material);
		for (int i = 0; i < numXTiles; ++i) {
			for (int j = 0; j < numYTiles; ++j) {
				verts2.add((i - ueberhang) * waterTilesX);
				verts2.add((j - ueberhang) * waterTilesY);
				verts2.add(0f);
				/*
				 * 1 - 2,3
				 * | 	 |
				 * 0,5 - 4
				 */
				if (i > 0 && j > 0) {
					if ((i+j) % 2 == 1 && true) {
						shorts.add(indexer - numXTiles - 1);
						shorts.add(indexer - 1);
						shorts.add(indexer);
						shorts.add(indexer);
						shorts.add(indexer - numXTiles);
						shorts.add(indexer - numXTiles - 1);
					} else {
						shorts.add(indexer);
						shorts.add(indexer - numXTiles);
						shorts.add(indexer - numXTiles - 1);
						shorts.add(indexer - numXTiles - 1);
						shorts.add(indexer - 1);
						shorts.add(indexer);
					}
				}
				indexer++;
			}
		}
		float[] verts = new float[verts2.size];
		for (int i = 0; i < verts2.size; ++i)
			verts[i] = verts2.get(i).floatValue();
		Gdx.app.log("index", "" + indexer*3);
		meshPBuilder.addMesh(verts, shorts.items);
		Gdx.app.log("BuilderCount", "" + counter + ", time: " + ((TimeUtils.nanoTime() - timeStart)));
		waterModel = modelBuilder.end();
		Gdx.app.log("MeshBuilder", "meshParts.size: " + waterModel.meshParts.size + 
				"\n meshes.size: " + waterModel.meshes.size +
				"\n water.meshparts[0].mesh.getNumVertices: " + waterModel.meshParts.get(0).mesh.getNumVertices() +
				"\n vertexAttributes.size: " + waterModel.meshParts.get(0).mesh.getVertexAttributes().size());
		return waterModel;
	}

	private Model buildWaterDeprecated() {
int fail = 0; 
		int floatIndexer = 0;
		int shortIndexer = 0;
		int indexer = 0;
		int counter = 0;
		int ueberhang = 0;
		int numXTiles = (int) ((world.width) / waterTilesX + 1 + 2*ueberhang),
				numYTiles = (int) ((world.height) / waterTilesY + 1 + 2*ueberhang);
		float timeStart = TimeUtils.nanoTime();
		Material material = new Material(ColorAttribute.createDiffuse(new Color(0f, 0f, 0.8f, 0.5f)));

		float[] floats = new float[numXTiles * numYTiles * 3];
		short[] shorts = new short[numXTiles * numYTiles * 6];
		int primType = GL30.GL_POINTS;
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder meshPBuilder = modelBuilder.part("water" + counter++, primType, Usage.Position, material);
		for (float j = 0; j < numYTiles; ++j) {
			for (float i = 0; i < numXTiles; ++i) {
				if ((short) indexer < 0) {
					meshPBuilder.addMesh(floats, shorts);
					floatIndexer = 0;
					shortIndexer = 0;
					indexer = 0;
					floats = new float[numXTiles * numYTiles * 3];
					shorts = new short[numXTiles * numYTiles * 6];
				}
				floats[floatIndexer++] = ((i - ueberhang) * waterTilesX);
				floats[floatIndexer++] = ((j - ueberhang) * waterTilesY);
				floats[floatIndexer++] = (0f);
				/*
				 * 1 - 2,3
				 * | 	 |
				 * 0,5 - 4
				 */
				if (i > 0 && j > 0 && indexer > numXTiles) {
					shorts[shortIndexer++] = (short) (indexer - numXTiles - 1);
					shorts[shortIndexer++] = (short) (indexer - 1);
					shorts[shortIndexer++] = (short) (indexer);
					shorts[shortIndexer++] = (short) (indexer);
					shorts[shortIndexer++] = (short) (indexer - numXTiles);
					shorts[shortIndexer++] = (short) (indexer - numXTiles - 1);
					for(int l = shortIndexer - 6; l < shortIndexer; ++l) if ((shorts[l]) < 0) System.out.println(l + ": " + shorts[l]);
				}
				indexer += 1;
			}
		}
		meshPBuilder.addMesh(floats, shorts);
		Gdx.app.log("BuilderCount", "" + counter + ", time: " + ((TimeUtils.nanoTime() - timeStart)));
		waterModel = modelBuilder.end();
		Gdx.app.log("MeshBuilder", "meshParts.size: " + waterModel.meshParts.size + 
				"\n meshes.size: " + waterModel.meshes.size +
				"\n water.meshparts[0].mesh.getNumVertices: " + waterModel.meshParts.get(0).mesh.getNumVertices() +
				"\n vertexAttributes.size: " + waterModel.meshParts.get(0).mesh.getVertexAttributes().size());
		return waterModel;
	}
	
	private Model buildWaterTiles() {
		int indexer = 0;
		int counter = 0;
		float tilesX = 5f;
		float tilesY = 5f;
		
		int numXTiles = (int) (100 / tilesX) + 1,
				numYTiles = (int) (100 / tilesY) + 1;
		float timeStart = TimeUtils.nanoTime();
		Material material = new Material(ColorAttribute.createDiffuse(new Color(0f, 0f, 0.8f, 0.5f)));

		Array<Float> verts2 = new Array<Float>();
		ShortArray shorts = new ShortArray();
		int primType = GL30.GL_TRIANGLES;
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder meshPBuilder = modelBuilder.part("water" + counter++, primType, Usage.Position, material);
		for (int i = 0; i < numXTiles; ++i) {
			for (int j = 0; j < numYTiles; ++j) {
				verts2.add(i * tilesX);
				verts2.add(j * tilesY);
				verts2.add(0f);
				/*
				 * 1 - 2,3
				 * | 	 |
				 * 0,5 - 4
				 */
				if (i > 0 && j > 0) {
					shorts.add(indexer - numXTiles - 1);
					shorts.add(indexer - 1);
					shorts.add(indexer);
					shorts.add(indexer);
					shorts.add(indexer - numXTiles);
					shorts.add(indexer - numXTiles - 1);
				}
				indexer++;
			}
		}
		float[] verts = new float[verts2.size];
		for (int i = 0; i < verts2.size; ++i)
			verts[i] = verts2.get(i).floatValue();
		meshPBuilder.addMesh(verts, shorts.items);
		Gdx.app.log("BuilderCount", "" + counter + ", time: " + ((TimeUtils.nanoTime() - timeStart)));
		waterModel = modelBuilder.end();
		Gdx.app.log("MeshBuilder", "meshParts.size: " + waterModel.meshParts.size + 
				"\n meshes.size: " + waterModel.meshes.size +
				"\n water.meshparts[0].mesh.getNumVertices: " + waterModel.meshParts.get(0).mesh.getNumVertices() +
				"\n vertexAttributes.size: " + waterModel.meshParts.get(0).mesh.getVertexAttributes().size());
		return waterModel;
	}
	
	/*private void moveClouds() {
		for (int i = 0; i < clouds.size; ++i) {
			Cloud cloud = clouds.get(i);
			if (boat.getRotation() < 0)
				cloud.move(-1);
			else
				cloud.move(1);
			staticBatch.begin();
			staticBatch.draw(cloudImage, clouds.get(i).pos.x, clouds.get(i).pos.y);
			staticBatch.end();
		}
	}*/

	public void leftSwing() {
		paddleAnimation.setAnimation("Paddle|paddle_leftswing", 1, paddleAnimListener);
		renderPaddle = true;
	}
	
	public void rightSwing() {
		paddleAnimation.setAnimation("Paddle|paddle_rightswing", 1, paddleAnimListener);
		renderPaddle = true;
	}

	private void initAssets() {
		arrowImage = AssetLoader.arrow;
	}

	public void dispose() {
		font.dispose();
		staticBatch.dispose();
		shaper.dispose();
		batch.dispose();
		staticShaper.dispose();
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
    				degreePerFrame *= 2.5;
    			else
    				degreePerFrame *= 5;
    		}
			if (angle < 1)
				cameraDirV2.rotate(degreePerFrame);
			else if (angle > 1)
				cameraDirV2.rotate(-degreePerFrame);	
    		cameraUpdate();
    		break;
    		
    	case dynamicNear:
    		degreePerFrame = 0.1f;
    		angle = boatDir.angle(cameraDirV2);
    		if (Math.abs(angle) > 30) {
    			if (Math.abs(angle) < 60)
    				degreePerFrame *= 5;
    			else
    				degreePerFrame *= 10;
    		}
			if (angle < -5)
				cameraDirV2.rotate(degreePerFrame);
			else if (angle > 5)
				cameraDirV2.rotate(-degreePerFrame);	
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
		cameraDirV2.set(boatDir);
		
		//TODO REMOVE
		cameraPos.set(world.width / 2, world.height / 2, camOrthoDist);
		cameraLookAt.set(world.width / 2, world.height / 2, 0f);
		
		//cameraPos.set(boatMid, 500f);
		camera.position.set(cameraPos);
		//cameraLookAt.set(boatMid, 0f);
		camera.lookAt(cameraLookAt);
		//camera.up.set(boatDir, 0);
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
	
	private Vector3 vec2to3(Vector2 vec) {
		return vec2to3(vec, 0f);
	}
	
	private Vector3 vec2to3(Vector2 vec, float z) {
		return new Vector3(vec.x, vec.y, z);
	}

	// ********************************** DEBUG **********************************
	private void debugCurrents() {
		for (int i = 0; i < currents.size; ++i)
			currents.get(i).draw(shaper);
		
		float[] vertices = new float[]{
				1000, 0,
				800, 200,
				500, 300,
				400, 500,
				500, 700,
				700, 700,
				800, 1000,
				600, 1500
			};
		shaper.begin(ShapeType.Filled);
		shaper.setColor(Color.MAGENTA);
		for (int i = 1; i < 16; i+= 2)
			shaper.circle(vertices[i-1], vertices[i], 5f);
		shaper.end();
	}
	
	private void debugCurrentGrid() {
		shaper.begin(ShapeType.Filled);
		shaper.setColor(Color.RED);
		for (int i = 0; i < currentGrid.length; ++i)
			for (int j = 0; j < currentGrid[0].length; ++j) {
				shaper.circle(i * 100f, j * 100f, 2.5f);
				// Draw lines for vectors
				shaper.line(i * 100f, j * 100f, i * 100f + currentGrid[i][j].x * 100f, j * 100f + currentGrid[i][j].y * 100f);
			}
		shaper.end();
	}
	
	private void debugHitboxes() {
		// Boat und Target Hitbox / Boat.Mid
		shaper.begin(ShapeType.Line);
		shaper.identity();
		shaper.setColor(Color.RED);
		shaper.polygon(boatBox.getTransformedVertices());
		shaper.setColor(Color.FIREBRICK);
		for (int i = 0; i < locations.size; ++i) {
			shaper.polygon(locations.get(i).getHitbox().getTransformedVertices());
		}
		shaper.circle(boatMid.x, boatMid.y, 5);
		shaper.circle(boatMid.x, boatMid.y, 1);
		shaper.end();
	}
	
	public enum ShaderIDName {
		choppyWave, simpleWave, flatWave, choppy, choppyFlat, defaultFlat;
	}

	public void resize(int width, int height) {
		viewport3D.update(width, height);
		gameUI.resize(width, height);
	}
}