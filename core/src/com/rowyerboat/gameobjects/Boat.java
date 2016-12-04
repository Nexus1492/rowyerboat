package com.rowyerboat.gameobjects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.Settings;

/**
 * Boat class holding and storing all the information about the boat.
 * Offers utility functions to process current movement and energy usage.
 * 
 * @author Roman Lamsal
 *
 */
public class Boat {
	
	private GameWorld world;
	
	/** vector holding information about the direction of the boat
	 *  it's length must always be >0 */
	final private Vector2 direction;
	/** the direction of the movement due to currents,
	 * 	length reflects m/s */
	private Vector2 currentDirection;
	/** position of the boat's mid in the gameworld; functions as its position */
	private Vector2 midPoint;
	/** time elapsed since the last stroke */
	private float t;
	/** velocity */
	private float v;
	/** velocity at the time of the last stroke */
	private float v0;
	/** velocity at the end of the acceleration stage */
	private float v1;
	/** velocity added per stroke */
	private final float vn;
	/** length in seconds of a acceleration interval */
	private final float interval;
	private float rotation = 0;
	/** maxSpeed in m/s */
	private float maxSpeed;

	/** amount of energy left */
	private float energy = 100;
	/** scale for movement based on energy */
	private float energyFactor = 1;

	/** how many degrees the boat will rotate per second */
	private final float rotationPerTick = 45f;
	/** how many rotation ticks have already passed */
	private int rotationTicks = 0;
	/** how many rotation ticks can pass at max */
	private int maxRotationTicks;
	
	private boolean goingLeft = false;
	private boolean goingRight = false;
	public boolean stopping = false;
	
	private float scale = 0.5f * 0.9f;
	/** width of the boat in texels, DON'T CHANGE */
	public float width = 6 * scale;
	/** height of the boat in texels, DON'T CHANGE */
	public float height = 22 * scale;
	
	/** boat's hitbox as polygon */
	private Polygon hitbox;
	/** coordinates of the bow in the world */
	public Vector2 bowPoint;
	/** coordinates of the stern in the world */
	public Vector2 sternPoint;

	/** Speed of current in m/s for 0 = bow 1 = stern 2 = actual displacement */
	public float[] currSpeed;
	
	private float speedScale = Settings.speedScale;

	/** levels of energy depletion */
	float level0 = 0f, level1 = 0.5f, level2 = 0.85f; // if adjusted, adjust GameUI accordingly
	
	/**
	 * 
	 * @param world gameworld to set
	 * @param vec position
	 */
	public Boat(GameWorld world, Vector2 vec) {
		this(world, vec.x, vec.y);
	}

	/**
	 * 
	 * @param world gameworld to set
	 * @param x x-coordinate in the world
	 * @param y y-coordinate in the world
	 */
	public Boat(GameWorld world, float x, float y) {
		this.world = world;
		direction = new Vector2(0, 1);
		currentDirection = direction.cpy();
		midPoint = new Vector2(x, y);
		
		currSpeed = new float[3];
		
		bowPoint = new Vector2();
		sternPoint = new Vector2();
		
		// 6 km/h <=> 6 * 1000 / 3600
		maxSpeed = 6f * (1000f / 3600f);
		
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
		
		v  = 0;
		v0 = 0;
		vn = 1 * (1000f/3600f);
		interval = 0.75f;
	}
	
	/**
	 * Updates the boat's speed, position, direction and hitbox to reflect the changes made
	 * over time by currents and rows
	 * 
	 * @param delta time passed since last frame
	 */
	public void update(float delta) {
		t += delta;
		
		if (midPoint.x < 0)
			midPoint.x = 0;
		if (midPoint.x > world.width)
			midPoint.x = world.width;
		if (midPoint.y < 0)
			midPoint.y = 0;
		if (midPoint.y > world.height)
			midPoint.y = world.height;

		if (stopping) {
			float r = ((v1 / maxSpeed) * (maxSpeed / vn) * interval) / 2;
			v *= MathUtils.cos(((t - interval) / r) * (MathUtils.PI / 2f));;
		} else if (t <= interval * energyFactor && v1 != 0) {
			v = v0 + vn * MathUtils.sin((t / interval) * (MathUtils.PI / 2f));
		} else if (v1 != 0) {
			float r = (v1 / maxSpeed) * (maxSpeed / vn) * interval;
			v *= MathUtils.cos(((t - interval) / r) * (MathUtils.PI / 2f));
		}
		
		if (v > maxSpeed)
			v = maxSpeed;
		if (v <= 0) {
			v = 0;
			v1 = 0;
			stopping = false;
		} else {
			midPoint.add(direction.cpy().setLength(v * delta * speedScale));	
		}
		
		if (t > interval) {
			energy = Math.min(energy + 15 * delta, 100);
		}
		
		if (v > 0 && rotationTicks < maxRotationTicks) {
			rotationTicks++;
			float sign = goingLeft ? 1 : -1;
			if (stopping)
				direction.rotate(2 * sign * rotationPerTick * delta * (0.5f + 0.5f * (1.0f - v/maxSpeed)));
			else
				direction.rotate(sign * rotationPerTick * delta * (0.2f + 0.8f * (1.0f - v/maxSpeed)));
		}
		rotation = direction.angle();

		updateHitbox();
	}
	
