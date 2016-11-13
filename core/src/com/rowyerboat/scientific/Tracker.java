package com.rowyerboat.scientific;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.gameobjects.*;
import com.rowyerboat.helper.Settings;

/**
 * Class to track the movement of a boat on a given map via its path.
 * Offer utility function to output an SVG with marked path (TBA).
 * 
 * @author Roman
 *
 */
public class Tracker {

	private Boat boat;
	private String startTime = null;
		
	private float timeElapsed = 0f;
	private Array<Vector2> points;
	
	/** time in milliseconds (realworld) */
	private Array<Float> times;
	
	public float timeTaken = 0f;
	
	/** interval to measure the boats position in seconds */
	public float interval = 0.2f;
	public boolean fixedInterval = true;
	
	public boolean isWin;
	
	public Tracker(Boat boat) {
		this.boat = boat;
		timeElapsed = 0f;
		points = new Array<Vector2>();
		times = new Array<Float>();
		times.add(0f);
		
		points.add(boat.getPos().cpy());
	}
	
	public void update(float delta) {
		if (startTime == null) {
			startTime = new Date(TimeUtils.millis()).toString();
		}
		
		timeElapsed += delta;
		timeTaken += delta;
		if (timeElapsed > interval) {
			points.add(boat.getPos().cpy());
			times.add(timeTaken);
			timeElapsed = 0f;
		}
	}
	
	public void render(ShapeRenderer batch) {
		Vector2 prev = points.first();
		batch.begin(ShapeType.Filled);
		batch.setColor(Color.BLACK);
		for(int i = 1; i < points.size; ++i) {
			batch.circle(points.get(i).x, points.get(i).y, 5f);
			batch.line(prev, points.get(i));
			prev = points.get(i);
		}
		batch.end();
	}
	
	public Array<Vector2> getPoints() {
		return points;
	}
	
	public void postPoints() {
		String urlString = "http://www.j-c-a.de/boatlog.php";// urlString = "http://httpbin.org/post";
		String content = "";
		for (int i = 0; i < points.size; ++i) {
			content += times.get(i).toString() + ", ";
			Vector2 vec = Transverter.gameToGPS(points.get(i));
			content += vec.x + ", " + vec.y + "\n";
		}
		
		HttpRequest req = new HttpRequest(HttpMethods.POST);
		req.setUrl(urlString);
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("ID", Settings.userID + Settings.userData.getString("userIDoffset"));
		map.put("Mission", Settings.mission.name);
		map.put("MissionAccomplished", isWin ? "TRUE" : "FALSE");
		map.put("Energy", Settings.useEnergy ? "ON" : "OFF");
		map.put("StartTime", startTime == null ? "NEVER" : startTime);
		map.put("log", content);
		
		req.setContent(HttpParametersUtils.convertHttpParameters(map));
		
		Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				Gdx.app.log("Http Request", "Code: " + httpResponse.getStatus().getStatusCode());
				Gdx.app.log("Http Request" , "Result: " + httpResponse.getResultAsString());
			}
			@Override
			public void failed(Throwable t) {
				Gdx.app.log("Http Request", "Failed: " + t.getMessage());
			}
			@Override
			public void cancelled() {
				Gdx.app.log("Http Request", "cancelled");
			}
			
		});
	}
}
