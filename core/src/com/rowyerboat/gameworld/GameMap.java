package com.rowyerboat.gameworld;

import java.util.HashMap;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.UBJsonReader;
import com.rowyerboat.gameobjects.Island;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.CSVModifier;
import com.rowyerboat.helper.SVGHandler;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;

public class GameMap {
	private static HashMap<MapID, GameMap> maps = new HashMap<MapID, GameMap>();
	
	public MapID ID;

	public Array<Location> islands;
	public Array<ModelInstance> islandInstances;

	private static Array<Location> LA_islands;

	public Rectangle worldRect;
	public int width;
	public int height;

	public Texture mapTex;

	public static enum MapID {
		lesserAntilles, caribbean, tutorial0, tutorial1;
	}
	
	public static void init() {
		Gdx.app.log("Initialization", "GameMaps");
		for (MapID id : MapID.values())
			maps.put(id, new GameMap(id));
	}
	
	public static GameMap getMap(MapID id) {
		return maps.get(id);
	}

	// Specific Map by ID
	private GameMap(MapID id) {
		islands = new Array<Location>();

		float vMin, vMax, uMin, uMax;

		ID = id;

		Model islandModel = null;
		G3dModelLoader modelLoader = new G3dModelLoader(new UBJsonReader());

		islandInstances = new Array<ModelInstance>();

		switch (id) {
		/*
		 * Lesser Antilles from 9.5�N to 19.1�N and 292� to 301.1�E 100 Pixels
		 * on-screen resemble approx 110m in real-world degree
		 */
		default:
		case lesserAntilles:
			islandModel = modelLoader
					.loadModel(Gdx.files.getFileHandle("models/LesserAntilles.g3db", Files.FileType.Internal));
			islandInstances.add(new ModelInstance(islandModel));
			islandInstances.get(0).transform.setToTranslation(width / 2, height / 2, 0).setToRotation(1, 0, 0, 90f)
					.scale(1f, 0.5f, 1f);
			Settings.boatScale = 0.1f;

			islands = processJSon();
			// processSVG("LesserAntilles.svg");

			mapTex = AssetLoader.mapTex;
			break;

		case caribbean:
			Settings.boatScale = 0.1f;

			vMin = 5f;
			vMax = 27f;
			uMin = 274f;
			uMax = 305;

			// createGrid("2016-05-13.csv", vMin, vMax, uMin, uMax);

			// processSVG("Caribbean.svg");
			mapTex = AssetLoader.mapTex;
			break;

		case tutorial0:
			Settings.boatScale = 0.1f;

			float bx = 100, by = 500, bz = 10;
			Model rect = modelLoader.loadModel(Gdx.files.getFileHandle("models/tutorialBoxes.g3db",
						Files.FileType.Internal));

			width = 1100;
			height = 1500;
			ModelInstance rectInst = new ModelInstance(rect);
			rectInst.transform.translate(550, 1150, 0).rotate(0, 0, 1, 90f).rotate(1, 0, 0, 90f).scale(1, 1, 3f / 5f);
			islandInstances.add(rectInst);
			islands.add(new Island("obst0",
					new float[] { 550 - by / 2 * 3f / 5f, 1150 - bx / 2, 550 - by / 2 * 3f / 5f, 1150 + bx / 2,
							550 + by / 2 * 3f / 5f, 1150 + bx / 2, 550 + by / 2 * 3f / 5f, 1150 - bx / 2, },
					new Vector2(550, 1150)));

			rectInst = new ModelInstance(rect);
			rectInst.transform.translate(350, 750, 0).rotate(1, 0, 0, 90f);
			islandInstances.add(rectInst);
			islands.add(new Island("obst1", new float[] { 350 - bx / 2, 750 - by / 2, 350 - bx / 2, 750 + by / 2,
					350 + bx / 2, 750 + by / 2, 350 + bx / 2, 750 - by / 2, }, new Vector2(350, 750)));

			rectInst = new ModelInstance(rect);
			rectInst.transform.translate(750, 750, 0).rotate(1, 0, 0, 90f);
			islandInstances.add(rectInst);
			islands.add(new Island("obst2", new float[] { 750 - bx / 2, 750 - by / 2, 750 - bx / 2, 750 + by / 2,
					750 + bx / 2, 750 + by / 2, 750 + bx / 2, 750 - by / 2, }, new Vector2(750, 750)));
			mapTex = AssetLoader.mapTex_tut0;
			break;
		case tutorial1:
			Settings.boatScale = 0.1f;

			bx = 100;
			by = 500;
			bz = 10;
			rect = modelLoader.loadModel(Gdx.files.getFileHandle("models/tutorialBoxes.g3db",
						Files.FileType.Internal));
			
			width = 1000;
			height = 1000;

			rectInst = new ModelInstance(rect);
			rectInst.transform.translate(400, 500, 0).rotate(1, 0, 0, 90f);
			islandInstances.add(rectInst);
			islands.add(new Island("obst0", new float[] { 400 - bx / 2, 500 - by / 2, 400 - bx / 2, 500 + by / 2,
					400 + bx / 2, 500 + by / 2, 400 + bx / 2, 500 - by / 2, }, new Vector2(400, 500)));

			mapTex = AssetLoader.mapTex_tut1;

			break;
		}
	}

	@SuppressWarnings("unused")
	private void processSVG(String path) {
		try {
			SVGHandler.start(path, width, height, 4f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		islands = SVGHandler.islands;
	}

	private Array<Location> processJSon() {
		if (LA_islands != null)
			return LA_islands;

		LA_islands = new Array<Location>();

		String filename = "SVGs/blenderHitboxes.json";
		Json json = new Json();
		HashMap<String, Array<Float>> map = json.fromJson(HashMap.class, Gdx.files.internal(filename));
		for (String s : map.keySet())
			LA_islands.add(new Location(s, map.get(s)));

		return LA_islands;
	}
}
