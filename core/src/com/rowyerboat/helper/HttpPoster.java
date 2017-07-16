package com.rowyerboat.helper;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.utils.Json;
import com.rowyerboat.gameworld.Campaign;
import com.rowyerboat.gameworld.Campaign.CampaignID;
import com.rowyerboat.gameworld.Mission.MissionID;
import com.rowyerboat.screens.GameScreen;
import com.rowyerboat.screens.HighscoreScreen;
import com.rowyerboat.screens.MainScreen;

public class HttpPoster {
	private final static String url = "http://www.nexus1492.eu/boatlogs/boatlog.php";
	private final static String urlRegister = "http://www.nexus1492.eu/boatlogs/registerEmail.php";
	private final static String urlTest = "http://www.nexus1492.eu/boatlogs/testing/boatlog.php";

	private static Preferences logStack = Gdx.app.getPreferences("rowyerboat.logStack");

	/**
	 * Send logs via HTTP POST to <code>HttpPoster.url</code>
	 * 
	 * @param map
	 * @return
	 */
	public static void sendLog(HashMap<String, String> map, boolean addToStack) {
		HttpRequest req = new HttpRequest(HttpMethods.POST);
		req.setUrl(url);
		req.setContent(HttpParametersUtils.convertHttpParameters(map));

		GameScreenResponseListener listener = new GameScreenResponseListener(req, addToStack);
		Gdx.net.sendHttpRequest(req, listener);
	}
	
