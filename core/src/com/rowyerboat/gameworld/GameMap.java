package com.rowyerboat.gameworld;

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.UBJsonReader;
import com.rowyerboat.gameobjects.Island;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.CSVModifier;
import com.rowyerboat.helper.SVGModifier;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;

public class GameMap {
	public MapID ID;

	public Array<Location> islands;
	public Array<ModelInstance> islandInstances;
	
	private Vector2[][] currentGrid;
	//private Array<Obstacle> obstacles;
	
	private Rectangle worldRect;
	public int width;
	public int height;
	public float gridDistance;
	
	private long seed;
	
	public Texture mapTex;
	
	public static enum MapID {
		lesserAntilles, caribbean, tutorial;
	}
	
	// Specific Map by ID
	public GameMap(long seed, MapID id) {
		this.seed = seed;
		MathUtils.random.setSeed(seed);
		islands = new Array<Location>();

		this.gridDistance = 100f;
		float vMin, vMax, uMin,	uMax;
		
		ID = id;
		
        Model islandModel = null;
        G3dModelLoader modelLoader = new G3dModelLoader(new UBJsonReader());
		
        islandInstances = new Array<ModelInstance>();
        
		switch(id){
		/*
		 * Lesser Antilles from 9.5�N to 19.1�N and 292� to 301.1�E
		 * 100 Pixels on-screen resemble approx 110m in real-world degree
		 */
		default:
		case lesserAntilles:
        	islandModel = modelLoader.loadModel(Gdx.files.getFileHandle("models/lesserAntillesDemo.g3db",
        			Files.FileType.Internal));
    	    islandInstances.add(new ModelInstance(islandModel, "all"));
    	    islandInstances.get(0).transform.setToRotation(1, 0, 0, 90f).scale(1f, 0.5f, 1f);
			Settings.boatScale = 0.1f;

			vMin = 9.5f;
			vMax = 19.1f;
			uMin = 292f;
			uMax = 301.1f;
			
			createGrid("05-13-2016_LA.csv", vMin, vMax, uMin, uMax);

			processSVG("LesserAntilles_all.svg");
			
			mapTex = AssetLoader.mapTex;
			break;

		case caribbean:
			Settings.boatScale = 0.1f;

			vMin = 5f;
			vMax = 27f;
			uMin = 274f;
			uMax = 305;
			
			createGrid("2016-05-13.csv", vMin, vMax, uMin, uMax);
			
			//processSVG("Caribbean.svg");
			mapTex = AssetLoader.mapTex;
			break;
			
			case tutorial:
				Settings.boatScale = 0.1f;
				
				float bx = 100, by = 500, bz = 10;
				ModelBuilder builder = new ModelBuilder();
				Model rect = builder.createBox(bx, by, bz,
						new Material(ColorAttribute.createDiffuse(Color.YELLOW)), Usage.Position | Usage.Normal);
				switch ((int)seed) {
				default:
				case 0:
					width = 1100;
					height = 1500;
					ModelInstance rectInst = new ModelInstance(rect);
					rectInst.transform.translate(550, 1150, 0).rotate(0, 0, 1, 90f).scale(1, 3f/5f, 1);
					islandInstances.add(rectInst);
					islands.add(new Island("obst0", new float[]{
							550 - by/2 * 3f/5f, 1150 - bx/2,
							550 - by/2 * 3f/5f, 1150 + bx/2,
							550 + by/2 * 3f/5f, 1150 + bx/2,
							550 + by/2 * 3f/5f, 1150 - bx/2,
					}, new Vector2(550, 1150)));
					
					rectInst = new ModelInstance(rect);
					rectInst.transform.translate(350, 750, 0);
					islandInstances.add(rectInst);
					islands.add(new Island("obst1", new float[] {
							350 - bx/2, 750 - by/2,
							350 - bx/2, 750 + by/2,
							350 + bx/2, 750 + by/2,
							350 + bx/2, 750 - by/2,
					}, new Vector2(350, 750)));
					
					rectInst = new ModelInstance(rect);
					rectInst.transform.translate(750, 750, 0);
					islandInstances.add(rectInst);
					islands.add(new Island("obst2", new float[] {
							750 - bx/2, 750 - by/2,
							750 - bx/2, 750 + by/2,
							750 + bx/2, 750 + by/2,
							750 + bx/2, 750 - by/2,
					}, new Vector2(750, 750)));
					mapTex = AssetLoader.mapTex_tut0;
					break;
				case 1:
					width = 1000;
					height = 1000;
					
					rectInst = new ModelInstance(rect);
					rectInst.transform.translate(400, 500, 0);
					islandInstances.add(rectInst);
					islands.add(new Island("obst0", new float[] {
							400 - bx/2, 500 - by/2,
							400 - bx/2, 500 + by/2,
							400 + bx/2, 500 + by/2,
							400 + bx/2, 500 - by/2,
					}, new Vector2(400, 500)));
					
					gridDistance = 50f;
					currentGrid = new Vector2[(int) (width/gridDistance)][(int) (height/gridDistance)];
					for (int i = 0; i < currentGrid.length; ++i)
						for (int j = 0; j < currentGrid[0].length; ++j)
							if (j > 5 && j <= 15)
								currentGrid[i][j] = new Vector2(-1f * (float)Math.sin((j/15.0) * Math.PI), 0);
							else
								currentGrid[i][j] = new Vector2(0, 0);
					
					mapTex = AssetLoader.mapTex_tut1;
					break;
				}
				break;
		}
	}

