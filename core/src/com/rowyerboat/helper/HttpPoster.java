package com.rowyerboat.helper;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.utils.Json;
import com.rowyerboat.screens.HighscoreScreen;
import com.rowyerboat.screens.WorldMapScreen;

public class HttpPoster {
	private final static String url = "http://localhost/rowyerboat/boatlog.php";//"http://www.nexus1492.eu/boatlogs/boatlog.php";
	private final static String urlTest = "http://httpbin.org/post";
	
	public static String echo;

	private static Preferences logStack = Gdx.app.getPreferences("rowyerboat.logStack");

	/**
	 * Send logs via HTTP POST to given url
	 * 
	 * @param map
	 * @return
	 */
	public static void sendLog(HashMap<String, String> map, boolean addToStack) {
		HttpRequest req = new HttpRequest(HttpMethods.POST);
		req.setUrl(url);
		req.setContent(HttpParametersUtils.convertHttpParameters(map));

		echo = "leer";
		MyResponseListener listener = new MyResponseListener(req, addToStack);
		Gdx.net.sendHttpRequest(req, listener);
	}

	/**
	 * Check the log stack and try to send as many requests as possible
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void checkStack() {
		logStack = Gdx.app.getPreferences("rowyerboat.logStack");
		Map<String, String> map = (Map<String, String>) logStack.get();
		int num = map.size();
		Gdx.app.log("Processing log stack", num + " log" + (num != 1 ? "s" : "") + " found.");

		Json json = new Json();

		for (String key : map.keySet()) {
			String jsonReq = map.get(key);
			HttpRequest req = json.fromJson(HttpRequest.class, jsonReq);

			MyResponseListener listener = new MyResponseListener(req, false, key) {
				@Override
				public void handleHttpResponse(HttpResponse httpResponse) {
					if (httpResponse.getStatus().getStatusCode() == 200) {
						logStack.remove(ID);
						logStack.flush();
					}
				}
			};
			Gdx.net.sendHttpRequest(req, listener);
		}
	}
	
	public static void getHighscores() {
		
	}
	
	public static void checkConnection() {
		HttpRequest req = new HttpRequest(HttpMethods.POST);
		req.setUrl(urlTest);
		req.setContent("Testing internet connection.");
		HttpResponseListener listener = new HttpResponseListener(){

			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				Settings.online = true;
				Gdx.app.log("Online Status", "True");
			}
			@Override
			public void failed(Throwable t) {
				Settings.online = false;
				Gdx.app.log("Online Status", "False");
			}
			@Override
			public void cancelled() {
				Settings.online = false;
				Gdx.app.log("Online Status", "False");
			}
		};
		Gdx.net.sendHttpRequest(req, listener);
	}

	private static void setScreenToHighscores(final String echo) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				HttpPoster.echo = echo;
				//Settings.game.setScreen(new WorldMapScreen(null, Settings.tracker.isWin));
				Settings.game.setScreen(new HighscoreScreen(Settings.game, echo));
			}
		});
	}

	public static class MyResponseListener implements HttpResponseListener {
		HttpRequest req;
		boolean addToStack;
		String ID;
		
		public MyResponseListener(HttpRequest req, boolean addToStack) {
			this.req = req;
			this.addToStack = addToStack;
		}

		public MyResponseListener(HttpRequest req, boolean addToStack, String ID) {
			this(req, addToStack);
			this.ID = ID;
		}

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			Gdx.app.log("Http Request", "Code: " + httpResponse.getStatus().getStatusCode());
			//Gdx.app.log("Http Request", "Result: " + httpResponse.getResultAsString());
			setScreenToHighscores(httpResponse.getResultAsString());
		}

		@Override
		public void failed(Throwable t) {
			Gdx.app.log("Http Request", "Failed");
			t.printStackTrace();
			if (addToStack)
				writeToStack();
			setScreenToHighscores("Error:\nNo internet connection.");
		}

		@Override
		public void cancelled() {
			Gdx.app.log("Http Request", "cancelled");
		}
		
		private void writeToStack() {
			Json json = new Json();
			String id = String.valueOf(req.getContent().hashCode());
			logStack.putString(id, json.toJson(req));
			Gdx.app.log("Put log into stack, ID: ", id);
			logStack.flush();
		}
	}
}
