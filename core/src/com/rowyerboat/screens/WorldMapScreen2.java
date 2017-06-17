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
import com.rowyerboat.helper.HttpPoster;
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
public class WorldMapScreen2 implements Screen {
	SpriteBatch batch;
	ShapeRenderer shaper;
	BitmapFont font;
	
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
	
	public WorldMapScreen2 (Screen s) {
		batch = new SpriteBatch();
		shaper = new ShapeRenderer();
		shaper.setColor(Color.GREEN);
		font = new BitmapFont();
		
		game = Settings.game;
		if (s != null)
			lastScreen = s;
		else
			lastScreen = new MainScreen(game);
		lastInput = Gdx.input.getInputProcessor();
		// unbind controls
		Gdx.input.setInputProcessor(null);
		
		worldMap = AssetLoader.mapTex;
		background = AssetLoader.mapBackground;
		
		viewport = new FitViewport(Settings.width, Settings.height);
		
		height = viewport.getWorldHeight();
		width = ((float)worldMap.getWidth() / (float)worldMap.getHeight()) * height;
		x = viewport.getWorldWidth()/2 - width/2;
		y = 0f;

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

		// after 1 second, accept input
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
		shaper.rect(x, y, width, height - 1);
		// render points as BLUE line
		shaper.setColor(Color.BLUE);
		int offset = Math.max(1, pts.size/1000);
		for (int i = 1; i < pts.size; i += offset) {
			vec2 = Transverter.gameToTexture(pts.get(i), width, height).add(x, y);
			shaper.line(vec, vec2);
			vec = vec2.cpy();
			shaper.circle(vec.x, vec.y, 1f);
			if (i + offset >= pts.size)
				i = pts.size - 1;
		}
		// render boat position
		shaper.circle(vec.x, vec.y, 5f);
		// render reached targets
		for (int i = 0; i < Settings.getMission().targetSize(); ++i) {
			Vector3 vec3 = Settings.tracker.targetsReached[i];
			shaper.setColor(Settings.tracker.targetsReached[i].z > 0 ? Color.GREEN : Color.RED);
			vec = Transverter.gameToTexture(new Vector2(vec3.x, vec3.y), width, height).add(x, y);
			shaper.circle(vec.x, vec.y, 1f);
			if (i == Settings.tracker.targetsReachedPointer)
				shaper.circle(vec.x, vec.y, 3);
			shaper.circle(vec.x, vec.y, 5f);
		}
		if (pts.size < 2) {
			shaper.setColor(Color.BLUE);
			vec = Transverter.gameToTexture(Settings.getMission().initialBoatPos, width, height).add(x, y);
			shaper.circle(vec.x, vec.y, 5f);
		}
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
