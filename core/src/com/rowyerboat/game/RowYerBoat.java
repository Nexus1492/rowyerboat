package com.rowyerboat.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.rowyerboat.gameworld.Campaign;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.HttpPoster;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.screens.*;

public class RowYerBoat extends Game {
	java.util.Stack<Screen> screens;
	java.util.Stack<InputProcessor> inputs;
	
	@Override
	public void create () {
		screens = new java.util.Stack<Screen>();
		inputs = new java.util.Stack<InputProcessor>();

		AssetLoader.load();
		Mission.init();
		Settings.init(this);
		Campaign.init();

		setScreen(new IntroScreen(this));
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
		Gdx.input.setInputProcessor(inputs.pop());
		super.setScreen(screens.pop());
	}
	
	@Override
	public void setScreen(Screen screen) {
		setScreen(screen, true);
	}
	
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
