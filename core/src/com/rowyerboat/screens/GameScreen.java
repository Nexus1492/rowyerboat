package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.RYBInputReader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.rendering.GameRenderer;
import com.rowyerboat.scientific.Tracker;

public class GameScreen implements Screen {
	InputMultiplexer gameInput;

	private GameWorld world;
	private GameRenderer renderer;
	
	private boolean doUpdate = false;

	public GameScreen() {
		RYBInputReader input = new RYBInputReader();
		gameInput = new InputMultiplexer();
		gameInput.addProcessor(input.ges);
		gameInput.addProcessor(input);
		Gdx.input.setInputProcessor(gameInput);
		
		Settings.tracker = new Tracker();
		this.world = new GameWorld(this);
		this.renderer = new GameRenderer(world);
		//this.renderer = new DebugWorldScreen(world);
		
		Settings.world = world;
		Settings.renderer = renderer;
		
		input.init(world, renderer);
		
		doUpdate = true;
		
		AssetLoader.gameMusic.play();
	}

	@Override
	public void render(float delta) {
		if (doUpdate) {
			world.update(delta);
			renderer.render(delta);
		}
	}
	
	public void end(boolean isWin) {
		AssetLoader.gameMusic.stop();
		doUpdate = false;
		Settings.getMission().reset();
		Settings.tracker.isWin = isWin;
		Settings.tracker.postPoints();
	}
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(gameInput);
		doUpdate = true;
	}

	@Override
	public void resize(int width, int height) {
		renderer.resize(width, height);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {
		Gdx.app.log("GameScreen", "Hide");
		doUpdate = false;
	}

	@Override
	public void dispose() {
		world.dispose();
		renderer.dispose();
	}

}
