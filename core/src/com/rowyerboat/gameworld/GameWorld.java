package com.rowyerboat.gameworld;

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
	
	public GameWorld(GameScreen screen) {
		map = Settings.map;
		width = map.width;
		height = map.height;
		gameScreen = screen;
		
		// Generate boat, locations and islands
		this.boat = new Boat(this, Settings.initialBoatPos);
		this.boat.setDir(Settings.initialBoatDir);
		this.boat.setScale(Settings.boatScale);
		
		this.locations = new Array<Location>();
		this.locations.addAll(map.islands);
		this.locations.addAll(Settings.mission.getLocations());

		this.target = Settings.mission.nextTarget();
		this.targetsHit = new Array<String>();
		
		Settings.tracker = new Tracker(boat);
		Gdx.app.log("GameWorld", "Succes creating random Boat and TargetIsland");
		Gdx.app.log("Boat", "" + Settings.initialBoatPos);
		Gdx.app.log("TargetIsland", "" + target.getPos());
		
		// Create the currents, one must be always null
		this.currentGrid = map.getCurrentGrid();
		gridDistance = map.gridDistance;
		
		worldRect = new Rectangle(0, 0, width, height);

		startRowing = false;
		
		paddleSplash = AssetLoader.paddleSplash;
	}

	public void update(float delta) {
		Vector2 currDirBow = new Vector2(0, 0),
				currDirStern = new Vector2(0, 0);

		currDirBow = calculateVecFromGrid(boat.bowPoint);
		currDirStern = calculateVecFromGrid(boat.sternPoint);
		
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
		Location nextT = Settings.mission.nextTarget();
		if (nextT == null)
			gameEnd(true);
		else {
			target = nextT;
			System.out.println("Target switched to: " + target.name);
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
		
		float x1, x2, xPos = point.x / gridDistance,
				ax = xPos - close_i,
				bx = close_i + 1 - xPos;
		x1 = ax * currentGrid[close_i + 1][close_j].x + bx * currentGrid[close_i][close_j].x;
		x2 = ax * currentGrid[close_i + 1][close_j + 1].x + bx * currentGrid[close_i][close_j + 1].x;
		
		float y1, y2, yPos = point.y / gridDistance,
				ay = yPos - close_j,
				by = close_j + 1 - yPos;
		y1 = ay * currentGrid[close_i][close_j + 1].y + by * currentGrid[close_i][close_j].y;
		y2 = ay * currentGrid[close_i + 1][close_j + 1].y + by * currentGrid[close_i + 1][close_j].y;
		xOffset = ay * x2 + by * x1;	
		yOffset = ax * y2 + bx * y1;
		
		Vector2 vec = new Vector2(xOffset, yOffset);
		
		return vec;
	}
	
	/**
	 * 
	 * @return  null if no intersection else the locations name
	 */
	private String intersectBoatAndIsland() {
		if (target.isConvex)
			if (Intersector.overlapConvexPolygons(boat.getHitbox(), target.getHitbox()))
				return target.name;
		for (int i = 0; i < locations.size; ++i) {
			if (Intersector.overlaps(locations.get(i).getHitbox().getBoundingRectangle(), boat.getHitbox().getBoundingRectangle())) {
				for (int j = 0; j < 3; j++) {
					Polygon islandPoly = locations.get(i).getHitbox(),
							boatPoly = boat.getHitbox();
					if (Intersector.isPointInPolygon(islandPoly.getVertices(), 0, islandPoly.getVertices().length,
							boatPoly.getTransformedVertices()[j], boatPoly.getTransformedVertices()[j+1]))
						return locations.get(i).name;
				}
			}
		}
		return null;
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
			//sound
		}
	}

	public void boatRightStop() {
		startRowing = true;
		if (!renderer.renderPaddle) {
			boat.stopRight();
			renderer.stopRight();
			//sound
		}
	}
	
	public void gameEnd(boolean isWin) {
		if (isWin)
			Gdx.app.log("Mission", "Accomplished.");
		else
			Gdx.app.log("Mission", "Failed.");
		Settings.tracker.isWin = isWin;
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
}
