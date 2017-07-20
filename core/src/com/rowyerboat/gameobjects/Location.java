package com.rowyerboat.gameobjects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;

public class Location {
	
	public String name;
	
	public ModelInstance modelInstance;
	
	protected Polygon hitbox;
	private Polygon[] hitboxTriangles; // TODO rework this and move it to the hitbox class
	
	protected Vector2 pos;

	protected float dangerRadius = 0;
	
	// TODO: rework to actually check the hitbox
	public boolean isConvex = true;
	
	protected Location() {
	}
	
	public Location(String name, Vector2 pos) {
		this(name, pos, 50);
	}
	
	public Location(String name, Array<Float> path) {
		this.name = name;
		float[] pathNew = new float[path.size];
		float minX = Float.MAX_VALUE, minY = minX, maxX = 0, maxY = 0;
		for (int i = 0; i < path.size; i += 2) {
			minX = Math.min(minX, path.get(i));
			maxX = Math.max(maxX, path.get(i));
			minY = Math.min(minX, path.get(i+1));
			maxY = Math.max(maxX, path.get(i+1));
			pathNew[i] = path.get(i);
			pathNew[i+1] = path.get(i+1);
		}
		this.hitbox = new Polygon(pathNew);
		EarClippingTriangulator tri = new EarClippingTriangulator();
		ShortArray triangles = tri.computeTriangles(pathNew);
		this.hitboxTriangles = new Polygon[triangles.size / 3];
		for (int i = 0; i < triangles.size; i += 3) {
			pathNew = hitbox.getTransformedVertices();
			hitboxTriangles[i/3] = new Polygon(new float[]{
					pathNew[triangles.get(i) * 2], pathNew[triangles.get(i) * 2 + 1],
					pathNew[triangles.get(i + 1) * 2], pathNew[triangles.get(i + 1) * 2 + 1],
					pathNew[triangles.get(i + 2) * 2], pathNew[triangles.get(i + 2) * 2 + 1]
			});
		}
		this.pos = new Vector2((minX + maxX)/2, (minY + maxY)/2);
		
		isConvex = false;
	}
	
	public Location(String name, Vector2 pos, float hitboxRadius) {
		this.name = name;
		this.pos = pos;
		this.hitbox = Hitbox.setCircleHitbox(pos, hitboxRadius, 32).getPoly(); //magic number
		this.isConvex = true;
	}
	
	public Vector2 getPos() {
		return pos.cpy();
	}
	
	public Polygon getHitboxPoly() {
		return hitbox;
	}
	
	public Polygon[] getTriangles() {
		if (hitboxTriangles != null)
			return hitboxTriangles;
		else
			return new Polygon[0];
	}
}
