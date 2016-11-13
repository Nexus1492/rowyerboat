package deprecated;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.rowyerboat.gameobjects.Boat;
import com.rowyerboat.gameobjects.Island;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.AssetLoader;

/** DEPRECATED - GAME IS NOW 3D - USE GameRenderer3D-Object */
public class GameRenderer2D {
	
	private GameWorld2D world;
	private Boat2D boat;
	private Island2D target;
	
	private OrthographicCamera camera;
	private float boatToCamDistance;
	private boolean isDynamicCamera;
	private Vector2 cameraDirV2;
	private Vector2 cameraUpV2;
	private Vector2 boatToTarget;
    
	private SpriteBatch batch;
	private SpriteBatch staticBatch;
	private ShapeRenderer shaper;
	private ShapeRenderer shaper2;
	
	private BitmapFont font;
	
	private Texture boatImage;
	private Texture arrowImage;
	private Texture waterImage;
	private Texture islandImage;
	
	private Polygon boatBox;
	private Polygon targetBox;
	
	PolygonSprite poly;
	PolygonRegion polyReg;
	PolygonSpriteBatch polyBatch;
	
	// Demo-ralted variables
	private boolean moveCamera = false;
	public Vector2 initialCamPos;
	public boolean debug = false;
	
	// TODO delete
	float time = 0f;
	float width = 300f, height = 100f;
	boolean sclFac = false;
	
	public CameraMode cameraMode;
	public static enum CameraMode {
		targetDir, boatDir, dynamicDir, dynamicTarget, demoCam;
		
			public CameraMode next() {
				return values()[(this.ordinal() + 1) % values().length];
			}
			public CameraMode previous() {
				CameraMode[] vals = values();
				return vals[(this.ordinal() - 1 + vals.length) % vals.length];
			}
	}

	public GameRenderer2D(GameWorld2D world) {
		this.world = world;
		this.boat = world.getBoat();
		this.target = world.getTarget();
		this.boatBox = boat.getHitbox();
		this.targetBox = target.getHitbox();
		
		cameraMode = CameraMode.demoCam;
		initialCamPos = new Vector2(boat.getMid().cpy().add(0, 180f)); 
    	
    	camera = new OrthographicCamera();
    	camera.setToOrtho(false, 800, 600);
    	camera.position.set(boat.getMid().x, boat.getMid().y + Gdx.graphics.getHeight()/2 - 30, 0);
    	camera.up.set(boat.getDir(), 0);
    	cameraUpV2 = new Vector2(boat.getDir());
    	cameraDirV2 = boat.getDir().cpy();
    	boatToTarget = new Vector2();
    	boatToTarget = target.getPos().cpy().sub(boat.getMid()).cpy();
    	
    	//boatToCamDistance = boat.getMid().dst(camera.position.x, camera.position.y);
    	boatToCamDistance = 200f;
    	
    	Gdx.app.log("CamDir", ""+camera.direction);
    	
    	batch = new SpriteBatch();
    	batch.setProjectionMatrix(camera.combined);
    	staticBatch = new SpriteBatch();
    	staticBatch.setProjectionMatrix(camera.combined);
    	
    	shaper = new ShapeRenderer();
    	shaper.setProjectionMatrix(camera.combined);
    	shaper2 = new ShapeRenderer();
    	shaper2.setProjectionMatrix(camera.combined);
    	
    	font = new BitmapFont();
    	
    	initAssets();
	}
	
