package com.rowyerboat.scientific;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rowyerboat.gameworld.GameMap.MapID;
import com.rowyerboat.helper.Settings;

/** 
 * Class to calculate various units to other relevant units.
 * 
 * @author Roman Lamsal
 *
 */
public abstract class Transverter {
	/** texture width in pixels FIXED */
	static float texWidth = 599f;//640f;
	/** texture height in pixels FIXED */
	static float texHeight = 632f;//674f;
	/** world width in pixels FIXED */
	static float worldWidth = 9100f;
	/** world height in pixels FIXED */
	static float worldHeight = 9600f;
	
	/** latitude in real-world coordinates */
	static float minLon = 292, maxLon = 301f;
	/** longitude in real-world coordinates */
	static float minLat = 9.5f, maxLat = 19f;

	static TreeMap<Float, Float> ingameValues = new TreeMap<Float, Float>();
	
	public static Vector2 GPStoGame(Vector2 vec) {
		// case: long0 = zeromeridian
		Vector2 gpsVec = new Vector2((vec.y + 360 - minLon)/(maxLon - minLon),
				(vec.x - minLat)/(maxLat - minLat)); // unscaled vector with x, y \in [0, 1]
		
		return new Vector2(gpsVec.x * worldWidth, gpsVec.y * worldHeight);
	}
	
	public static Vector2 gameToGPS(Vector2 vec) {
		return new Vector2(gameToLat(vec.y), gameToLong(vec.x));
	}
	
	public static float gameToLong(float gameX) {
		return (gameX)/worldWidth * (maxLon - minLon) + minLon - 360;
	}
	
	public static float gameToLat(float gameY) {
		return (gameY)/worldHeight * (maxLat - minLat) + minLat;
	}
	
	/** calculate game coordinates from pixel coordinates of the texture */
	public static Vector2 textureToGame(Vector2 vec, boolean yDown) {
		Vector2 v = new Vector2();
		v.x = vec.x / texWidth * worldWidth;
		v.y = vec.y / texHeight * worldHeight;
		if (yDown)
			v.y = worldHeight - v.y;
		return v;
	}

	/** calculate texture coordinates from game coordinates */
	public static Vector2 gameToTexture(Vector2 vec) {
		Vector2 v = new Vector2();
		v.x = vec.x / worldWidth * texWidth;
		v.y = vec.y / worldHeight * texHeight;
		return v;
	}
	
	/** calculate texture coordinates from game coordinates unto the specified texWidth and texHeight */
	public static Vector2 gameToTexture(Vector2 vec, float texWidth, float texHeight) {
		Vector2 v = new Vector2();
		v.x = vec.x / (float)Settings.map.width * texWidth;
		v.y = vec.y / (float)Settings.map.height * texHeight;
		return v;
	}
	
	/** transform time in seconds as <code>String</code> into formatted String "%2d:%2d" */
	public static String secondsToString(String stringSecs) {
		float secs = Float.valueOf(stringSecs);
		String str = String.format("%2d" + ":" + (secs % 60 < 10 ? "0" : "") + "%d",
				(int)(secs / 60),
				(int)(secs % 60));
		return str;
	}
	
	/** transform time in seconds into formatted String "min:sec:msec" as "%2d:%2d:%d" */
	public static String secondsToString(float secs) {
		int min = (int)(secs / 60);
		int sec = (int)(secs % 60);
		int msec = (int)((secs - (int)secs + 0.005) * 100);
		if (msec >= 100) {
			msec -= 100;
			sec += 1;
			if (sec >= 60) {
				sec -= 60;
				min += 1;
			}
		}
		String str = String.format("%2d:%02d:%02d",
				min, sec, msec);
		return str;
	}
	
	/** transform String min:sec:msec as "%2d:%02d:%02d" into time in seconds */
	public static float stringToSeconds(String str) {
		String[] parts = str.split(":");
		return Float.parseFloat(parts[0]) * 60
				+ Float.parseFloat(parts[1])
				+ Float.parseFloat(parts[2])/100;
	}
	
	/**
	 * For a given position in the game world (only works on the lesser Antilles as of now),
	 * return the scaling factor for the x velocity-scaling
	 * @param gamePos
	 * @return
	 */
	public static float getLocationScale(Vector2 gamePos) {
		if (Settings.map.ID != MapID.lesserAntilles) // TODO customize once whole caribbean is implemented
			return 1;

		Entry<Float, Float> floor = ingameValues.floorEntry(gamePos.y);
		Entry<Float, Float> ceil = ingameValues.ceilingEntry(gamePos.y);
		if (floor == null && ceil == null)
			return 1;
		else if (floor == null && ceil != null)
			return ceil.getValue();
		else if (floor != null && ceil == null)
			return floor.getValue();
		else if (floor.equals(ceil))
			return floor.getValue();
		
		float fac = Math.abs(gamePos.y - floor.getKey())/Math.abs(floor.getKey() - ceil.getKey());
		Float value = floor.getValue() * fac + ceil.getValue() * (1.0f - fac);
		if (value <= 0.9f || value >= 1 || value.equals(Float.NaN)) {
			Gdx.app.log("LocationScale", "Something wrong, got " + value);
			return 1f;
		}
		return value;
	}
	
	/**
	 * Initialize the lookup table for the ingame scaling due to the mercator projection
	 */
	@SuppressWarnings("unchecked")
	public static void init() {
		Json json = new Json();
		TreeMap<Object, Object> lookup = new TreeMap<Object, Object>();
		lookup = json.fromJson(lookup.getClass(), Gdx.files.internal("coordinateScaleLookup.json"));
		Array<Float> values = (Array<Float>)lookup.get("values");
		
		float minLat = (float) lookup.get("minLat"), maxLat = (float) lookup.get("maxLat"),
				stepsize = (float) lookup.get("stepsize");
		for (float lat = minLat, i = 0; i < values.size; lat += stepsize, i++) {
			ingameValues.put((lat - minLat)/(maxLat - minLat) * worldHeight, values.get((int)i));
		}
	}
	
	public static class GameTime {
		private final Float val;
		private final String str;
		
		public GameTime(Float fl) {
			val = fl;
			str = secondsToString(fl);
		}
		
		public GameTime(String s) {
			val = stringToSeconds(s);
			str = s;
		}
		
		public Float toFloat() {
			return val;
		}
		
		public String toString() {
			return str;
		}
	}
}
