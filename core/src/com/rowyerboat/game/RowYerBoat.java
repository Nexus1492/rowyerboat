package com.rowyerboat.game;

import java.util.Locale;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.TimeUtils;
import com.rowyerboat.gameworld.*;
import com.rowyerboat.helper.*;
import com.rowyerboat.screens.*;
import com.rowyerboat.scientific.*;

public class RowYerBoat extends Game {
	java.util.Stack<Screen> screens;
	java.util.Stack<InputProcessor> inputs;
	
	public Boolean init = null;
	
	@Override
	public void create () {
		screens = new java.util.Stack<Screen>();
		inputs = new java.util.Stack<InputProcessor>();

		Settings.game = this;
		setScreen(new IntroScreen());
	}

	public void init() {
		Gdx.app.log("Initialization", "Started");
		final long start = TimeUtils.nanoTime();
		AssetLoader.init();
		GameMap.init();
		Thread initThread = new Thread() {
			public void run() {
				CurrentData.init();
				Mission.init();
				Transverter.init();
				Campaign.init();
				Settings.init();
			};
		};
		initThread.start();
		try {
			initThread.join();
			Gdx.app.log("Initialization", String.format(Locale.US, "Finished after %f seconds",
					TimeUtils.timeSinceNanos(start)/1000000000.0));
			init = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setScreen(new MainScreen());
	}
	
	public void setLastScreen(Screen scr) {
		this.screen.hide();
		scr.hide();
		this.screen = scr;
	}
	
	public Screen getLastScreen() {
		return screens.peek();
	}
	
	public void returnToLastScreen() {
		super.setScreen(screens.pop());
		Gdx.input.setInputProcessor(inputs.pop());
	}
	
	/**
	 * Calls <code>setScreen(screen, true)</code>
	 */
	@Override
	public void setScreen(Screen screen) {
		setScreen(screen, true);
	}
	
	/**
	 * Set the gamescreen to be screen
	 * @param screen
	 * @param currentScreenReturnable if true, the current screen FROM WHICH this method is called
	 * will be added to the stack
	 */
	public void setScreen(Screen screen, boolean currentScreenReturnable) {
		if (currentScreenReturnable) {
			screens.push(this.screen);
			inputs.push(Gdx.input.getInputProcessor());
		}
		super.setScreen(screen);
	}

	public void dispose() {
		super.dispose();
		AssetLoader.dispose();
	}
}
