package com.rowyerboat.helper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.gameworld.GameMap;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.gameworld.Mission.MissionID;
import com.rowyerboat.rendering.GameRenderer;
import com.rowyerboat.rendering.GameRenderer.CameraMode;
import com.rowyerboat.scientific.Tracker;
import com.rowyerboat.screens.MissionSelectionScreen;

public class Settings {
	public static Game game;
	public static Preferences highscores;
	public static Preferences userData;
	public static Preferences lastSession;
	public static String userID;
	public static boolean firstTime;
	
	public static AssetManager assets;
	
	// Screen resolution
	final public static int width = 1040;
	final public static int height = 585;
	
	// rendering (dynamic)
	public static CameraMode cameraMode;
	public static boolean debug;
	public static boolean hud;
	public static int shaderID;

	public static float boatScale = 0.5f;
	final public static float speedScale = 25f;
	
	// set by map
	public static Vector2 initialBoatPos;
	public static Vector2 initialBoatDir;
	
	// set by menu screen
	public static GameMap map;

	// set by missionSelection screen
	public static Mission mission;
	public static boolean useEnergy;
	
	// set by game screen
	public static GameWorld world;
	public static GameRenderer renderer;
	
	// set by world
	public static Tracker tracker;
	
	public static void init(Game g) {
		MathUtils.random.setSeed(TimeUtils.nanoTime());
		
		game = g;
		highscores = Gdx.app.getPreferences("rowyerboat.highscores");
		userData = Gdx.app.getPreferences("rowyerboat.userData");
		lastSession = Gdx.app.getPreferences("rowyerboat.lastSession");
		
		checkVersion();
		
		userID = userData.getString("userID", "");
		
		useEnergy = lastSession.getBoolean("lastMissionEnergy", false);
		
		// Read out the last Mission
		mission = new Mission(MissionID.valueOf(lastSession.getString("lastMission",
				MissionID.Pottery.toString())));
		
		firstTime = userData.getBoolean("firstTime", true); firstTime = true;
		
		debug = false;
		hud = false;
		shaderID = 0;
		boatScale = 0.5f;
	}
	
	private static void checkVersion() {
		float version = userData.getFloat("version", 1.00f);
		float curr_version = 1.03f;
		if (version < 1.01f) {
			userData.remove("lastMission");
			userData.putBoolean("lastMissionEnergy", false);
			userData.putBoolean("tutorialDisplayed", false);
		}
		if (version < 1.02f) {
			userData.remove("lastMission");
			userData.remove("lastMissionEnergy");
			lastSession.putBoolean("lastMissionEnergy", false);
			updateMission(new Mission(MissionID.Pottery));
		}
		if (version < 1.03f) {
			// Bugfix: Highscores are not properly saved (only for "Mission01ON"/"Mission01OFF")
			highscores.putFloat(Mission.MissionID.Pottery + "ON",
					highscores.getFloat("Mission01ON", Float.MAX_VALUE));
			highscores.putFloat(Mission.MissionID.Pottery + "OFF",
					highscores.getFloat("Mission01OFF", Float.MAX_VALUE));
			highscores.putFloat(Mission.MissionID.JaguarTeeth + "ON", Float.MAX_VALUE);
			highscores.putFloat(Mission.MissionID.JaguarTeeth + "OFF", Float.MAX_VALUE);
			highscores.remove("Mission01ON");
			highscores.remove("Mission01OFF");
			
			if (userData.getString("userIDoffset", null) == null) {
				int offset = Math.abs(MathUtils.random.nextInt());
				userData.putString("userIDoffset", "-" + Integer.toString(offset));
				userData.flush();
			}
			if (userData.getString("userIDoffset").charAt(0) != '-')
				userData.putString("userIDoffset", "-" + userData.getString("userIDoffset"));
		}
		userData.putFloat("version", curr_version);
		userData.flush();
		lastSession.flush();
		highscores.flush();
	}

	public static void updateEnergy(boolean nrg) {
		useEnergy = nrg;
		userData.putBoolean("lastMissionEnergy", nrg);
		userData.flush();
	}
	
	public static void updateMission(Mission mis) {
		mission = mis;
		lastSession.putString("lastMission", mis.id.toString());
		lastSession.flush();
	}
}
