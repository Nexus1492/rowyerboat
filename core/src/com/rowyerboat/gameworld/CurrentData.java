package com.rowyerboat.gameworld;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.helper.CSVModifier;

public class CurrentData {
	public final static float gridDistance = 100f;
	
	private static LinkedHashMap<String, Vector2[][]> currentGrids;
	
	public static void init() {
		currentGrids = new LinkedHashMap<String, Vector2[][]>();
		float vMinLA = 9.5f,
			vMaxLA = 19.1f,
			uMinLA = 292f,
			uMaxLA = 301.1f;
		createGrid("2016-05-13_LA.csv", vMinLA, vMaxLA, uMinLA, uMaxLA); // not random
		// now the randoms
		createGrid("2016-03-15_LA.csv", vMinLA, vMaxLA, uMinLA, uMaxLA);
		createGrid("2016-07-15_LA.csv", vMinLA, vMaxLA, uMinLA, uMaxLA);
		createGrid("2016-09-01_LA.csv", vMinLA, vMaxLA, uMinLA, uMaxLA);
		createGrid("2016-11-15_LA.csv", vMinLA, vMaxLA, uMinLA, uMaxLA);
	}
	
	public static Vector2[][] getGrid(Mission mis, String data) {
		Vector2[][] grid = currentGrids.get(data);
		GameMap map = mis.map;
		map.width = (int) ((grid.length - 1) * gridDistance);
		map.height = (int) ((grid[0].length - 1) * gridDistance);
		map.worldRect = new Rectangle(0, 0, map.width, map.height);
		mis.gridDistance = gridDistance;
		mis.currentDate = data;
		return grid;
	}
	
	public static Vector2[][] getRandomGrid(Mission mis) {
		MathUtils.random.setSeed(TimeUtils.millis());
		int randomNum = (int) (MathUtils.random(3) + 1); // 4 random values stored in currentGrids[1] to currentGrids[4]
		String data = (String) currentGrids.keySet().toArray()[randomNum];
		Gdx.app.log("Random Currents", data);
		return getGrid(mis, data);
	}

	private static void createGrid(String data, float vMin, float vMax, float uMin, float uMax) {
		Gdx.app.log("CurrentGrid", "Start creating");
		
		String fullPath = "waterdata/" + data;
		FileHandle clean_data = Gdx.files.internal(fullPath + ".clean");
		if (!clean_data.exists()) {
			try {
				CSVModifier csvmod = new CSVModifier(Gdx.files.internal(fullPath));
				csvmod.cleanupCSV(uMin, uMax, vMin, vMax);
				clean_data = Gdx.files.local(fullPath + ".clean");
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println(e + "\nUsing backup data from 2016-05-13."); //TODO important!
				clean_data = Gdx.files.internal("waterdata/2016-05-13_LA.csv.clean");
			}
		} else
			Gdx.app.log("CurrentGrid", "Clean file found.");
		
		String[] csvData = clean_data.readString().split("\n");
		Vector2[][] currentGrid = new Vector2[Integer.valueOf(csvData[0].split(",")[0])]
				[Integer.valueOf(csvData[0].split(",")[1])];
		for (int k = 1; k < csvData.length; ++k) {
			String[] row = csvData[k].split(",");
			
			int i = (k - 1) % currentGrid.length;
			int j = (k - 1) / currentGrid.length;
			currentGrid[i][j] = new Vector2(Float.valueOf(row[0]), Float.valueOf(row[1]));
		}
		currentGrids.put(data, currentGrid);
		Gdx.app.log("CurrentGrid " + data, "Done creating");
	}

	

}
