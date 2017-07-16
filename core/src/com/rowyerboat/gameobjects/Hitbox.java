package com.rowyerboat.gameobjects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Hitbox {
	Array<Vector2> points;
	Polygon poly;
	
	float radius;
	
	HitboxType type;
	
	public enum HitboxType {
		CIRCULAR, CONCAVE, CONVEX;
	}
	
	public Hitbox(float[] path) {
		this(new Polygon(path));
	}
	
	public Hitbox(Polygon poly) {
		this.poly = poly;
		this.points = new Array<Vector2>();
		for (int i = 0; i < poly.getVertices().length; i+=2)
			this.points.add(new Vector2(poly.getTransformedVertices()[i],
					poly.getTransformedVertices()[i+1]));
	}
	
	public Polygon getPoly() {
		return poly;
	}
	
	public static Hitbox setCircleHitbox(Vector2 pos, float radius, int numPoints) {
		float[] vecs = new float[numPoints * 2];
		for (int i = 0; i < numPoints; ++i) {
			float factor = (float)(i+1) / (float)numPoints;
			vecs[i * 2] = pos.x + MathUtils.cosDeg(factor * 360f) * radius;
			vecs[i * 2 + 1] = pos.y + MathUtils.sinDeg(factor * 360f) * radius;
		}
		Hitbox box = new Hitbox(vecs);
		box.type = HitboxType.CIRCULAR;
		box.radius = radius;
		return box;
	}
}
