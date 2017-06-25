package com.rowyerboat.helper;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;

public class CSVModifier {
	FileHandle loc;
	
	public CSVModifier(FileHandle handle) {
		loc = handle;
	}
	
	public void cleanupCSV(float uMin, float uMax, float vMin, float vMax) {
		Vector2[][] grid = new Vector2[(int)((uMax - uMin) * 10) + 1][(int)((vMax - vMin) * 10) + 1];
		boolean[][] isSet = new boolean[grid.length][grid[0].length];
		String[] splits = loc.readString().split("\n");
		for (int i = 0; i < splits.length; ++i) {
			String[] parts = splits[i].split(",");
			
			int x = i % grid.length;
			int y = (int)(i / grid.length);
			
			float u, v;
			if (parts[4].equals("NaN"))
				u = 0;
			else
				u = Float.parseFloat(parts[4]);
			
			if (parts[5].equals("NaN"))
				v = 0;
			else
				v = Float.parseFloat(parts[5]);
			
			if (grid[x][y] != null)
				Gdx.app.log("CurrentGrid", "ERROR: Doubled vector: " + x + ", "+ y);
			
			grid[x][y] = new Vector2(u, v);
			isSet[x][y] = true;
			
			if (i % (int)(splits.length/10.0) == 0)
				System.out.print(".");
		}
		System.out.println("!");
		for (int i = 0; i < isSet.length; ++i)
			for (int j = 0; j < isSet[0].length; ++j)
				if (!isSet[i][j])
					System.out.println("ERROR: " + i + ", " + j);
		
		FileHandle write = Gdx.files.local(loc.path() + ".clean");
		StringBuilder builder = new StringBuilder();
		builder.append(grid.length + "," + grid[0].length + "\n");
		for (int i = 0; i < grid.length; ++i)
			for (int j = 0; j < grid[0].length; ++j) {
				builder.append(grid[i][j].x)
					.append(",")
					.append(grid[i][j].y)
					.append("\n");
			}
		write.writeString(builder.toString(), false);
	}
	
	public void cleanupCSVtoJson() {
		Vector2[][] grid = new Vector2[(int)((301.1 - 292) * 10) + 1][(int)((19.1 - 9.5) * 10) + 1];
		boolean[][] isSet = new boolean[grid.length][grid[0].length];
		String[] splits = loc.readString().split("\n");
		for (int i = 0; i < splits.length; ++i) {
			String[] parts = splits[i].split(",");
			
			int x = i % grid.length;
			int y = (int)(i / grid.length);
			
			float u, v;
			if (parts[4].equals("NaN"))
				u = 0;
			else
				u = Float.parseFloat(parts[4]);
			
			if (parts[5].equals("NaN"))
				v = 0;
			else
				v = Float.parseFloat(parts[5]);
			
			if (grid[x][y] != null)
				Gdx.app.log("CurrentGrid", "ERROR: Doubled vector: " + x + ", "+ y);
			
			grid[x][y] = new Vector2(u, v);
			isSet[x][y] = true;
			
			if (i % (int)(splits.length/10.0) == 0)
				System.out.print(".");
		}
		for (int i = 0; i < isSet.length; ++i)
			for (int j = 0; j < isSet[0].length; ++j)
				if (!isSet[i][j])
					System.out.println("ERROR: " + i + ", " + j);
		System.out.println("!");
		FileHandle write = Gdx.files.local(loc.path() + ".clean");
		Json json = new Json();
		json.toJson(grid, write);
	}
	
	@Deprecated
	public boolean cleanupCSVDeprec() {
		String[] splits = loc.readString().split("\n");
		StringBuilder newStr = new StringBuilder();
		for (int i = 0; i < splits.length; ++i) {
			String[] parts = splits[i].split(",");
			newStr.append(String.format(Locale.US, "%.1f", Float.parseFloat(parts[3])));
			newStr.append(",");

			newStr.append(String.format(Locale.US, "%.1f", Float.parseFloat(parts[2])));
			newStr.append(",");
			
			if (parts[4].equals("NaN"))
				newStr.append("0");
			else
				newStr.append(parts[4]);
			newStr.append(",");
			
			if (parts[5].equals("NaN"))
				newStr.append("0");
			else
				newStr.append(parts[5]);
			newStr.append("\n");
			
			if (i % 50 == 0)
				System.out.print("\r" + "Progress: " + 
						String.format(Locale.US, "%.2f", i / (float)splits.length * 100f));
		}
		System.out.println("\n");
		FileHandle write = Gdx.files.local(loc.path() + ".clean");
		write.writeString(newStr.toString(), false);
		
		return false;
	}
}
