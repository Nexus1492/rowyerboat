package com.rowyerboat.gameworld;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.gameworld.GameMap.MapID;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;
import com.rowyerboat.scientific.Transverter;

public class Mission {
	
	public String name;
	public MissionID id;
	public String description;
	
	public Vector2 initialBoatPos;
	public Vector2 initialBoatDir;

	private Array<Location> targets;
	private Array<Location> dangers;
	
	private static int pointerT = 0;
	private static int pointerD = 0;
	
	static HashMap<MissionID, Mission> missions;
	
	public static enum MissionID {
		Placeholder, JaguarTeeth, Pottery
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
		default:
		case JaguarTeeth:
			initialBoatPos = Transverter.textureToGame(new Vector2(551, 384), true);
			initialBoatDir = new Vector2(1, -1);
			name = "Jaguar Teeth";
			addTargets(new Location("target0", Transverter.textureToGame(new Vector2(450, 540), true)),
					new Location("target1", Transverter.textureToGame(new Vector2(447, 372), true)),
					new Location("target2", Transverter.textureToGame(new Vector2(551, 384), true))
					);
			Settings.map = new GameMap(TimeUtils.millis(), MapID.lesserAntilles);
			break;
		case Pottery:
			initialBoatPos = Transverter.textureToGame(new Vector2(321, 48), true);
			initialBoatDir = new Vector2(-1, 0);
			name = "Pottery Acquisition";
			addTargets(new Location("target0", Transverter.textureToGame(new Vector2(210, 33), true)),
					new Location("target1", Transverter.textureToGame(new Vector2(175, 64), true)),
					new Location("target2", Transverter.textureToGame(new Vector2(118, 76), true)));
			Settings.map = new GameMap(TimeUtils.millis(), MapID.lesserAntilles);
			break;
		case Placeholder:
			initialBoatPos = new Vector2(100, 100);
			initialBoatDir = new Vector2(1, 1);
			name = "Placeholder";
			addTargets(new Location("target0", new Vector2(1000, 1000)));
			Settings.map = new GameMap(TimeUtils.millis(), MapID.caribbean);
		}
		description = getDesc(id);
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
	 * Simply resets the pointers for dangers and targets.
	 */
	public static void reset() {
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
					+ "to bring them there safely. Make sure to make two stops in the Virgin Islands to "
					+ "stock up on food and water.";
		case JaguarTeeth:
			return "Jaguar Teeth\n\n"
					+ "Next month will be the inauguration of the new chief in your village on Barbados. "
					+ "Since you are the most skilled canoe-captain of the whole island, "
					+ "you are given the task to make the dangerous journey to Trinidad to trade some of your "
					+ "villages most elaborate pottery for rare and precious Jaguar teeth.\n"
					+ "On your way back, you should make a stop at Grenada to gather some fresh water "
					+ "and provisions. Do not stop anywhere else and make sure to keep in mind the strong "
					+ "currents around the Grenadines.";
		case Placeholder:
			return "Test\n\nOn the whole caribbean.";
		}
		return null;
	}
}
