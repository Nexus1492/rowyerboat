package com.rowyerboat.gameobjects;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

public class Current {
	private Vector2 direction;
	private Polygon outline;
	private Array<Current> subCurrents;
	public float speed;
	protected boolean isRight;
	
	public static enum Alignment {
		top, right, bottom, left;
	}
	
	// Konstruktor für "Hauptströmung", beinhaltet keine Richtungen
	public Current(float[] vertices, float width, Alignment alignment, float speed) {
		subCurrents = new Array<Current>();
		this.speed = speed;
		FloatArray outlineLeft = new FloatArray();
		FloatArray outlineRight = new FloatArray();
		
		boolean[] rightOrientation = new boolean[vertices.length/2 - 1];
		Array<Polygon> outlines = new Array<Polygon>();
		
		Array<Vector2> vectors = new Array<Vector2>();
		
		Vector2 leftRightVec = new Vector2();
		// TODO direkt isRight
		switch (alignment) {
		case right:
			leftRightVec.set(-1, 0);
			break;
		case left:
			leftRightVec.set(1, 0);
			break;
		case top:
			leftRightVec.set(0, -1);
			break;
		case bottom:
			leftRightVec.set(0, 1);
			break;
		}
		
		// Instantiate the vectors and polygons
		Polygon outline;
		for (int i = 0; i < vertices.length/2 - 1; ++i) {
			int xNext = (i+1)*2;
			if (i > 0)
				leftRightVec = vectors.peek();
			Vector2 currVec = new Vector2(vertices[xNext] - vertices[xNext - 2], vertices[xNext + 1] - vertices[xNext - 1]);
			vectors.add(currVec);
			rightOrientation[i] = vectors.peek().angle(leftRightVec) > 0;
			
			if (rightOrientation[0])
				outline = new Polygon(generateRightRect(currVec.len(), width));
			else
				outline = new Polygon(generateLeftRect(currVec.len(), width));
			outline.rotate(currVec.angle() - 90f);
			outline.translate(vertices[xNext - 2], vertices[xNext - 1]);
			outlines.add(outline);
		}
		isRight = rightOrientation[0];
		if (isRight)
			outlineRight.items = vertices;
		else
			outlineLeft.items = vertices;
		
		// Fill gaps and create actual subCurrents
		for (int i = 0; i < vectors.size; ++i) {
			if (rightOrientation[i] != isRight) {
				if (isRight) {
					outline = new Polygon(new float[] {
							vertices[i*2], vertices[i*2 + 1],
							outlines.get(i-1).getTransformedVertices()[2], outlines.get(i-1).getTransformedVertices()[3],
							outlines.get(i).getTransformedVertices()[4], outlines.get(i).getTransformedVertices()[5]
					});
					subCurrents.add(new Current(outline, vectors.get(i-1).cpy().add(vectors.get(i))));
				} else {
					outline = new Polygon(new float[] {
							vertices[i*2], vertices[i*2 + 1],
							outlines.get(i-1).getTransformedVertices()[0], outlines.get(i-1).getTransformedVertices()[1],
							outlines.get(i).getTransformedVertices()[6], outlines.get(i).getTransformedVertices()[7]
					});
					subCurrents.add(new Current(outline, vectors.get(i-1).cpy().add(vectors.get(i))));
				}
			}
			subCurrents.add(new Current(outlines.get(i), vectors.get(i)));
		}
		
		if (isRight)
			outlineLeft.reverse();
		else
			outlineRight.reverse();
		outlineLeft.addAll(outlineRight);
		//this.outline = new Polygon(outlineLeft.items);
	}
	
	private Current(Polygon outline, Vector2 direction) {
		this.outline = outline;
		this.direction = direction.nor();
	}
	
	// Attach to left corner
	/*
	 * 	0,1 - 2,3
	 * 	|	   |
	 * 	6,7 - 4,5
	 */	
	private float[] generateRightRect(float height, float width) {
		return new float[]{
				0, height,
				width, height,
				width, 0,
				0, 0
		};
	}

	// Attach to right corner
	/*
	 * 	0,1 - 2,3
	 * 	|	   |
	 * 	6,7 - 4,5
	 */	
	private float[] generateLeftRect(float height, float width) {
		return new float[]{
			-width, height,
			0, height,
			0, 0,
			-width, 0
		};
	}
	
	public void draw(ShapeRenderer shaper) {
		shaper.begin(ShapeType.Line);
		shaper.setColor(Color.CYAN);
		for (int i = 0; i < subCurrents.size; ++i)
			shaper.polygon(subCurrents.get(i).getDrawableVertices());
		shaper.setColor(Color.RED);
		//shaper.polygon(outline.getTransformedVertices()); TODO
		shaper.end();
	}
	
	public float[] getDrawableVertices() {
		return outline.getTransformedVertices();
	}
	
	/** Returns the normalized direction of the current influencing the boat */
	public boolean getCurrentDirection(Vector2 boatPos, Vector2 currentDirection) {
		boolean touched = false;
		for (int i = 0; i < subCurrents.size; ++i) {
			if (subCurrents.get(i).outline.contains(boatPos)) {
				touched = true;
				currentDirection.add(subCurrents.get(i).direction).nor();
			}
		}
		return touched;
	}

	public boolean getCurrentDirection(Vector3 boatPos, Vector2 currentDirection) {
		return getCurrentDirection(new Vector2 (boatPos.x, boatPos.y), currentDirection);
	}
}
