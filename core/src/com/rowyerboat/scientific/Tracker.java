package com.rowyerboat.scientific;

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

	/** time in milliseconds (realworld) */
	private Array<Float> times;

	public float timeTaken = 0.5f;

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
	public int targetsReachedPointer = 0;

	public boolean isWin;
	
	public Tracker() {
		timeElapsed = 0f;
		points = new Array<Vector2>();
		times = new Array<Float>();
		times.add(0f);
		
		points.add(Settings.mission.initialBoatPos);

		targetsReached = new Vector3[Settings.mission.targetSize()];
		for (int i = 0; i < Settings.mission.targetSize(); ++i) {
			targetsReached[i] = new Vector3(Settings.mission.getTargets().get(i).getPos(), 0);
		}
	}

	public void update(float delta) {
		if (startTime == null) {
			Date date = new Date(TimeUtils.millis());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			startTime = formatter.format(date);
		}

		timeElapsed += delta;
		timeTaken += delta;
		if (timeElapsed > interval) {
			points.add(boat.getPos().cpy());
			times.add(timeTaken);
			timeElapsed = 0f;
		}
	}

	public void targetReached() {
		targetsReached[targetsReachedPointer++] = new Vector3(boat.getPos(), 1);
	}

	public Array<Vector2> getPoints() {
		return points;
	}

	public void postPoints() {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < points.size && i < times.size; ++i) {
			content.append(times.get(i).toString() + ", ");
			Vector2 vec = Transverter.gameToGPS(points.get(i));
			content.append(vec.x + ", " + vec.y + "\n");
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("ID", Settings.userID + Settings.userData.getString("userIDoffset"));
		map.put("Mission", Settings.mission.name);
		map.put("MissionAccomplished", isWin ? "TRUE" : "FALSE");
		map.put("Energy", Settings.useEnergy ? "ON" : "OFF");
		map.put("StartTime", startTime == null ? "NEVER" : startTime);
		map.put("log", content.toString());
		map.put("timeTaken", "" + timeTaken);

		HttpPoster.sendLog(map, true);
	}

	public void setBoat(Boat boat) {
		this.boat = boat;
		points.add(boat.getPos().cpy());
	}
}
