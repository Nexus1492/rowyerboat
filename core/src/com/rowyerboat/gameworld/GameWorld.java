package com.rowyerboat.gameworld;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rowyerboat.gameobjects.Boat;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.rendering.GameRenderer;
import com.rowyerboat.scientific.Tracker;
import com.rowyerboat.screens.GameScreen;

public class GameWorld {

	private GameScreen gameScreen;
	private GameRenderer renderer;
	private int seed;

	private Boat boat;
	private Location target;
	private Array<Location> locations;
	private Vector2[][] currentGrid;
	private Rectangle worldRect;

	private GameMap map;
	public float gridDistance;
	public int width;
	public int height;

	private boolean startRowing;
	private Sound paddleSplash;

	private Array<String> targetsHit;

	private Vector2[] resetPosDir;

	public GameWorld(GameScreen screen) {
		map = Settings.map;
		width = map.width;
		height = map.height;
		gameScreen = screen;

		// Generate boat, locations and islands
		Mission mis = Settings.getMission();
		this.boat = new Boat(this, mis.initialBoatPos);
		this.boat.setDir(mis.initialBoatDir);
		this.boat.setScale(Settings.boatScale);
		this.resetPosDir = new Vector2[] { mis.initialBoatPos, mis.initialBoatDir };

		this.locations = new Array<Location>();
		this.locations.addAll(map.islands);
		this.locations.addAll(mis.getLocations());

		this.target = mis.nextTarget();
		this.targetsHit = new Array<String>();

		Settings.tracker.setBoat(boat);
		Gdx.app.log("Boat", "" + mis.initialBoatPos);
		Gdx.app.log("TargetIsland", "" + target.getPos());

		this.currentGrid = mis.getCurrentGrid();
		gridDistance = mis.gridDistance;

		worldRect = new Rectangle(0, 0, width, height);

		startRowing = false;

		paddleSplash = AssetLoader.paddleSplash;
	}

	public void update(float delta) {
		Vector2 currDirBow = new Vector2(0, 0), currDirStern = new Vector2(0, 0);

		if (currentGrid != null) {
			currDirBow = calculateVecFromGrid(boat.bowPoint);
			currDirStern = calculateVecFromGrid(boat.sternPoint);
		}

		if (startRowing) {
			Settings.tracker.update(delta);
			boat.update(delta);
			boat.moveBowAndStern(currDirBow, currDirStern, delta);
		}

		String intersectName = intersectBoatAndIsland();
		if (intersectName != null) {
			if (target.name.equals(intersectName)) {
				targetsHit.add(intersectName);
				checkForNextTarget();
			} else if (!targetsHit.contains(intersectName, false) && !intersectName.contains("target"))
				gameEnd(false);
		}
	}

	private void checkForNextTarget() {
		Location nextT = Settings.getMission().nextTarget();
		if (nextT == null) { // last target reached
			Settings.tracker.targetReached();
			gameEnd(true);
		} else { // only set up next target
			AssetLoader.fx_targetReached.play();
			target = nextT;
			resetPosDir = new Vector2[] { boat.getPos(), boat.getDir() };
			Gdx.app.log("Target switched to", target.name);
			Settings.tracker.targetReached();
		}
	}

	private Vector2 calculateVecFromGrid(Vector2 point) {
		int close_i = (int) (point.x / gridDistance);
		if (close_i < 0)
			close_i = 0;
		while (close_i >= currentGrid.length - 1)
			close_i--;
		int close_j = (int) (point.y / gridDistance);
		if (close_j < 0)
			close_j = 0;
		while (close_j >= currentGrid[0].length - 1)
			close_j--;

		float xOffset = 0, yOffset = 0;

		float x1, x2, xPos = point.x / gridDistance, ax = xPos - close_i, bx = close_i + 1 - xPos;
		x1 = ax * currentGrid[close_i + 1][close_j].x + bx * currentGrid[close_i][close_j].x;
		x2 = ax * currentGrid[close_i + 1][close_j + 1].x + bx * currentGrid[close_i][close_j + 1].x;

		float y1, y2, yPos = point.y / gridDistance, ay = yPos - close_j, by = close_j + 1 - yPos;
		y1 = ay * currentGrid[close_i][close_j + 1].y + by * currentGrid[close_i][close_j].y;
		y2 = ay * currentGrid[close_i + 1][close_j + 1].y + by * currentGrid[close_i + 1][close_j].y;
		xOffset = ay * x2 + by * x1;
		yOffset = ax * y2 + bx * y1;

		Vector2 vec = new Vector2(xOffset, yOffset);

		return vec;
	}

	/**
	 * 
	 * @return null if no intersection else the locations name
	 */
	private String intersectBoatAndIsland() {
		if (target.isConvex)
			if (Intersector.overlapConvexPolygons(boat.getHitbox(), target.getHitbox()))
				return target.name;
		for (int i = 0; i < locations.size; ++i) {
			if (Intersector.overlaps(locations.get(i).getHitbox().getBoundingRectangle(),
					boat.getHitbox().getBoundingRectangle())) {
				for (int j = 0; j < boat.getHitbox().getVertices().length / 2; j++) {
					Polygon boatPoly = boat.getHitbox();
					if (locations.get(i).isConvex) {
						Polygon islandPoly = locations.get(i).getHitbox();
						if (Intersector.isPointInPolygon(islandPoly.getTransformedVertices(), 0,
								islandPoly.getVertices().length, boatPoly.getTransformedVertices()[j * 2],
								boatPoly.getTransformedVertices()[j * 2 + 1]))
							return locations.get(i).name;
					} else {
						for (Polygon islandPoly : locations.get(i).getTriangles())
							if (Intersector.isPointInPolygon(islandPoly.getTransformedVertices(), 0,
									islandPoly.getVertices().length, boatPoly.getTransformedVertices()[j * 2],
									boatPoly.getTransformedVertices()[j * 2 + 1])) {
								return locations.get(i).name;
							}
					}

				}
			}
		}
		return null;
	}