	/**
	 * Moving the bow and stern seperately and derive the direction of the boat,
	 * the direction of displacement and the displaced position of the boat based
	 * on the currents.
	 * Refer to manual for more info.
	 * 
	 * @param bowDir The displacement-vector based off the boat's bow
	 * @param sternDir The displacement-vector based off the boat's stern
	 * @param delta Time passed between frames for scaling from per frame to per second
	 */
	public void moveBowAndStern(Vector2 bowDir, Vector2 sternDir, float delta) {
		currSpeed[0] = bowDir.len();
		currSpeed[1] = sternDir.len();
		if (currSpeed[0] > 0 && currSpeed[1] > 0) {
			Vector2 newBow = bowPoint.cpy().add(bowDir.scl(delta * Settings.speedScale));
			Vector2 newStern = sternPoint.cpy().add(sternDir.scl(delta * Settings.speedScale));
			Vector2 newDir = newBow.cpy().sub(newStern);
			Vector2 newMid = newStern.add(newDir.scl(0.5f));

			direction.set(newDir);
			rotation = direction.angle();

			currSpeed[2] = currentDirection.len() / delta / Settings.speedScale;
			currentDirection = newMid.cpy().sub(midPoint);
			midPoint.add(currentDirection);
		}
	}
	
	public void leftRow() {
		issuedRow();
		
		goingLeft = false;
		goingRight = true;
	}

	public void rightRow() {
		issuedRow();
		
		goingLeft = true;
		goingRight = false;
	}
	
	/**
	 * depletes energy based on speed, sets the boat in acceleration stage and randomly
	 * sets the maximum rotation ticks
	 */
	private void issuedRow() {
		float[] costs = new float[]{2, 5, 15};
		float cost = v/maxSpeed < level1 ? costs[0] : v/maxSpeed < level2 ? costs[1] : costs[2];
		if (Settings.useEnergy) {
			energyFactor = Math.min(energy/cost, 1);
			energy -= cost;
			energy = Math.max(energy, 0);
		}

		t = 0;
		v0 = v;
		v1 = Math.min(v0 + vn * energyFactor, maxSpeed);

		rotationTicks = 0;
		maxRotationTicks = 2 * (int)(100 * (1.0 + MathUtils.random.nextDouble() * 0.5));
	}
	
	public void stopLeft() {
		goingLeft = true;
		goingRight = false;
		
		stopping();
	}
	
	public void stopRight() {
		goingLeft = false;
		goingRight = true;
		
		stopping();
	}
	
	private void stopping() {
		t = interval;
		v1 = v;
		stopping = true;

		rotationTicks = 0;
		maxRotationTicks = 2 * (int)(100 * (1.0 + MathUtils.random.nextDouble() * 0.5));
	}
	
	public boolean goingLeft() {
		return goingLeft;
	}
	
	public boolean goingRight() {
		return goingRight;
	}
	
	public Vector2 getPos() {
		return midPoint.cpy();
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
	
	public Vector2 getCurrentDir() {
		return currentDirection.cpy();
	}
	
	public Vector2 getDirOverGround() {
		return direction.cpy().setLength(v).add(currentDirection.cpy().setLength(currSpeed[2]));
	}

	public Polygon getHitbox() {
		return hitbox;
	}

	public float getSpeed() {
		return v;
	}
	
	public float getRelativeSpeed() {
		return v/maxSpeed;
	}
	
	public float getGroundSpeed() {
		return 0;
	}

	public void setDir(Vector2 dirVec) {
		this.direction.set(dirVec);
		this.currentDirection = dirVec;
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
	
	/** updates the hitbox, bow and stern points based off displacement via movement and currents */
	private void updateHitbox() {
		hitbox.setRotation(rotation + 90f);
		hitbox.setPosition(midPoint.x, midPoint.y);
		bowPoint.set(hitbox.getTransformedVertices()[2], hitbox.getTransformedVertices()[3]);
		sternPoint.set(hitbox.getTransformedVertices()[8], hitbox.getTransformedVertices()[9]);
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public float getEnergy() {
		return energy;
	}
	
	public void resetBoat(Vector2 pos) {
		new Boat(world, pos);
	}
}