	/**
	 * Utility method which is for users who played the game pre-1.04. The actual logs to be pushed
	 * should ALWAYS be send by <code>Tracker.postPoints()</code> method.
	 * 
	 * @param id
	 * @param nrg
	 */
	public static void postPersonalHighscore(MissionID id, boolean nrg) {
		float timeTaken = Settings.highscores.getFloat(id.toString() + (nrg ? "ON" : "OFF"), -1);
		if (timeTaken <= 10) // TODO set to 70, more convenient. 10 is just a random number
			return;
		Map<String, String> map = new HashMap<String, String>();
		map.put("ID", Settings.userID + Settings.userData.getString("userIDoffset"));
		map.put("Mission", id.toString());
		map.put("Version", String.valueOf(Settings.userData.getFloat("version")));
		map.put("Platform", Gdx.app.getType().toString());
		map.put("TutorialFinished", "" + Campaign.getCampaign(CampaignID.TutorialCampaign).isFinished);
		map.put("MissionAccomplished", "" + Settings.tracker.isWin);
		map.put("Energy", nrg ? "ON" : "OFF");
		map.put("StartTime", "NEVER");
		map.put("TimeTaken", String.valueOf(timeTaken));
		
		HttpRequest req = new HttpRequest(HttpMethods.POST);
		req.setUrl(url);
		req.setContent(HttpParametersUtils.convertHttpParameters(map));
		
		Gdx.net.sendHttpRequest(req, new GameScreenResponseListener(req, true) {
			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				Gdx.app.log("Posting personal records: ", httpResponse.getResultAsString());
			}
			@Override
			public void failed(Throwable t) {
				writeToStack();
			}
			@Override
			public void cancelled() {
			}
		});
	}

	/**
	 * Check the log stack and try to send as many requests as possible
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void sendLogsFromStack() {
		logStack = Gdx.app.getPreferences("rowyerboat.logStack");
		Map<String, String> map = (Map<String, String>) logStack.get();
		int num = map.size();
		Gdx.app.log("Processing log stack", num + " log" + (num != 1 ? "s" : "") + " found.");

		Json json = new Json();

		for (String key : map.keySet()) {
			String jsonReq = map.get(key);
			HttpRequest req = json.fromJson(HttpRequest.class, jsonReq);

			GameScreenResponseListener listener = new GameScreenResponseListener(req, false, key) {
				@Override
				public void handleHttpResponse(HttpResponse httpResponse) {
					if (httpResponse.getStatus().getStatusCode() == 200) {
						logStack.remove(ID);
						logStack.flush();
						Gdx.app.log("Log succesfully sent", ID);
					}
				}
				@Override
				public void failed(Throwable t) {
				}
			};
			Gdx.net.sendHttpRequest(req, listener);
		}
	}
	
	public static void getHighscores(MissionID missionID, boolean useEnergy) {
		HttpRequest req = new HttpRequest(HttpMethods.GET);
		req.setUrl(url);
		req.setContent("Energy=" + (useEnergy ? "ON" : "OFF") + "&Mission=" + missionID.toString());
		HttpResponseListener listener = new GameScreenResponseListener(null, false) {
			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				if (httpResponse.getStatus().getStatusCode() == 200)
					showNextScreen(httpResponse.getResultAsString());
				else
					showNextScreen("Error:\nNo internet connection");
			}

			@Override
			public void failed(Throwable t) {
				showNextScreen("Error:\nNo internet connection");
			}
			@Override
			public void cancelled() {
			}
		};
		Gdx.net.sendHttpRequest(req, listener);
	}

	public static void checkStack() {
		checkConnection(new Runnable() {
			@Override
			public void run() {
				HttpPoster.sendLogsFromStack();
			}
		}, null);
	}
	
	public static void checkConnection(final Runnable success, final Runnable fail) {
		HttpRequest req = new HttpRequest(HttpMethods.POST);
		req.setUrl(urlTest);
		req.setContent("Testing internet connection.");
		HttpResponseListener listener = new HttpResponseListener(){

			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				Settings.online = true;
				Gdx.app.log("Online Status", "True");
				if (success != null && httpResponse.getStatus().getStatusCode() == 200)
					success.run();
			}
			@Override
			public void failed(Throwable t) {
				Settings.online = false;
				Gdx.app.log("Online Status", "False");
				if (fail != null)
					fail.run();
			}
			@Override
			public void cancelled() {
				Settings.online = false;
				Gdx.app.log("Online Status", "False");
				if (fail != null)
					fail.run();
			}
		};
		Gdx.net.sendHttpRequest(req, listener);
	}

	private static void showNextScreen(final String echo) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Settings.game.setScreen(new HighscoreScreen(Settings.game, echo),
						Settings.game.getScreen().getClass() != GameScreen.class);
			}
		});
	}
	
	public static void registerEmail(String mail) {
		HttpRequest req = new HttpRequest(HttpMethods.GET);
		req.setUrl(urlRegister);
		req.setContent("Mail="+mail+"&UserID="+Settings.userID+"&UserIDOffset="+Settings.userIDOffset);
		
		Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
			
			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				Gdx.app.log("RegisterEmail", httpResponse.getResultAsString());	
			}
			@Override
			public void failed(Throwable t) {
				t.printStackTrace();
			}
			@Override
			public void cancelled() {
			}
		});
	}

	public static class GameScreenResponseListener implements HttpResponseListener {
		HttpRequest req;
		boolean addToStack;
		String ID;
		
		public GameScreenResponseListener(HttpRequest req, boolean addToStack) {
			this.req = req;
			this.addToStack = addToStack;
		}

		public GameScreenResponseListener(HttpRequest req, boolean addToStack, String ID) {
			this(req, addToStack);
			this.ID = ID;
		}

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			String res = httpResponse.getResultAsString();
			if (httpResponse.getStatus().getStatusCode() == 200)
				showNextScreen(res);
			else {
				showNextScreen("Error:\nCode: " + httpResponse.getStatus().getStatusCode());
				writeToStack();
			}
		}

		@Override
		public void failed(Throwable t) {
			Gdx.app.log("Http Request", "Failed");
			if (addToStack) {
				t.printStackTrace();
				writeToStack();
			}
			showNextScreen("Error:\nNo internet connection");
		}

		@Override
		public void cancelled() {
			Gdx.app.log("Http Request", "cancelled");
		}
		
		protected void writeToStack() {
			Json json = new Json();
			String id = String.valueOf(req.getContent().hashCode());
			logStack.putString(id, json.toJson(req));
			Gdx.app.log("Put log into stack, ID: ", id);
			logStack.flush();
		}
	}
}
