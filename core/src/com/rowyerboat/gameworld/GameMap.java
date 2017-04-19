package com.rowyerboat.gameworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.helper.CSVModifier;
import com.rowyerboat.helper.SVGModifier;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;

public class GameMap {
	public MapID ID;

	public Array<Location> islands;
	
	private Vector2[][] currentGrid;
	//private Array<Obstacle> obstacles;
	
	private Rectangle worldRect;
	public int width;
	public int height;
	public float gridDistance;
	
	private long seed;
	
	public static enum MapID {
		lesserAntilles, caribbean;
	}
	
	// Specific Map by ID
	public GameMap(long seed, MapID id) {
		this.seed = seed;
		MathUtils.random.setSeed(seed);
		islands = new Array<Location>();

		this.gridDistance = 100f;
		float vMin, vMax, uMin,	uMax;
		
		ID = id;
		
		switch(id){
		/*
		 * Lesser Antilles from 9.5�N to 19.1�N and 292� to 301.1�E
		 * 100 Pixels on-screen resemble approx 110m in real-world degree
		 */
		default:
		case lesserAntilles:
			Settings.boatScale = 0.1f;

			vMin = 9.5f;
			vMax = 19.1f;
			uMin = 292f;
			uMax = 301.1f;
			
			createGrid("05-13-2016_LA.csv", vMin, vMax, uMin, uMax);

			processSVG("LesserAntilles_all.svg");
			break;

		case caribbean:
			Settings.boatScale = 0.1f;

			vMin = 5f;
			vMax = 27f;
			uMin = 274f;
			uMax = 305;
			
			createGrid("2016-05-13.csv", vMin, vMax, uMin, uMax);
			
			//processSVG("Caribbean.svg");
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
		int widthNumber  = (int) ((uMax - uMin) * 10 + 1);
		int heightNumber = (int) ((vMax - vMin) * 10 + 1);
		
		this.width = (int) (widthNumber * gridDistance - gridDistance);
		this.height = (int) (heightNumber * gridDistance - gridDistance);
		Gdx.app.log("World Meassures", width + " x " + height);
		worldRect = new Rectangle(0, 0, width, height);
		
		currentGrid = new Vector2[widthNumber][heightNumber];
		
		data = "waterdata/" + data;
		FileHandle clean_data = Gdx.files.local(data + ".clean");
		if (!clean_data.exists()) {
			try {
				CSVModifier csvmod = new CSVModifier(Gdx.files.internal(data));
				csvmod.cleanupCSV();
				clean_data = Gdx.files.local(data + ".clean");
			}
			catch (Exception e) {
				System.out.println(e + "\nUsing backup data from 05-13-2016."); //TODO important!
				clean_data = Gdx.files.internal("waterdata/05-13-2016_LA.csv.clean");
			}
		}

		Gdx.app.log("CurrentGrid", "Start creating");
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
