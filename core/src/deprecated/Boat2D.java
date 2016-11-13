package deprecated;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.rowyerboat.gameworld.GameWorld;

public class Boat2D {
	
	private GameWorld2D world;
	
	private Vector2 position;
	private Vector2 midPoint;

	private Vector2 direction;
	private float rotation;
	private float speed;
	private float maxSpeed;
	
	private int rotationTicks;
	private int maxRotationTicks;
	private float rotationPerTick;
	
	private boolean goingLeft;
	private boolean goingRight;
	
	public float width = 37;
	public float height = 132;
	
	private Polygon hitbox;
	
	// Demo-related variables
	public boolean moveForward = false;
	
	public Boat2D(GameWorld2D world, Vector2 vec) {
		this(world, vec.x, vec.y);
	}
	
	public Boat2D(GameWorld2D world, float x, float y) {
		this.world = world;
		direction = new Vector2(0, 1);
		position = new Vector2(x, y);
		midPoint = new Vector2(position.x + width/2, position.y + height/2);
		
		rotation = 0;
		speed = 0;
		maxSpeed = 120;
		
		rotationPerTick = 2f;
		
		goingLeft = false;
		goingRight = false;
		
		
		// hitbox around the boat, clockwise; deprecated
		float[] betterbox = new float[] {
			0, height/2,
			width/2, height/2,
			width/2, -height/2,
			0, -height/2,
			-width/2, -height/2,
			-width/2, height/2
		};
		
		hitbox = new Polygon(betterbox);
		hitbox.setOrigin(0, 0);
		hitbox.setPosition(midPoint.x, midPoint.y);
	}
	
	public void update(float delta) {
		if (midPoint.x < 0)
			midPoint.x = 0;
		if (midPoint.x > world.width)
			midPoint.x = world.width;
		if (midPoint.y < 0)
			midPoint.y = 0;
		if (midPoint.y > world.height)
			midPoint.y = world.height;
		
		
		if (speed < 3) speed = 0;
		
		//direction.add(rowDirection);
		direction.nor();
		if (moveForward)
			position.add(direction.cpy().scl(speed).scl(delta));
		//midPoint.add(currentDirection);
		
		double speedThreshold = 1;
		
		if (speed > speedThreshold)
			speed *= 1 - speedThreshold/100;
		else if (speed > 0 && speed < speedThreshold)
			speed = 0;
		
		if (goingLeft && speed > 0 && rotationTicks < maxRotationTicks) {
			direction.rotate(-rotationPerTick * speed/maxSpeed);
			rotationTicks++;
		} else if (goingRight && speed > 0 && rotationTicks < maxRotationTicks) {
			direction.rotate(rotationPerTick * speed/maxSpeed);
			rotationTicks++;
		}

		midPoint.set(position.x + width/2, position.y + height/2);
		rotation = direction.angle();
		hitbox.setRotation(rotation + 90);
		hitbox.setPosition(midPoint.x, midPoint.y);
	}
	
	public void leftRow() {
		speed += 30;
		if (speed > maxSpeed) speed = 120;
		rotationTicks = 0;
		maxRotationTicks = 2 * (int) (100 * (1.0 + MathUtils.random.nextFloat() * 0.5));
		//Gdx.app.log("lRow", ""+maxRotationTicks);
		goingLeft = true;
		goingRight = false;
	}
	
	public void rightRow() {
		speed += 30;
		if (speed > maxSpeed) speed = 120;
		rotationTicks = 0;
		maxRotationTicks = 2 * (int) (100 * (1.0 + MathUtils.random.nextFloat() * 0.5));
		//Gdx.app.log("rRow", ""+maxRotationTicks);
		goingLeft = false;
		goingRight = true;
	}
	
	public boolean goingLeft(){
		return goingLeft;
	}
	
	public boolean goingRight(){
		return goingRight;
	}
	
	public Vector2 getPos() {
		return position.cpy();
	}
	
	public Vector2 getMid() {
		return midPoint.cpy();
	}
	
	public float getRotation() {
		return rotation;
	}
	
	public Vector2 getDir() {
		return direction.cpy();
	}

	public Polygon getHitbox() {
		return hitbox;
	}

	public float getSpeed() {
		return speed;
	}
	
	public float getRelSpeed() {
		return speed/maxSpeed;
	}
	
	public void setDir(Vector2 newDir) {
		this.direction.set(newDir);
	}
}
