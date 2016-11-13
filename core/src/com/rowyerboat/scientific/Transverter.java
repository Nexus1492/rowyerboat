package com.rowyerboat.scientific;

import com.badlogic.gdx.math.Vector2;

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
	static float minLon = 292, maxLon = 301.1f;
	/** longitude in real-world coordinates */
	static float minLat = 9.5f, maxLat = 19.1f;
	
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
		v.x = vec.x / worldWidth * texWidth;
		v.y = vec.y / worldHeight * texHeight;
		return v;
	}
	
	/** transform time in seconds into formatted String "%2d:%2d" */
	public static String secondsToString(float secs) {
		String str = String.format("%2d" + ":" + (secs % 60 < 10 ? "0" : "") + "%d",
				(int)(secs / 60),
				(int)(secs % 60));
		
		return str;
	}
	
	/** transform String "%2d:%2d" into time in seconds */
	public static float stringToSeconds(String str) {
		String[] parts = str.split(":");
		return Float.parseFloat(parts[0]) * 60 + Float.parseFloat(parts[1]);
	}
}