	public void resetBoat() {
		this.boat = new Boat(this, resetPosDir[0]);
		this.boat.setDir(resetPosDir[1]);
		this.boat.setScale(Settings.boatScale);
		renderer.resetBoat(this.boat);
		Settings.tracker.setBoat(this.boat);
	}

	public void boatLeftSwing() {
		startRowing = true;
		if (!renderer.renderPaddle) {
			boat.leftRow();
			renderer.leftSwing();
			paddleSplash.play();
		}
	}

	public void boatRightSwing() {
		startRowing = true;
		if (!renderer.renderPaddle) {
			boat.rightRow();
			renderer.rightSwing();
			paddleSplash.play();
		}
	}

	public void boatLeftStop() {
		startRowing = true;
		if (!renderer.renderPaddle) {
			boat.stopLeft();
			renderer.stopLeft();
		}
	}

	public void boatRightStop() {
		startRowing = true;
		if (!renderer.renderPaddle) {
			boat.stopRight();
			renderer.stopRight();
		}
	}

	public void gameEnd(boolean isWin) {
		if (isWin) {
			AssetLoader.fx_missionAccomplished.play();
			Gdx.app.log("Mission", "Accomplished.");
		} else {
			Gdx.app.log("Mission", "Failed.");
		}
		gameScreen.end(isWin);
	}

	public Boat getBoat() {
		return boat;
	}

	public Location getTarget() {
		return target;
	}

	public Rectangle getRect() {
		return worldRect;
	}

	public void setRenderer(GameRenderer renderer) {
		this.renderer = renderer;
	}

	public void dispose() {

	}

	public void testShaper(ShapeRenderer shaper) {
		shaper.begin(ShapeType.Line);
		shaper.rect(200f, 200f, 200f, 200f);
		shaper.end();
	}

	public long getSeed() {
		return this.seed;
	}

	public Vector2[][] getCurrentGrid() {
		return currentGrid;
	}

	public Array<Location> getLocations() {
		return locations;
	}

	private boolean intersectPolys(Polygon p1, Polygon p2) {
		// Convert polygons into more practical format
		ArrayList<Vector2> p1Points = new ArrayList<Vector2>();
		ArrayList<Vector2> p2Points = new ArrayList<Vector2>();
		for (int i = 0; i < p1.getVertices().length; i += 2) {
			p1Points.add(new Vector2(p1.getVertices()[i], p1.getVertices()[i + 1]));
		}
		for (int i = 0; i < p2.getVertices().length; i += 2) {
			p2Points.add(new Vector2(p2.getVertices()[i], p2.getVertices()[i + 1]));
		}

		// Reusable point for determining the point of intersection between two
		// line segments
		Vector2 intersectionPoint;
		ArrayList<Vector2> outputList = p1Points;
		ArrayList<Vector2> inputList = new ArrayList<Vector2>();
		Vector2 edgePoint1;
		Vector2 edgePoint2;
		// Define the current points of the clip edge
		for (int i = 0; i < p2Points.size(); i++) {
			edgePoint1 = p2Points.get(i);
			// Wrap around to beginning of array if index points to the end
			edgePoint2 = i < p2Points.size() - 1 ? p2Points.get(i + 1) : p2Points.get(0);
			inputList.clear();
			// Add all elements to input list
			for (Vector2 p : outputList) {
				inputList.add(p);
			}
			outputList.clear();

			if (inputList.isEmpty()) {
				return false;
			}

			Vector2 s = inputList.get(inputList.size() - 1);

			for (Vector2 e : inputList) {
				Vector2 intersection = new Vector2();
				// determine if point is inside clip edge
				if (Intersector.pointLineSide(edgePoint2, edgePoint1, e) > 0) {
					if (!(Intersector.pointLineSide(edgePoint2, edgePoint1, s) > 0)) {
						Intersector.intersectLines(s, e, edgePoint1, edgePoint2, intersection);
						outputList.add(intersection);
					}
					outputList.add(e);
				} else if (Intersector.pointLineSide(edgePoint2, edgePoint1, s) > 0) {
					Intersector.intersectLines(s, e, edgePoint1, edgePoint2, intersection);
					outputList.add(intersection);
				}
				s = e;
			}
		}
		if (!outputList.isEmpty()) {
			float verts[] = new float[outputList.size() * 2];
			int i = 0;
			for (Vector2 vector2 : outputList) {
				verts[i] = vector2.x;
				verts[i + 1] = vector2.y;
				i += 2;
			}
			// overlap.setVertices(verts);
			return true;
		} else {
			// overlap.setVertices(new float[0]);
			return false;
		}
	}
}