	private void processSVG(String path) {
		Gdx.app.log("Map", "Reading SVG");
		try {
			SVGModifier.start(path, width, height, 4f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		islands = SVGModifier.islands;
	}

	private void createGrid(String data, float vMin, float vMax, float uMin, float uMax) {
		Gdx.app.log("CurrentGrid", "Start creating");
		
		int widthNumber  = (int) ((uMax - uMin) * 10 + 1);
		int heightNumber = (int) ((vMax - vMin) * 10 + 1);
		
		this.width = (int) (widthNumber * gridDistance - gridDistance);
		this.height = (int) (heightNumber * gridDistance - gridDistance);
		Gdx.app.log("World Meassures", width + " x " + height);
		worldRect = new Rectangle(0, 0, width, height);
		
		currentGrid = new Vector2[widthNumber][heightNumber];
		
		data = "waterdata/" + data;
		FileHandle clean_data = Gdx.files.internal(data + ".clean");
		if (!clean_data.exists()) {
			try {
				CSVModifier csvmod = new CSVModifier(Gdx.files.internal(data));
				csvmod.cleanupCSV();
				clean_data = Gdx.files.internal(data + ".clean");
			}
			catch (Exception e) {
				System.out.println(e + "\nUsing backup data from 05-13-2016."); //TODO important!
				clean_data = Gdx.files.internal("waterdata/05-13-2016_LA.csv.clean");
			}
		} else
			Gdx.app.log("CurrentGrid", "Clean file found.");
		
		String[] csvData = clean_data.readString().split("\n");
		
		for (int k = 0; k < csvData.length; ++k) {
			String[] row = csvData[k].split(",");
			
			int i = (int)(Float.parseFloat(row[0]) * 10f - uMin * 10f),
					j = (int)(Float.parseFloat(row[1]) * 10f - vMin * 10f);
			Vector2 vec = new Vector2(Float.parseFloat(row[2]), Float.parseFloat(row[3]));
			

			if (currentGrid[i][j] != null)
				Gdx.app.log("CurrentGrid", "ERROR: Doubled vector");

			currentGrid[i][j] = vec;
		}
		Gdx.app.log("CurrentGrid", "Done creating");
	}
	
	public Vector2[][] getCurrentGrid() {
		return currentGrid;
	}
	
	public long getSeed() {
		return seed;
	}
}
