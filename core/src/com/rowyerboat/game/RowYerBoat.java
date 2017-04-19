package com.rowyerboat.game;

import com.badlogic.gdx.Game;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.screens.*;

public class RowYerBoat extends Game {
	
	@Override
	public void create () {
		Mission.init();
		Settings.init(this);
		AssetLoader.load();

		setScreen(new IntroScreen(this));
		//setScreen(new HighscoreScreen(this, null));
	}

	public void dispose() {
		super.dispose();
		AssetLoader.dispose();
	}
}
