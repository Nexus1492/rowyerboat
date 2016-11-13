package com.rowyerboat.gameobjects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class Location {
	
	public String name;
	
	public ModelInstance modelInstance;
	
	protected Polygon hitbox;
	
	protected Vector2 pos;
	
	protected float dangerRadius;
	
	// TODO: rework to actually check the hitbox
	public boolean isConvex = false;
	
	protected Location() {
	}
	
	public Location(String name, Vector2 pos) {
		this.name = name;
		this.pos = pos;
		this.dangerRadius = 0;
		this.setCircleHitbox(50f);
	}
	
	public Vector2 getPos() {
		return pos;
	}
	
	public Polygon getHitbox() {
		return hitbox;
	}
	
	public Location setCircleHitbox(float radius) {
		return setCircleHitbox(radius, 32); //magic number
	}
	
	public Location setCircleHitbox(float radius, int numPoints) {
		float[] vecs = new float[numPoints * 2];
		for (int i = 0; i < numPoints; ++i) {
			float factor = (float)(i+1) / (float)numPoints;
			vecs[i * 2] = pos.x + MathUtils.cosDeg(factor * 360f) * radius;
			vecs[i * 2 + 1] = pos.y + MathUtils.sinDeg(factor * 360f) * radius;
		}
		hitbox = new Polygon(vecs);
		isConvex = true;
		return this;
	}
}
