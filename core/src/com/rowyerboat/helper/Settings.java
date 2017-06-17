package com.rowyerboat.helper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.game.RowYerBoat;
import com.rowyerboat.gameworld.Campaign;
import com.rowyerboat.gameworld.GameMap;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.gameworld.Campaign.CampaignID;
import com.rowyerboat.gameworld.Mission.MissionID;
import com.rowyerboat.rendering.GameRenderer;
import com.rowyerboat.rendering.GameRenderer.CameraMode;
import com.rowyerboat.scientific.Tracker;

public class Settings {
	public static RowYerBoat game;
	
	public static boolean online = false;
	
	public static Preferences highscores;
	public static Preferences userData;
	public static Preferences lastSession;
	public static Preferences campaignProgress;
	public static String userID;
	public static String userIDOffset;
	public static boolean firstTime;
	
	// Screen resolution
	final public static int width = 1040;
	final public static int height = 585;
	
	// rendering (dynamic)
	public static CameraMode cameraMode;
	public static boolean debug;
	public static boolean hud;
	public static int shaderID;

	public static float boatScale = 0.1f;
	final public static float speedScale = 25f;
	
	// set by menu screen
	public static GameMap map;

	// set by missionSelection screen and on startup
	private static Mission mission; // needs to be private in order to not mess up things, call setMission() instead
	public static MissionID missionID;
	public static boolean useEnergy;
	
	// set by game screen
	public static GameWorld world;
	public static GameRenderer renderer;
	
	// set by world
	public static Tracker tracker;
	
	public static void init(Game g) {
		MathUtils.random.setSeed(TimeUtils.nanoTime());
		
		game = (RowYerBoat) g;
		highscores = Gdx.app.getPreferences("rowyerboat.highscores");
		userData = Gdx.app.getPreferences("rowyerboat.userData");
		lastSession = Gdx.app.getPreferences("rowyerboat.lastSession");
		campaignProgress = Gdx.app.getPreferences("rowyerboat.campaignProgress");
		
		checkVersion();
		
		userID = userData.getString("userID", "");
		userIDOffset = userData.getString("userIDoffset", "");
		
		useEnergy = lastSession.getBoolean("lastMissionEnergy", false);
		
		// Read out the last Mission
		setMission(Mission.getMission(MissionID.valueOf(
				lastSession.getString("lastMission", MissionID.Pottery.toString())
				)).id); // if no last mission is given, set lastMission to POTTERY ACQUISITION
		
		firstTime = userData.getBoolean("firstTime", true);
		
		debug = false;
		hud = false;
		shaderID = 0;
	}
	
	private static void checkVersion() {
		float version = userData.getFloat("version", 1.00f);
		float curr_version = 1.04f;
		if (version < 1.01f) {
			userData.remove("lastMission");
			userData.putBoolean("lastMissionEnergy", false);
			userData.putBoolean("tutorialDisplayed", false);
		}
		if (version < 1.02f) {
			userData.remove("lastMission");
			userData.remove("lastMissionEnergy");
			lastSession.putBoolean("lastMissionEnergy", false);
			setMission(MissionID.Pottery);
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

			// save userIDoffset as negative integer
			if (userData.getString("userIDoffset", null) == null) {
				int offset = Math.abs(MathUtils.random.nextInt());
				userData.putString("userIDoffset", "-" + Integer.toString(offset));
				userData.flush();
			} // if userIDoffset is already set, make sure it is negative
			else if (userData.getString("userIDoffset").charAt(0) != '-')
				userData.putString("userIDoffset", "-" + userData.getString("userIDoffset"));
		}
		if (version < 1.04 && version < curr_version) {
			campaignProgress.putBoolean("TutorialCampaign_UNLOCKED", true);
			campaignProgress.putBoolean("Campaign01_UNLOCKED", true);
			campaignProgress.putBoolean("Campaign02_UNLOCKED", false);
			
			lastSession.putString("lastMission", MissionID.Tutorial0.toString());
			lastSession.putBoolean("lastMissionEnergy", false);
			
			userData.putBoolean("tutorialDisplayed", true);
		}
		userData.putFloat("version", curr_version);
		userData.flush();
		lastSession.flush();
		highscores.flush();
		campaignProgress.flush();
	}

	public static void updateEnergy(boolean nrg) {
		useEnergy = nrg;
		userData.putBoolean("lastMissionEnergy", nrg);
		userData.flush();
	}
	
	/** 
	 * update the ID of the last selected mission
	 * 
	 * @param mis
	 */
	public static void setMission(MissionID mis) {
		missionID = mis;
		mission = Mission.getMission(missionID);
		map = mission.map;
		lastSession.putString("lastMission", mis.toString());
		lastSession.flush();
		tracker = new Tracker();
	}

	public static Mission getMission() {
		return mission;
	}
}