	public void render(float delta) {
        Gdx.gl.glClearColor(255, 255, 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        inputProcessing();
        
        switch(cameraMode) {
        case dynamicDir:
        default:
        	cameraDynamic();
        	if (debug) {
        		drawVec(boat.getMid(), boat.getMid().cpy().add(boat.getDir().setLength(1000f)), Color.RED);
        		Vector2 camUp = new Vector2(camera.up.x, camera.up.y);
        		drawVec(boat.getMid(), boat.getMid().add(camUp.cpy().setLength(1000f).rotate(30f)), Color.YELLOW);
        		drawVec(boat.getMid(), boat.getMid().add(camUp.cpy().setLength(1000f).rotate(-30f)), Color.YELLOW);
        		//drawCamUp(boat.getMid());
        	}
        	break;
        
        case boatDir:
        	cameraBoat();
        	if (debug) {
        		drawVec(boat.getMid(), boat.getMid().cpy().add(boat.getDir().setLength(1000f)), Color.RED);
        		//drawCamUp(boat.getMid());
        	}
        	break;
    	
        case targetDir:
        	cameraTarget();
        	if (debug) {
        		drawVec(boat.getMid(), boat.getMid().cpy().add(boat.getDir().cpy().setLength(1000f)), Color.RED);
        		drawVec(boat.getMid(), boat.getMid().add(boatToTarget.cpy().setLength(1000f)), Color.BLUE);
        		//drawCamUp(boat.getMid());
        	}
        	break;
        	
        case dynamicTarget:
        	cameraDynamicTarget();
        	if (debug) {
        		drawVec(boat.getMid(), boat.getMid().cpy().add(boat.getDir().setLength(1000f)), Color.RED);
        		drawVec(boat.getMid(), boat.getMid().add(boatToTarget.cpy().setLength(1000f)), Color.BLUE);
        		//drawCamUp(boat.getMid());
        	}
        	break;
        	
        case demoCam:
        	camera.position.set(boat.getMid().cpy().add(0, 180f), 0f);
        	if (debug)
        		drawVec(boat.getMid(), boat.getMid().cpy().add(boat.getDir().setLength(1000f)), Color.RED);
        	break;
        }
        camera.update();
        
        if (!moveCamera) {
        	camera.position.set(initialCamPos, 0f);
        	camera.up.set(0f, 1f, 0f);
        	camera.update();
        }

        batch.setProjectionMatrix(camera.combined);
        shaper.setProjectionMatrix(camera.combined);

		// Hintergrund
		shaper.begin(ShapeType.Line);
		shaper.setColor(0, 0, 0, 0);
		for (int i = 0; i < 10; ++i) {
			for (int j = 0; j < 10; ++j)
				shaper.rect(i * world.width/10, j * world.height/10,
						world.width/10, world.height/10);
		}
		// WorldRect
		shaper.setColor(Color.RED);
		shaper.rect(0, 0, world.width, world.height);
		shaper.end();
		
		// Water-Background
		/*batch.begin();
		for (int i = 0; i < 10; ++i) {
			for (int j = 0; j < 10; ++j)
				batch.draw(waterImage, i * 400, j * 300);
		}
		batch.end();*/

		// Insel
		shaper.begin(ShapeType.Line);
		shaper.setColor(Color.TEAL);
		shaper.polygon(targetBox.getTransformedVertices());
		shaper.end();
		
		// Inselkreis
		shaper.begin(ShapeType.Filled);
		shaper.setColor(Color.RED);
		shaper.circle(target.x, target.y, 5);
		shaper.end();

		// Boat
    	Vector2 arrowDir = (new Vector2(target.x, target.y)).sub(boat.getMid().cpy());
		batch.begin();
		batch.draw(islandImage, target.x - 200, target.y - 200);
		batch.draw(boatImage, boat.getPos().x, boat.getPos().y, boat.width/2, boat.height/2,
				boat.width, boat.height, 1, 1, boat.getRotation() - 90f,
				0, 0, (int)boat.width, (int)boat.height, false, false);
		batch.end();
		
		// Boat and Target hitbox
		shaper2.setProjectionMatrix(camera.combined);
		shaper2.begin(ShapeType.Line);
		shaper2.identity();
		shaper2.setColor(Color.RED);
		shaper2.polygon(boatBox.getTransformedVertices());
		shaper2.polygon(target.getHitbox().getTransformedVertices());
		shaper2.end();

		// BoarderRects
		float boarderWidth = 200;
		shaper.begin(ShapeType.Filled);
		shaper.setColor(Color.BLACK);
		shaper.rect(-boarderWidth, -boarderWidth, world.width+boarderWidth, boarderWidth-1);
		shaper.rect(-boarderWidth, 0, boarderWidth-1, world.height+boarderWidth);
		shaper.rect(0, world.height+1, world.width+boarderWidth, boarderWidth-1);
		shaper.rect(world.width+1, -boarderWidth, boarderWidth-1, world.height);
		shaper.end();
		
		// HUD
		staticBatch.begin();
		font.draw(staticBatch, "BoatPos: " + (int)boat.getPos().x + ", "+(int)boat.getPos().y, 5, 20);
		font.draw(staticBatch, "CameraPos: " + camera.position, 5, 40);
		font.draw(staticBatch, "BoatMid: " + boat.getMid(), 5, 60);
		font.draw(staticBatch, "CameraMode: " + (moveCamera ? cameraMode.name() : "noMove"), 5, 80);
		if (!camera.frustum.pointInFrustum(target.x, target.y, 0))
			staticBatch.draw(arrowImage, 400  - 16, 300 - 16, 16, 16, 32, 32, 1, 1,
				- arrowDir.angle(new Vector2(camera.up.x, camera.up.y)), 0, 0, 32, 32, false, false);
		// TODO REMOVE
		font.setColor(Color.RED);
		font.draw(staticBatch, "Angle: " + MathUtils.radiansToDegrees * time, 5, 100);
		staticBatch.end();
		
		//polyBatch.setTransformMatrix(camera.combined);
    	polyReg = new PolygonRegion(new TextureRegion(islandImage), new float[]{
    			-250, -250,
    			-250, 250,
    			250, 250,
    			250, -250}, new short[]{0, 1, 2, 0, 2, 3});
    	polyBatch = new PolygonSpriteBatch();
		polyBatch.begin();
		PolygonSprite polySprite = new PolygonSprite(polyReg);
		polySprite.setOrigin(400, 300);
		polySprite.setPosition(400, 300);
		//polySprite.scale(0.5f);
		//polySprite.draw(polyBatch);
		polyBatch.end();
		
		/* DELETE THIS TODO TODO TODO ALARM */
		/*Vector2 source = new Vector2(camera.position.x, camera.position.y),
				target = new Vector2(source.x + 730, source.y + 85);
		shaper.begin(ShapeType.Line);
		float width = 100, height = 200;
		shaper.ellipse(camera.position.x - width, camera.position.y - height, 2 * width, 2 * height);
		//shaper.circle(source.x, source.y, 200f);
		shaper.circle(target.x, target.y, 15);
		shaper.line(source, target);
		shaper.line(source, source.cpy().add(200f, 0));
		shaper.line(source, source.cpy().add(0f, 100f));
		double atan = MathUtils.random.nextFloat() * Math.PI * 2 - Math.PI;
		atan = (Math.atan2(target.y - source.y, target.x - source.x));
		shaper.setColor(Color.RED);
		//shaper.circle((float)(camera.position.x + (Math.cos(atan) * 200f)), (float)(camera.position.y + (Math.sin(atan) * 100f)), 10f);
		shaper.circle(
				source.x - (float)(width * height / (Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2) * Math.pow(Math.tan(atan), 2)))),
				source.y - (float)(width * height / (Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2) / Math.pow(Math.tan(atan), 2)))),
				10f
				);
		shaper.end();*/

	}
	
	private void testFPrime(Vector2 vec) {
		
	}
	
	private void testIntersect() {
		
	}
	
	private void inputProcessing() {
		if (Gdx.input.isKeyJustPressed(Keys.M)) {
			this.boat.moveForward = !boat.moveForward;
		}
		if (Gdx.input.isKeyJustPressed(Keys.C)) {
			this.moveCamera = ! moveCamera;
		}
		if (Gdx.input.isKeyJustPressed(Keys.D)) {
			this.debug = !debug;
		}
		if (Gdx.input.isKeyJustPressed(Keys.F1)) {
			this.cameraMode = cameraMode.next();
		}
		if (Gdx.input.isKeyJustPressed(Keys.F2)) {
			this.cameraMode = cameraMode.previous();
		}
	}
	
	private void drawVec(Vector2 source, Vector2 dest, Color col) {
		shaper.begin(ShapeType.Line);
		shaper.setColor(col != null ? col : Color.WHITE);
		shaper.line(source, dest);
		shaper.end();
	}
	
	private void drawCamUp(Vector2 source) {
		drawVec(source, source.add(new Vector2(camera.up.x, camera.up.y).setLength(1000f)), Color.BLUE);
	}
	
	private void cameraTarget() {
    	boatToTarget = target.getPos().cpy().sub(boat.getMid());
    	cameraDirV2 = boatToTarget.cpy();
    	camera.up.set(boatToTarget, 0);
        camera.position.set(boat.getMid().cpy().add(boatToTarget.setLength(boatToCamDistance)), 0);
	}
	
	// TODO Rework rotateFromTarget
	private void cameraDynamicTarget() {
    	boatToTarget = target.getPos().cpy().sub(boat.getMid());
		boolean boatLiesInside = liesInside(boatToTarget, boat.getDir(), 60);
		boolean rotateFromTarget = false;
		if (!camera.frustum.pointInFrustum(target.x, target.y, 0)) {
	    	if (boatLiesInside && isDynamicCamera) {
	    		cameraToVec(boatToTarget, 0.1f);
	    		if (liesInside(cameraUpV2, boatToTarget, 3))
	    			isDynamicCamera = false;
	    	} else if (boatLiesInside && !isDynamicCamera) {
				cameraTarget();
			}
	    	if (!boatLiesInside) {
				isDynamicCamera = true;
				cameraToVec(boat.getDir(), 30);
			}
		} else if (camera.frustum.pointInFrustum(target.x, target.y, 0)
				|| rotateFromTarget) {
			rotateFromTarget = false;
			if (liesInside(boatToTarget, boat.getDir(), 90)) {
	    		cameraToVec(boatToTarget, 0.1f);
	    		if (liesInside(cameraUpV2, boatToTarget, 3))
	    			cameraTarget();
			} else
				cameraToVec(boat.getDir(), 30);
			if (!camera.frustum.pointInFrustum(target.x, target.y, 0))
				rotateFromTarget = false;
		}
	}

	private void cameraBoat() {
		cameraDirV2.set(boat.getDir());
        camera.position.set(boat.getMid().cpy().add(cameraDirV2.setLength(boatToCamDistance)), 0);
		cameraUpV2.set(boat.getDir());
		camera.up.set(cameraUpV2, 0);
	}

	private void cameraDynamic() {
		if (!liesInside(boat.getDir(), cameraUpV2, 30))
			cameraToVec(boat.getDir(), 0.8f);
		else {
	        camera.position.set(boat.getMid().cpy().add(cameraDirV2.setLength(boatToCamDistance)), 0);
		}
	}
	
	private void cameraToVec(Vector2 vec, float degree) {
		cameraUpV2.set(camera.up.x,camera.up.y);
		float angle = vec.angle(cameraUpV2);
		if (!(angle < degree && angle > -degree)) {
			if (vec.angle(cameraUpV2) < 0)
				cameraDirV2.rotate(0.5f);
			else
				cameraDirV2.rotate(-0.5f);
		}
        camera.position.set(boat.getMid().cpy().add(cameraDirV2.setLength(boatToCamDistance)), 0);
		camera.up.set(cameraDirV2, 0);
		cameraUpV2.set(cameraDirV2);
	}
	
	public boolean liesInside(Vector2 vec1, Vector2 vec2, float maxDegree) {
		return vec1.angle(vec2) < maxDegree	&& vec1.angle(vec2) > -maxDegree;
	}
	
	private void initAssets() {
		arrowImage = AssetLoader.arrow;
	}

	public void dispose() {
		font.dispose();
		staticBatch.dispose();
		shaper.dispose();
		batch.dispose();
		shaper2.dispose();
	}
}
