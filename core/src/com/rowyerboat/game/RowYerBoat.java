package com.rowyerboat.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Transverter;
import com.rowyerboat.screens.*;

public class RowYerBoat extends Game {
	
	@Override
	public void create () {
		Settings.init(this);
		AssetLoader.load();

		setScreen(new IntroScreen(this));
	}

	public void dispose() {
		super.dispose();
		AssetLoader.dispose();
	}
}
