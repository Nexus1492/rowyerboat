package com.rowyerboat.gameworld;

import java.util.HashMap;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.gameworld.Campaign.CampaignID;
import com.rowyerboat.gameworld.GameMap.MapID;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;
import com.rowyerboat.scientific.Transverter;

public class Mission {
	public String name;
	public MissionID id;
	public String description;
	public CampaignID campaignID;
	public GameMap map;
	
	public Vector2 initialBoatPos;
	public Vector2 initialBoatDir;

	private Array<Location> targets;
	private Array<Location> dangers;
	
	private static int pointerT = 0;
	private static int pointerD = 0;
	
	private static HashMap<MissionID, Mission> missions;
	
	public static enum MissionID {
		Tutorial0, Tutorial1, //tutorialcampaign
		JaguarTeeth, JaguarTeeth2, Pottery, //campaign01
		Placeholder; //campaign02;
		
		public CampaignID getCampaignID() {
			return missions.get(this).campaignID;
		}
	}
	
	public static void init() {
		missions = new HashMap<MissionID, Mission>();
		for (MissionID id : MissionID.values())
			missions.put(id, new Mission(id));
	}
	
	public static Mission getMission(String idString) {
		return getMission(MissionID.valueOf(idString));
	}
	
	public static Mission getMission(MissionID id) {
		return missions.get(id);
	}
	
	private Mission(String missionName) {
		this(MissionID.valueOf(missionName));
	}
	
	private Mission(MissionID id) {
		targets = new Array<Location>();
		dangers = new Array<Location>();
		this.id = id;
		switch(id){
		case JaguarTeeth:
			initialBoatPos = Transverter.GPStoGame(new Vector2(14.4f,-60.8f));
			initialBoatDir = new Vector2(-1, -1);
			name = "Jaguar Teeth (1/2)";
			/*initialBoatPos = Transverter.textureToGame(new Vector2(551, 384), true);
			addTargets(new Location("target0", Transverter.textureToGame(new Vector2(450, 540), true)),
					new Location("target1", Transverter.textureToGame(new Vector2(447, 372), true)),
					new Location("target2", Transverter.textureToGame(new Vector2(551, 384), true))
					);*/
			addTargets(new Location("target0", Transverter.GPStoGame(new Vector2(13.2f,-59.7f))),
					new Location("target1", Transverter.GPStoGame(new Vector2(13.4f,-61.2f))));
			map = new GameMap(TimeUtils.millis(), MapID.lesserAntilles);
			break;
		case JaguarTeeth2:
			initialBoatPos = Transverter.GPStoGame(new Vector2(13.4f,-61.2f));
			initialBoatDir = new Vector2(-1, -1);
			name = "Jaguar Teeth (2/2)";
			
			addTargets(Transverter.GPStoGame(new Vector2(10.9f,-61.1f)),
					Transverter.GPStoGame(new Vector2(14.4f,-60.8f)));
			map = new GameMap(TimeUtils.millis(), MapID.lesserAntilles);
			break;
		case Pottery:
			initialBoatPos = Transverter.GPStoGame(new Vector2(16.3f,-61.1f));
			initialBoatDir = new Vector2(-1, 1);
			name = "Pottery Acquisition";
			addTargets(new Location("target0", Transverter.GPStoGame(new Vector2(17.1f,-61.6f))),
					new Location("target1", Transverter.GPStoGame(new Vector2(18.4f,-63.1f))),
					new Location("target2", Transverter.GPStoGame(new Vector2(18.6f,-64.8f))),
					new Location("target3", Transverter.GPStoGame(new Vector2(18.1f,-65.3f))),
					new Location("target4", Transverter.GPStoGame(new Vector2(17.9f,-66.2f))));
			map = new GameMap(TimeUtils.millis(), MapID.lesserAntilles);
			break;
		case Placeholder:
			name = "Placeholder";
			map = new GameMap(TimeUtils.millis(), MapID.lesserAntilles);
			break;
		case Tutorial0:
			initialBoatPos = new Vector2(550, 250);
			initialBoatDir = new Vector2(0, 1);
			name = "Obstacle Course";
			addTargets(new Location("target0", new Vector2(550, 750)));
			addTargets(new Location("target1", new Vector2(550, 1300)));
			map = new GameMap(0, MapID.tutorial);
			break;
		case Tutorial1:
			initialBoatPos = new Vector2(500, 150);
			initialBoatDir = new Vector2(0, 1);
			addTargets(new Location("target0", new Vector2(500, 850), 20));
			map = new GameMap(1, MapID.tutorial);
			name = "Rough Currents";
			
			break;
		}
		description = getDesc(id);
	}
	
	public void addTargets(Vector2... vectors) {
		for (int i = 0; i < vectors.length; ++i) {
			this.targets.add(new Location("target"+i, vectors[i]));
		}
	}
	
