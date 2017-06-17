package com.rowyerboat.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.rowyerboat.gameobjects.Island;
import com.rowyerboat.gameobjects.Location;

public class SVGModifier {
	private static String source;
	public static Array<Vector2> startPoints;
	public static Array<Location> islands;
	
	private static float svgX, svgY, xFactor, yFactor;
	
	public static void start(String fileName, int width, int height, float precision) throws Exception {
		FileHandle svgClean = Gdx.files.internal("SVGs/" + fileName + ".clean");
		FileHandle svg = Gdx.files.internal("SVGs/" + fileName);
		source = fileName;
		
		if (svgClean.exists()) {
			Gdx.app.log("SVGReader", "Clean file found");
			readFromFile(svgClean);
		} else if (svg.exists()) {
			transformSVG(svg.readString(), width, height, precision);
		} else
			throw new Exception("Neither clean nor normal *.svg found.");

		// Initiate some safe start points
		// heuristically
		startPoints = new Array<Vector2>();
		startPoints.addAll(
				new Vector2(280f * xFactor, (svgY - 570f) * yFactor),
				new Vector2(425f * xFactor, (svgY - 490f) * yFactor),
				new Vector2(465f * xFactor, (svgY - 410f) * yFactor),
				new Vector2(479f * xFactor, (svgY - 362f) * yFactor),
				new Vector2(457f * xFactor, (svgY - 290f) * yFactor),
				new Vector2(428f * xFactor, (svgY - 244f) * yFactor),
				new Vector2(567f * xFactor, (svgY - 449f) * yFactor),
				new Vector2(596f * xFactor, (svgY - 201f) * yFactor),
				new Vector2(308f * xFactor, (svgY - 160f) * yFactor),
				new Vector2(288f * xFactor, (svgY -  68f) * yFactor),
				new Vector2(204f * xFactor, (svgY -  91f) * yFactor),
				new Vector2(105f * xFactor, (svgY - 112f) * yFactor),
				new Vector2( 38f * xFactor, (svgY - 101f) * yFactor)
		);
		
	}

