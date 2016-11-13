package com.rowyerboat.gameworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rowyerboat.gameobjects.Current;
import com.rowyerboat.gameobjects.Island;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.gameobjects.Current.Alignment;
import com.rowyerboat.helper.CSVModifier;
import com.rowyerboat.helper.SVGReader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Transverter;

public class GameMap {
	public MapID ID;
	
	private Vector2 windDir;

	public Array<Location> islands;
	
	private Array<Current> currents;
	private Vector2[][] currentGrid;
	//private Array<Obstacle> obstacles;
	
	private Rectangle worldRect;
	public int width;
	public int height;
	public float gridDistance;
	
	private long seed;
	
	public static enum MapID {
		lesserAntilles, Random, withCurrent, testCase;
	}
	
	// Specific Map by ID
	public GameMap(long seed, MapID id) {
		this.seed = seed;
		MathUtils.random.setSeed(seed);
		islands = new Array<Location>();
		float[] vertices;
		
		Vector2 targetPos = new Vector2();

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
		
		case Random:
			randomGameMap(4000, 3000);
			islands.get(0).isConvex = true;
			break;
		
		case withCurrent:
			this.width = 2000;
			this.height = 1500;
			worldRect = new Rectangle(0, 0, width, height);
			targetPos = new Vector2(width - 200f, height - 200f);
			Settings.initialBoatPos.set(100f, 100f);
			Settings.initialBoatDir.set(targetPos.cpy().sub(Settings.initialBoatPos));
			currents = new Array<Current>();
			vertices = new float[]{
				2000, 800,
				1500, 900,
				1200, 1000,
				1000, 1200,
				900, 1500
			};
			currents.add(new Current(vertices, 200f, Alignment.right, 1.30f));
			islands.add(new Island(targetPos, 160));
			islands.get(0).isConvex = true;
			break;
			
		case testCase:
			this.width = 2000;
			this.height = 1500;
			worldRect = new Rectangle(0, 0, width, height);
			targetPos = new Vector2(width - 200f, height - 200f);
			Settings.initialBoatPos.set(100f, 100f);
			Settings.initialBoatDir.set(targetPos.cpy().sub(Settings.initialBoatPos));
			currents = new Array<Current>();
			vertices = new float[]{
				2000, 800,
				1500, 900,
				1200, 1000,
				1000, 1200,
				900, 1500
			};
			currents.add(new Current(vertices, 400f, Alignment.right, 0.80f));
			vertices = new float[]{
				1000, 0,
				800, 200,
				500, 300,
				400, 500,
				500, 700,
				700, 700,
				800, 1000,
				600, 1500
			};
			currents.add(new Current(vertices, 400f, Alignment.bottom, 0.70f));
			vertices = new float[]{
					2000, 200,
					1700, 500,
					1200, 800,
					800, 1000
			};
			currents.add(new Current(vertices, 300f, Alignment.bottom, 2f));
			islands.add(new Island(targetPos, 160));
			islands.get(0).isConvex = true;
			break;
		}
	}
	
	// RandomMap + randomTargetPos generator
	public void randomGameMap(int width, int height) {
		this.width = width;
		this.height = height;
		worldRect = new Rectangle(0, 0, width, height);
		Settings.initialBoatPos.set(MathUtils.random.nextFloat() * width, MathUtils.random.nextFloat()*height);
		islands.add(new Island(randomTargetPos(MathUtils.random.nextFloat() * 2100 + 1500), 160));
		currents = new Array<Current>();
		windDir = new Vector2(MathUtils.random.nextFloat() - 0.5f,
				MathUtils.random.nextFloat() - 0.5f);
		windDir.nor();
	}
	
	// vec := targetPos
	private Vector2 randomTargetPos(float dist) {
		Vector2 vec = new Vector2();
		Vector2 dirVec = new Vector2();
		boolean succes = false;
		for (int i = 0; i < 100 && !succes; ++i){
			dirVec.set(MathUtils.random.nextFloat()*2 - 1,
				MathUtils.random.nextFloat()*2 - 1);
			vec = dirVec.cpy().setLength(dist).add(Settings.initialBoatPos);
			if (worldRect.contains(vec) && vec.dst(Settings.initialBoatPos) > 1500) succes = true;
			
			if (!succes && i == 99) {
				Settings.initialBoatPos = new Vector2(MathUtils.random.nextFloat() * width, MathUtils.random.nextFloat()*height);
				i = 0;
			}
		}
		Settings.initialBoatDir = new Vector2(dirVec);
		return vec;
	}
	
	public Array<Current> getCurrents() {
		return currents;
	}
	
	public Vector2[][] getCurrentGrid() {
		return currentGrid;
	}
	
	public long getSeed() {
		return seed;
	}
}
