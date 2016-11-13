package com.rowyerboat.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.Settings;

public class BoatOld {
	
	private GameWorld world;
	
	//private Vector2 position;
	private Vector2 direction;
	private Vector2 midPoint;
	
	private float rotation = 0;
	/** speed in m/s */
	private float speed = 0;
	/** maxSpeed in m/s */
	private float maxSpeed;

	/** how many degrees/s the boat will rotate after rotation is issued */
	private float rotationPerTick = 30f;
	private int rotationTicks = 0;
	private int maxRotationTicks;
	
	private boolean goingLeft = false;
	private boolean goingRight = false;
	
	private float scale = 0.5f * 0.9f;
	public float width = 6 * scale;
	public float height = 22 * scale;
	
	private Polygon hitbox;
	public Vector2 bowPoint;
	public Vector2 sternPoint;

	public float[] currSpeed;
	
	private float speedScale = Settings.speedScale;
	
	public BoatOld(GameWorld world, Vector2 vec) {
		this(world, vec.x, vec.y);
	}
	
	public BoatOld(GameWorld world, float x, float y) {
		this.world = world;
		direction = new Vector2(0, 1);
		midPoint = new Vector2(x, y);
		
		currSpeed = new float[2];
		
		bowPoint = new Vector2();
		sternPoint = new Vector2();
		
		// 6 km/h <=> 6 * 1000 / 3600 = 6 * 3.6
		maxSpeed = 6f * 3.6f * speedScale;
		
		
		// hitbox around the boat, clockwise; deprecated
		/*float[] betterbox = new float[] {
			0, height/2 - 7f,
			width/2 - 7f, height/6 + 22f,
			width/2 - 7f, -height/6 - 7f,
			0, -height/2 + 7f,
			-width/2 + 7f, -height/6 - 7f,
			-width/2 + 7f, height/6 + 22f
		};*/
		/*
		 * 5 4 3
		 * 0 1 2
		 */
		float[] betterbox = new float[] {
				-width, -height,
				0, -height,
				width, -height,
				width, height,
				0, height,
				-width, height
		};
		
		hitbox = new Polygon(betterbox);
		hitbox.setOrigin(0, 0);
		updateHitbox();
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
		
		if (speed > maxSpeed)
			speed = maxSpeed;

		midPoint.add(direction.cpy().setLength(speed * delta * speedScale));

		double speedThreshold = 0.001;

		if (speed > speedThreshold)
			speed *= 0.975;
		else
			speed = 0;
		
		if (goingLeft && speed > 0 && rotationTicks < maxRotationTicks) {
			direction.rotate(-rotationPerTick * delta * (1 - speed/maxSpeed));
			rotationTicks++;
		} else if (goingRight && speed > 0 && rotationTicks < maxRotationTicks) {
			direction.rotate(rotationPerTick  * delta * (1 - speed/maxSpeed));
			rotationTicks++;
		}
		
		rotation = direction.angle();
		
		updateHitbox();
	}
	
	public void moveBowAndStern(Vector2 bowDir, Vector2 sternDir) {
		currSpeed[0] = bowDir.len();
		currSpeed[1] = sternDir.len();
		if (bowDir.len2() > 0 || sternDir.len2() > 0) {
			Vector2 newBow = bowPoint.add(bowDir);
			Vector2 newStern = sternPoint.add(sternDir);
			Vector2 newMid = newBow.cpy().sub(newStern).scl(0.5f);
			midPoint.set(sternPoint.cpy().add(newMid));
			direction.set(newMid);
		}
	}
	
	public void leftRow() {
		speed += 3f * 3.6f * speedScale;
		rotationTicks = 0;
		maxRotationTicks = 2 * (int) (100 * (1.0 + MathUtils.random.nextDouble() * 0.5));
		goingLeft = true;
		goingRight = false;
	}
	
	public void rightRow() {
		speed += 3f * 3.6f * speedScale;
		rotationTicks = 0;
		maxRotationTicks = 2 * (int) (100 * (1.0 + MathUtils.random.nextDouble() * 0.5));
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
		return midPoint;
	}
	
	public Vector2 getMid() {
		return midPoint;
	}
	
	public float getRotation() {
		return rotation;
	}
	
	public Vector2 getDir() {
		return direction;
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

	public void setDir(Vector2 dirVec) {
		this.direction.set(dirVec);
		this.rotation = direction.angle();
		updateHitbox();
	}
	
	public float getScale() {
		return scale;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
		this.width = 6 * scale;
		this.height = 22 * scale;
		float[] betterbox = new float[] {
				-width, -height,
				0, -height,
				width, -height,
				width, height,
				0, height,
				-width, height
		};
		
		hitbox = new Polygon(betterbox);
		hitbox.setOrigin(0, 0);
		hitbox.setPosition(midPoint.x, midPoint.y);
		hitbox.setRotation(rotation + 90f);
		bowPoint.set(hitbox.getTransformedVertices()[2], hitbox.getTransformedVertices()[3]);
		sternPoint.set(hitbox.getTransformedVertices()[8], hitbox.getTransformedVertices()[9]);
	}
	
	public void setPos(Vector2 posVec) {
		midPoint = posVec;
		updateHitbox();
	}
	
	private void updateHitbox() {
		hitbox.setRotation(rotation + 90f);
		hitbox.setPosition(midPoint.x, midPoint.y);
		bowPoint.set(hitbox.getTransformedVertices()[2], hitbox.getTransformedVertices()[3]);
		sternPoint.set(hitbox.getTransformedVertices()[8], hitbox.getTransformedVertices()[9]);
	}
}
