package com.rowyerboat.gameworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.helper.CSVModifier;
import com.rowyerboat.helper.SVGReader;
import com.rowyerboat.helper.Settings;

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
		lesserAntilles;
	}
	
	// Specific Map by ID
	public GameMap(long seed, MapID id) {
		this.seed = seed;
		MathUtils.random.setSeed(seed);
		islands = new Array<Location>();

		this.gridDistance = 100f;
		float vMin = 9.5f,
				vMax = 19.1f,
				uMin = 292f,
				uMax = 301.1f;
		
		switch(id){
		/*
		 * Lesser Antilles from 9.5°N to 19°N and 292° to 301°E
		 * 100 Pixels on-screen resemble approx 110m in real-world°
		 */
		default:
		case lesserAntilles:
			Settings.boatScale = 0.1f;
			
			int widthNumber  = (int) ((uMax - uMin) * 10 + 1);
			int heightNumber = (int) ((vMax - vMin) * 10 + 1);
			
			this.width = (int) (widthNumber * gridDistance - gridDistance);
			this.height = (int) (heightNumber * gridDistance - gridDistance);
			Gdx.app.log("World Meassures", width + " x " + height);
			worldRect = new Rectangle(0, 0, width, height);
			
			currentGrid = new Vector2[widthNumber][heightNumber];
			
			String data = "waterdata/05-13-2016_LA.csv";
			FileHandle clean_data = Gdx.files.local(data + ".clean");
			if (!clean_data.exists()) {
				try {
					CSVModifier csvmod = new CSVModifier(Gdx.files.local(data));
					csvmod.cleanupCSV();
					clean_data = Gdx.files.local(data + ".clean");
				}
				catch (Exception e) {
					System.out.println(e + "\nUsing backup data from 05-13-2016.");
					clean_data = Gdx.files.internal("05-13-2016_LA.csv.clean");
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

			Gdx.app.log("Map", "Reading SVG");
			try {
				SVGReader.start("LesserAntilles_all.svg", width, height, 25f);
			} catch (Exception e) {
				e.printStackTrace();
			}
			islands = SVGReader.islands;
			
			break;
		}
	}
	
	public Vector2[][] getCurrentGrid() {
		return currentGrid;
	}
	
	public long getSeed() {
		return seed;
	}
}
