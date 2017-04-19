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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rowyerboat.gameobjects.Boat;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;
import com.rowyerboat.scientific.Transverter;

public class HighscoreScreen implements Screen {
	Game game;
	
	Stage stage;
	
	public HighscoreScreen(Game g, String scores) {
		this.game = g;
		
		if (Settings.tracker.isWin)
			updatePersonalHighscore();
		
System.out.println("ECHO: " + scores);
		
		stage = new HighscoreStage(scores);
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
		float recordTime = Settings.highscores.getFloat(Settings.mission.id + (Settings.useEnergy ? "ON" : "OFF"),
				Float.MAX_VALUE);
		if (recordTime > timeTaken) {
			Settings.highscores.putFloat(Settings.mission.id + (Settings.useEnergy ? "ON" : "OFF"), timeTaken);
			Settings.highscores.flush();
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}
	@Override
	public void show() {
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
		stage.getBatch().dispose();
	}
	
	private class HighscoreStage extends RWYStage {
		
		public HighscoreStage(String scores) {
			super(new FitViewport(Settings.width, Settings.height), new SpriteBatch());
			
			Actor background = new Actor() {
				@Override
				public void draw (Batch batch, float parentAlpha) {
					batch.setColor(Color.WHITE);
					batch.draw(AssetLoader.titleScreen, 0, 0, stage.getWidth(), stage.getHeight());
				}
			};
			background.setPosition(0, 0);
			addActor(background);
			
			boolean win = Settings.tracker.isWin;

			Label.LabelStyle style = new Label.LabelStyle(AssetLoader.font, Color.WHITE);
			
			Group scoreGroup = new Group();
			scoreGroup.setPosition(getWidth()/2, getHeight() * 0.75f);
			float sep = 40f;
			addActor(scoreGroup);
			
			Table personalscore = new Table();
			personalscore.add(new Label("Personal score of ", style)).left();
			personalscore.add(new Label(String.valueOf(Settings.userID)
					+ String.valueOf(Settings.userData.getInteger("userIDoffset") % 10000), style)).right();
			personalscore.row();
			personalscore.add(new Label("Mission: ", style)).left();
			personalscore.add(new Label(Settings.mission.name, style)).right();
			personalscore.row();
			personalscore.add(new Label("Energy: ", style)).left();
			personalscore.add(new Label(Settings.useEnergy ? "On" : "Off", style)).right();
			personalscore.row();
			personalscore.add(new Label("Status: ", style)).left();
			personalscore.add(new Label(win ? "Accomplished" : "Failed", style)).right();
			personalscore.row();
			personalscore.add(new Label("Time: ", style)).left();
			personalscore.add(new Label(Transverter.secondsToString(Settings.tracker.timeTaken), style)).right();
			personalscore.row();
			personalscore.add(new Label("Personal record: ", style)).left();
			float recordTime = Settings.highscores.getFloat(Settings.mission.id + (Settings.useEnergy ? "ON" : "OFF"), -1);
			personalscore.add(new Label(recordTime > 0 ? Transverter.secondsToString(recordTime) : "-", style)).right();

			personalscore.top().right().setPosition(-sep/2f, 0);
			scoreGroup.addActor(personalscore);
			
			
			Table globalscore = new Table();
			globalscore.add(new Label("Global highscore: ", style)).left();
			globalscore.add(new Label(Settings.mission.name, style)).right();
			String[] highscores = scores.split("\n");
			for (int i = 0; i < highscores.length - highscores.length % 2; i+=2) {
				globalscore.row();
				globalscore.add(new Label(highscores[i], style)).left();
				globalscore.add(new Label(Transverter.secondsToString(Float.valueOf(highscores[i+1]) % 10000), style)).right();
			}
			

			globalscore.top().left().setPosition(sep/2f, 0);
			scoreGroup.addActor(globalscore);
			
			Table buttonTable = new Table();
			buttonTable.setPosition(getWidth()/2, getHeight() * 0.35f);
			addActor(buttonTable);
			
			TextButton retryButton = new TextButton("Retry", tbs);
			retryButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.mission.reset();
					game.setScreen(new GameScreen(game));
				}
			});
			
			final TextButton mapButton = new TextButton("Show Map", tbs);
			mapButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					game.setScreen(new WorldMapScreen(game.getScreen()));
					mapButton.setChecked(false);
				}
			});
			
			TextButton menuButton = new TextButton("Back to Main Menu", tbs);
			menuButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					game.setScreen(new MainScreen(game));
				}
			});
			
			float padding = 40f;
			buttonTable.add(retryButton.pad(10f)).pad(padding);
			buttonTable.add(mapButton.pad(10f)).pad(padding);
			buttonTable.add(menuButton.pad(10f)).pad(padding);
		}
		

	}
	
	private String debug() {
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
	}
}
