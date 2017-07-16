package com.rowyerboat.scientific;

import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.gameobjects.*;
import com.rowyerboat.gameworld.Campaign;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.gameworld.Campaign.CampaignID;
import com.rowyerboat.helper.HttpPoster;
import com.rowyerboat.helper.Settings;

/**
 * Class to track the movement of a boat on a given map via its path. Also
 * responsible for POSTing the boatlogs to the server. Offer utility function to
 * output an SVG with marked path (TBI).
 * 
 * @author Roman Lamsal
 *
 */
public class Tracker {

	private Boat boat;
	private String startTime = null;

	private float timeElapsed = 0f;
	private Array<Vector2> points;
	private Array<Vector2> currentData;

	/** time in milliseconds (realworld) */
	private Array<Float> times;

	public float timeTaken = 0f;

	/**
	 * interval to measure the boats position in seconds (e.g. interval = 0.2f
	 * => measure all 0.2 seconds
	 */
	public float interval = 0.2f;
	public boolean fixedInterval = true;

	/**
	 * (x,y)-coordinates of reached targets with z indicating if the target was
	 * actually reached (1) or not (0)
	 */
	public Vector3[] targetsReached;
	public int targetsReachedPointer = -1;

	public boolean isWin;
	
	public Tracker() {
		timeElapsed = 0f;
		points = new Array<Vector2>();
		currentData = new Array<Vector2>();
		times = new Array<Float>();
		times.add(0f);
		
		Mission mission = Settings.getMission();
		points.add(mission.initialBoatPos);
		currentData.add(new Vector2(0, 0));

		targetsReached = new Vector3[mission.targetSize()];
		for (int i = 0; i < mission.targetSize(); ++i) {
			targetsReached[i] = new Vector3(mission.getTargets().get(i).getPos(), 0);
		}
	}

	public void update(float delta) {
		if (boat == null)
			throw new NullPointerException("Must set a boat in Tracker via Tracker.setBoat()");
		
		if (startTime == null) {
			Date date = new Date(TimeUtils.millis());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			startTime = formatter.format(date);
		}

		timeElapsed += delta;
		timeTaken += delta;
		if (timeElapsed > interval) {
			points.add(boat.getPos().cpy());
			currentData.add(boat.getCurrentDisplacement());
			times.add(timeTaken);
			timeElapsed = 0f;
		}
	}

	public void targetReached() {
		targetsReachedPointer = Math.max(0, targetsReachedPointer);
		targetsReached[targetsReachedPointer++] = new Vector3(boat.getPos(), 1);
	}

	public Array<Vector2> getPoints() {
		return points;
	}

	/** 
	 * Send HttpPost to Server with information from Tracker object,
	 * also update the campaign progress score
	 */
	public void postPoints() {
		Mission mission = Settings.getMission();
		if (isWin) { //update progress
			Settings.campaignProgress.putString(Settings.missionID.toString(), mission.campaignID.toString());
			Settings.campaignProgress.flush();
			Campaign.getCampaign(Settings.missionID).updateProgress();
		}
		
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < points.size && i < times.size; ++i) {
			content.append(times.get(i).toString() + ", ");
			Vector2 vec = Transverter.gameToGPS(points.get(i));
			content.append(vec.x + ", " + vec.y + ", ");
			vec = currentData.get(i);
			content.append(vec.x + ", " + vec.y);
			content.append("\n");
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("ID", Settings.userID + Settings.userData.getString("userIDoffset"));
		map.put("Mission", mission.id.toString());
		map.put("Version", String.valueOf(Settings.userData.getFloat("version")));
		map.put("Platform", Gdx.app.getType().toString());
		map.put("TutorialFinished", "" + Campaign.getCampaign(CampaignID.TutorialCampaign).isFinished);
		map.put("MissionAccomplished", isWin ? "TRUE" : "FALSE");
		map.put("Energy", Settings.useEnergy ? "ON" : "OFF");
		map.put("StartTime", startTime == null ? "NEVER" : startTime);
		map.put("log", content.toString());
		map.put("TimeTaken", "" + timeTaken);
		map.put("CurrentDate", mission.currentDate != null ? mission.currentDate : "");

		HttpPoster.sendLog(map, true);
	}

	public void setBoat(Boat boat) {
		this.boat = boat;
		points.add(boat.getPos().cpy());
	}
}