	public void addTargets(Location... locations) {
		for (Location loc : locations) {
			this.targets.add(loc);
		}
	}
	
	public void addDangers(Location... locations) {
		for (Location loc : locations)
			this.dangers.add(loc);
	}
	
	public Location nextTarget() {
		if (hasTarget())
			return targets.get(pointerT++);
		else {
			return null;
		}
	}
	
	/** returns whether a next target is given */
	public boolean hasTarget() {
		return !(pointerT >= targets.size);
	}

	
	public Location nextDanger() {
		if (hasDanger())
			return dangers.get(pointerD++);
		else {
			pointerD = 0;
			return null;
		}
	}
	
	/** returns whether a next danger is given */
	public boolean hasDanger() {
		return !(pointerD >= dangers.size);
	}
	
	public Array<Location> getTargets() {
		return targets;
	}
	
	public Array<Location> getLocations() {
		Array<Location> arr = new Array<Location>();
		arr.addAll(targets);
		arr.addAll(dangers);
		return arr;
	}
	
	public int targetSize() {
		return targets.size;
	}
	
	public int dangersSize() {
		return dangers.size;
	}
	
	/**
	 * Debug function
	 */
	public void printGPS() {
		System.out.println(this.name);
		int i = 0;
		System.out.println((i++) +".: " + Transverter.gameToGPS(initialBoatPos));
		for (Location vec : targets)
			System.out.println((i++) +".: " + Transverter.gameToGPS(vec.getPos()));
	}
	
	/**
	 * Simply resets the pointers for dangers and targets.
	 */
	public void reset() {
		pointerT = 0;
		pointerD = 0;
	}
	
	public static String getDesc(MissionID id) {
		switch(id){
		case Pottery:
			return "Pottery Acquisition\n\n"
					+ "Your village's chief has heard about some very beautiful pottery being "
					+ "made by the people living in Puerto Rico. He now wants to send four of the village's "
					+ "most skilled potters for an apprenticeship to learn the techniques. Your task is "
					+ "to bring them there safely. Make sure to pass by some islands on your way to "
					+ "stock up on food and water.";
		case JaguarTeeth:
			return "Jaguar Teeth (1/2)\n\n"
					+ "Next month will be the inauguration of the new chief in your village on Martinique. "
					+ "Since you are the most skilled canoe-captain of the whole island, "
					+ "you are given the task to make the dangerous journey to Trinidad to trade some of your "
					+ "villages most elaborate pottery for rare and precious jaguar teeth.\n\n"
					+ "But instead of going directly there, make sure to bring some of your fellow "
					+ "villagers to Barbados first and stock up on provisions and fresh water at the Grenadines.\n";
		case JaguarTeeth2:
			return "Jaguar Teeth (2/2)\n\n"
					+ "You are still on your journey to Trinidad. After you stocked up on fresh water and provisions, "
					+ "everything is well prepared so you can finally make your way to Trinidad.\n"
					+ "You have no time to waste anymore, so make your way to Trinidad and back home to Martinique "
					+ "without pause.\n"
					+ "The village elder warned you about the strong currents around Trinidad and that it might "
					+ "be wise to navigate around them.\n";
		case Placeholder:
			return "Test\n\nTrying to test Transverter.GPStoGame()";
		case Tutorial0:
			String desc = "Get comfortable with the basic controls and navigate through the given course "
					+ "where no currents are present yet.\n";
			
			if (Gdx.app.getType() != ApplicationType.Desktop)
				desc += "By swiping downwards on your screen in the left or right half of the screen you ";
			else
				desc += "By pressing the left or right arrow key on your keyboard you ";
			
			desc += "navigate the boat.\n\n"
					+ "Here are some Tips:\n"
					+ "The faster you are, the worse you can navigate the boat! Lose some speed for better "
					+ "maneuverability when needed.\n"
					+ "Around the circular HUD element on the top left you can see an arrow "
					+ "on the outer ring, pointing towards your next target.\n"
					+ "Press the \"Show Map\" button below to get an overview of the map; the blue dot marks your "
					+ "starting point, the red dots your targets.\n\n"
					+ "Be sure not to hit any of the obstacles or fall off the world or you'll lose!";
			return desc;
		case Tutorial1:
			return "Now that you are able to navigate your boat through gentle waters, let me introduce "
					+ "you to currents.\n\n"
					+ "In this mission, you have only one target to reach. But the direct way might not be "
					+ "an option...\n\n"
					+ "The arrow inside the circular HUD element on the top left will tell you, in which direction "
					+ "the currents are dragging you. Use this information to your advantage!\n"
					+ "Also, you can quickreset the boat by pressing "
					+ (Gdx.app.getType() == ApplicationType.Desktop ? "BACKSPACE" : "BACK on your device.");
		}
		return null;
	}
}
