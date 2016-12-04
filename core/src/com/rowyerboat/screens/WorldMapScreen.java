package com.rowyerboat.screens;

import java.io.IOException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Transverter;

/**
 * Screen displaying a map of the gameworld.
 * Handles the logic to display certain shapes on the map, like the boat's path.
 * Temporarily unbinds all controls in order to avoid complications.
 * Pressing a button/touching the screen resets the map back to its former {@link InputProcessor}
 * and {@link Screen}
 * 
 * @author Roman Lamsal
 * 
 */
public class WorldMapScreen implements Screen {
	SpriteBatch batch;
	ShapeRenderer shaper;
	BitmapFont font;
	
	Boolean isWin;
	
	Game game;
	Screen lastScreen;
	InputProcessor lastInput;
	
	float time = 0;
	
	final Texture worldMap;
	final Texture background;
	
	float x, y;
	float width, height;

	Stage stage;
	Viewport viewport;
	
	String timeTakenString;
	String recordString;
	
	public WorldMapScreen (Screen s) {
		this(s, null);
	}
	
	public WorldMapScreen (Screen s, Boolean b) {
		batch = new SpriteBatch();
		shaper = new ShapeRenderer();
		shaper.setColor(Color.GREEN);
		font = new BitmapFont();
		isWin = b;
		
		game = Settings.game;
		lastScreen = s;
		lastInput = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(null);
		
		worldMap = AssetLoader.mapTex;
		background = AssetLoader.mapBackground;
		
		viewport = new FitViewport(Settings.width, Settings.height);
		
		height = viewport.getWorldHeight();
		width = ((float)worldMap.getWidth() / (float)worldMap.getHeight()) * height;
		x = viewport.getWorldWidth()/2 - width/2;
		y = 0f;

		if (isWin != null ? isWin : false) {
			float timeTaken = Settings.tracker.timeTaken;
			timeTakenString = Transverter.secondsToString(timeTaken);
			float recordTime = Settings.highscores.getFloat(Settings.mission.id + (Settings.useEnergy ? "ON" : "OFF"),
					Float.MAX_VALUE);
			if (recordTime > timeTaken) {
				Settings.highscores.putFloat(Settings.mission.id + (Settings.useEnergy ? "ON" : "OFF"), timeTaken);
				Settings.highscores.flush();
				recordTime = timeTaken;
			}
			recordString = Transverter.secondsToString(recordTime);
		}
		
		// unbind controls
		if (isWin != null) {
			Settings.tracker.postPoints();
		}

		createStage();
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		time += delta;
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		stage.act();
		stage.draw();

		if (time > 1 && (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(-1))) {
			Gdx.input.setInputProcessor(lastInput);
			game.setScreen(lastScreen);
		}
		
		Array<Vector2> pts = Settings.tracker.getPoints();
		Vector2 vec = Transverter.gameToTexture(pts.get(0), width, height).add(x, y);
		Vector2 vec2;
		
		shaper.setProjectionMatrix(stage.getCamera().combined);
		shaper.begin(ShapeType.Line);
		// render mapborders
		shaper.setColor(Color.WHITE);
		shaper.rect(x, y, width, height-1);
		// render points as BLUE line
		shaper.setColor(Color.BLUE);
		for (int i = 1; i < pts.size; i += Math.max(1, pts.size/1000)) {
			vec2 = Transverter.gameToTexture(pts.get(i), width, height).add(x, y);
			shaper.line(vec, vec2);
			vec = vec2.cpy();
			shaper.circle(vec.x, vec.y, 1f);
		}
		// render boat position
		shaper.circle(vec.x, vec.y, 5f);
		// render reached targets
		for (int i = 0; i < Settings.mission.targetSize(); ++i) {
			Vector3 vec3 = Settings.tracker.targetsReached[i];
			shaper.setColor(Settings.tracker.targetsReached[i].z > 0 ? Color.GREEN : Color.RED);
			vec = Transverter.gameToTexture(new Vector2(vec3.x, vec3.y), width, height).add(x, y);
			shaper.circle(vec.x, vec.y, 1f);
			if (i == Settings.tracker.targetsReachedPointer)
				shaper.circle(vec.x, vec.y, 3);
			shaper.circle(vec.x, vec.y, 5f);
		}
		// render unreached targets
		shaper.end();
	}
	
	private void createStage() {
		stage = new Stage(viewport, batch);
		Actor map = new Actor() {
			@Override
			public void draw (Batch batch, float parentAlpha) {
				batch.draw(background, x, y, width, height);
				batch.setColor(Color.ORANGE);
				batch.draw(worldMap, x, y, width, height);
				batch.setColor(Color.WHITE);
			}
		};
		map.setPosition(x, y);
		stage.addActor(map);
		
		/*Actor mapBorder = new Actor() {
			@Override
			public void draw (Batch batch, float parentAlpha) {
				batch.end();
				
				Array<Vector2> pts = Settings.tracker.getPoints();
				Vector2 vec = Transverter.gameToTexture(pts.get(0), width, height).add(x, y), vec2;
				
				shaper.setProjectionMatrix(stage.getCamera().combined);
				shaper.begin(ShapeType.Line);
				shaper.setColor(Color.WHITE);
				shaper.rect(this.getX(), this.getY(), width, height-1);
				shaper.setColor(Color.BLUE);
				for (int i = 1; i < pts.size; ++i) {
					vec2 = Transverter.gameToTexture(pts.get(i), width, height);
					shaper.line(vec, vec2);
					vec = vec2.cpy();
					shaper.circle(vec.x, vec.y, 1f);
				}
				shaper.circle(vec.x, vec.y, 5f);
				shaper.end();
				
				batch.begin();
			}
		};
		mapBorder.setPosition(x, y);
		stage.addActor(mapBorder);*/
		
		if (isWin != null) {
			Table table = new Table();
			Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
			if (isWin) {
				table.add(new Label("Mission accomplished!", style)).right().colspan(2);
				table.row();
				table.add(new Label("Time Taken:", style)).left();
				table.add(new Label(timeTakenString, style)).right();
				table.row();
				table.add(new Label("Record:", style)).left();
				table.add(new Label(recordString, style)).right();
			} else
				table.add(new Label("You failed.", style)).right().colspan(2);
			
			//table.left();
			table.setPosition(x/2, stage.getHeight()/2);
			
			stage.addActor(table);
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
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
		
	}

}
