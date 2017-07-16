package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rowyerboat.game.RowYerBoat;
import com.rowyerboat.gameobjects.Boat;
import com.rowyerboat.gameworld.Campaign;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;
import com.rowyerboat.scientific.Transverter;

public class HighscoreScreen implements Screen {
	RowYerBoat game;
	Mission mission;
	
	Stage stage;
	
	public HighscoreScreen(RowYerBoat g, String score1) {
		this(g, score1, null);
	}
	
	public HighscoreScreen(RowYerBoat g, String score1, String score2) {
		this.game = g;
		mission = Settings.getMission();
		
		if (Settings.tracker.isWin)
			updatePersonalHighscore();
		
		stage = new HighscoreStage(score1, score2);
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		stage.act();
		stage.draw();
	}
	
	private void updatePersonalHighscore() {
		float timeTaken = Settings.tracker.timeTaken;
		float recordTime = Settings.highscores.getFloat(Settings.getMission().id + (Settings.useEnergy ? "ON" : "OFF"),
				Float.MAX_VALUE);
		if (recordTime > timeTaken) {
			Settings.highscores.putFloat(Settings.getMission().id + (Settings.useEnergy ? "ON" : "OFF"), timeTaken);
			Settings.highscores.flush();
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}
	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}
	@Override
	public void pause() {
	}
	@Override
	public void resume() {
	}
	@Override
	public void hide() {
	}
	@Override
	public void dispose() {
		stage.dispose();
	}
	
	private class HighscoreStage extends RYBStage {
		
		LabelStyle style;
		
		public HighscoreStage(String score1, String score2) {
			super(new FitViewport(Settings.width, Settings.height), new SpriteBatch());
			style = new LabelStyle(AssetLoader.getFont(), Color.WHITE);
			
			Actor background = new Actor() {
				@Override
				public void draw (Batch batch, float parentAlpha) {
					batch.setColor(Color.WHITE);
					batch.draw(AssetLoader.titleScreen, 0, 0, stage.getWidth(), stage.getHeight());
				}
			};
			background.setPosition(0, 0);
			addActor(background);
			
			float sep = 40f;
			
			Table rightTable = createHighscoreTable(score1);
			Table leftTable;
			if (score2 == null)
				leftTable = createPersonalTable();
			else
				leftTable = createHighscoreTable(score2);
			
			rightTable.top().left().setPosition(sep/2f, 0);
			leftTable.top().right().setPosition(-sep/2f, 0);
			
			Table buttonTable = new Table();
			buttonTable.setPosition(getWidth()/2, getHeight() * 0.35f);
			
			TextButton retryButton = new TextButton("Retry", skin);
			retryButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					game.setScreen(new GameScreen(), false);
				}
			});
			
			final TextButton mapButton = new TextButton("Show Map", skin);
			mapButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					game.setScreen(new WorldMapScreen());
					mapButton.setChecked(false);
				}
			});
			
			TextButton menuButton = new BackButton("Back to Menu", skin);
			
			TextButton missionSelectionButton = new TextButton("Select Mission", skin);
			missionSelectionButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.setMission(Campaign.getCampaign(mission.campaignID)
							.nextMission(Settings.missionID).id);
					game.setScreen(new MissionSelectionScreen(), false);
				}
			});
			
			float padding = 40f;
			buttonTable.add(retryButton.pad(10f)).pad(padding);
			buttonTable.add(mapButton.pad(10f)).pad(padding);
			buttonTable.add(menuButton.pad(10f)).pad(padding);
			buttonTable.add(missionSelectionButton.pad(10f)).pad(padding);
			
			Table masterTable = new Table();
			masterTable.add(leftTable).padRight(10f).center().top();
			masterTable.add(rightTable).padLeft(10f).center().top();
			masterTable.row();
			masterTable.add(buttonTable).colspan(2);
			masterTable.center();
			masterTable.setPosition(getWidth()/2, getHeight()/2);
			addActor(masterTable);

			if (score2 != null || Settings.game.getLastScreen().getClass() != MainScreen.class ||
					Settings.game.getScreen().getClass() == MissionSelectionScreen.class) {
				retryButton.setVisible(false);
				mapButton.setVisible(false);
				missionSelectionButton.setVisible(false);
			}
		}

		/**
		 * Personal (left) table. Will display the following content:
		 * "Personal score of *PLAYER-ID*-*FIRST 4 DIGITS OF USErIDoFFSET*
		 * Mission: *MISSION NAME*
		 * Energy: *ENERGY SETTINGS*
		 * Status: *isWIN?*
		 * Time: *TimeTaken*
		 * Personal record: *PERSONAL RECORD*
		 * 
		 * @return
		 */
		private Table createPersonalTable() {
			boolean win = Settings.tracker.isWin;
			float timeTaken = Settings.tracker.timeTaken;
			float recordTime = Settings.highscores.getFloat(mission.id + (Settings.useEnergy ? "ON" : "OFF"), -1);
			String shortOffset = Settings.userData.getString("userIDoffset");
			
			Table personalscore = new Table();
			personalscore.add(new Label("Personal score of ", style)).left();
			personalscore.add(new Label(String.valueOf(Settings.userID) + "-" 
					+ shortOffset.substring(shortOffset.length() - 4) ,style)).right();
					//+ String.valueOf(Settings.userData.getInteger("userIDoffset") % 10000), style)).right();
			personalscore.row();
			personalscore.add(new Label("Mission: ", style)).left();
			personalscore.add(new Label(mission.name, style)).right();
			personalscore.row();
			/*personalscore.add(new Label("Energy: ", style)).left();
			personalscore.add(new Label(Settings.useEnergy ? "On" : "Off", style)).right();
			personalscore.row();*/
			personalscore.add(new Label("Status: ", style)).left();
			personalscore.add(new Label(win || (recordTime > 0 && timeTaken == 0f) ?
					"Accomplished" : "Failed", style)).right();
			personalscore.row();
			personalscore.add(new Label("Time: ", style)).left();
			personalscore.add(new Label(timeTaken != 0f ? Transverter.secondsToString(timeTaken) : "-", style)).right();
			personalscore.row();
			personalscore.add(new Label("Personal record: ", style)).left();
			personalscore.add(new Label(recordTime > 0 ? Transverter.secondsToString(recordTime) : "-", style)).right();
			return personalscore;
		}
		
		private Table createHighscoreTable(String highscore) {
			Table table = new Table();
			table.add(new Label("Global highscore: ", style)).left();
			table.add(new Label(mission.name, style)).right();
			String[] echo = highscore.split("\n");
			for (int i = 0; i < Math.min(echo.length - echo.length % 2, 10); i+=2) {
				table.row();
				table.add(new Label(echo[i], style)).left();
				String nextEntry;
				if (highscore.contains("Error"))
					nextEntry = echo[i+1];
				else
					nextEntry = Transverter.secondsToString(Float.valueOf(echo[i+1]) % 10000);
				table.add(new Label(nextEntry, style)).right();
			}
			return table;
		}

	}
	
	/*private String debug() {
		Settings.tracker = new Tracker();
		Settings.tracker.isWin = false;
		Settings.tracker.timeTaken = 79;
		return "schnitzel-1257\n" +
				"12.13\n" +
				"fleisch-9835\n" +
				"12.34\n" +
				"hack-1290\n" +
				"13.37\n" +
				"schnitzel-1357\n" +
				"16.02\n" +
				"wurst-8975\n" +
				"19.91\n";
	}*/
}