	public static void transformSVG(final String content, int width, int height, float precision) throws Exception {
		islands = new Array<Location>();
		String toFile = "";
		
		/** the width of the svg */
		float svgX = 0;
		/** the height of the svg */
		float svgY = 0;
		/** 26 islands have been implemented so far */
		int islandCount = 0;

		Pattern pathPattern = Pattern.compile(".*(M \\d+[^Z]*Z).*"),
				viewBoxPattern = Pattern.compile(".*viewBox=\"(.*)\".*"),
				xyPattern = Pattern.compile("\\D*([0-9]+\\.[0-9]*)\\D*");
		Matcher pathMatcher = pathPattern.matcher(content),
				viewBoxMatcher = viewBoxPattern.matcher(content),
				xyMatcher;

		float xFactor = 0;
		float yFactor = 0;

		// Calculate the scalar necessary to fit the world.width and world.height
		float xMin = 0, xMax = 0, yMin = 0, yMax = 0;
		if (viewBoxMatcher.find()) {
			svgX = Float.parseFloat(viewBoxMatcher.group(1).split(" ")[2]);
			svgY = Float.parseFloat(viewBoxMatcher.group(1).split(" ")[3]);
			xFactor = width / svgX;
			yFactor = height / svgY;
		}
		xMin = svgX * xFactor;
		yMin = svgY * yFactor;
		
		toFile += svgX + ";" + svgY + ";" + xFactor + ";" + yFactor + "\n";
		
		String path = "";
		while (pathMatcher.find()) {
			path += (pathMatcher.group(1))
					.replace("             ", "")
					.replace("           C ", "")
					.replace("M ", "")
					.replace(" Z", "")
					 + ";";
			++islandCount;
		}
		String[] paths = path.substring(0, path.length() - 1).split(";");
		Gdx.app.log("IslandCount", islandCount + " islands have been found.");
		
		// Build Islands
		Gdx.app.log("Islands", islandCount + " islands have been found. Create islands.");
		for (int i = 0; i < islandCount; ++i) {
			FloatArray verts = new FloatArray();
			float[] savingVals = new float[6];
			
			String name = "island" + (i < 10 ? "0" : "") + i;
			
			xyMatcher = xyPattern.matcher(paths[i]);
			xyMatcher.find();
			verts.add(Float.parseFloat(xyMatcher.group(1)) * xFactor);
			xyMatcher.find();
			verts.add((svgY - Float.parseFloat(xyMatcher.group(1))) * yFactor);
			
			Vector2 p0 = new Vector2(verts.get(0) / xFactor, verts.get(1) / yFactor); // p0 - Startpoint
			
			int index = 0;
			float xMin2 = xMin,
					xMax2 = xMax,
					yMin2 = yMin,
					yMax2 = yMax;
			while (xyMatcher.find()) {
				savingVals[index++] = Float.parseFloat(xyMatcher.group(1));
				// Quadratic-Beziercurve is read (x1y1, x2y2, x3y3)
				if (index == 6) {
					Vector2 p1 = new Vector2(savingVals[0], svgY - savingVals[1]), //p1 - Controllpoint1
							p2 = new Vector2(savingVals[2], svgY - savingVals[3]), //p2 - Controllpoint2
							p3 = new Vector2(savingVals[4], svgY - savingVals[5]); //p3 - Endpoint
					for (int j = 0; j <= precision; ++j) {
						Vector2 vec = Bezier.cubic(new Vector2(), j/precision, p0, p1, p2, p3, new Vector2());
						vec.x *= xFactor;
						vec.y *= yFactor;
						verts.add(vec.x);
						verts.add(vec.y);
						// Take mean of x and y as position
						xMin2 = xMin2 > vec.x ? vec.x : xMin2;
						xMax2 = xMax2 < vec.x ? vec.x : xMax2;
						yMin2 = yMin2 > vec.y ? vec.y : yMin2;
						yMax2 = yMax2 < vec.y ? vec.y : yMax2;
					}
					index = 0;
					savingVals = new float[6];
					p0 = p3.cpy();
				}
			}
			Vector2 pos = new Vector2((xMax2 + xMin2)/2, (yMax2 + yMin2)/2);
			
			float[] vertItems = new float[verts.size];
			for (int k = 0; k < verts.size; ++k)
				vertItems[k] = verts.get(k);
			
			// Write islanddata into file
			toFile += (name + ";" + verts.toString() + ";" + verts.size + ";" + pos.toString() + "\n");
			
			islands.add(new Island(name, vertItems, pos));
			// reset stuff
			verts = new FloatArray();
			xMin2 = xMin;
			xMax2 = xMax;
			yMin2 = yMin;
			yMax2 = yMax;
		}
		
		writeToFile(toFile);
	}
	
	/**
	 * read island-data from file. One island per line, each line consists of
	 * name;[x0, y0, ..., xn, yn];n*2 (size);(pos.x, pos.y)
	 */
	private static void readFromFile(FileHandle svgClean) throws Exception {
		islands = new Array<Location>();
		String content = svgClean.readString();
		String[] lines = content.split("\n");
		
		// svg dimensions
		String[] dimensions = lines[0].split(";");
		svgX = Float.valueOf(dimensions[0]);
		svgY = Float.valueOf(dimensions[1]);
		xFactor = Float.valueOf(dimensions[2]);
		yFactor = Float.valueOf(dimensions[3]);
		
		// process island
		for (int i = 1; i < lines.length; ++i) {
			String[] fields = lines[i].split(";");
			// process array
			String[] array = fields[1].substring(1, fields[1].length() - 2).split(", "); // omit first [ and trailing ], then split
			float[] verts = new float[Integer.parseInt(fields[2])];
			for (int j = 0; j < verts.length; ++j)
				verts[j] = Float.valueOf(array[j]);
			// process pos
			String[] posString = fields[3].substring(1, fields[3].length() - 2).split(","); // omit fist ( and trailing )
			Vector2 pos = new Vector2(Float.valueOf(posString[0]), Float.valueOf(posString[1]));
			islands.add(new Island(fields[0], verts, pos));
		}
	}
	
	private static void writeToFile(String toFile) throws Exception {
		Gdx.app.log("SVGReader", "Writing to file");
		FileHandle file = Gdx.files.local("SVGs/" + source + ".clean");
		file.writeString(toFile, false);
	}
}
