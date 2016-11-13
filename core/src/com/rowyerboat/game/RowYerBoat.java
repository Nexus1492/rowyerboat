package com.rowyerboat.game;

import com.badlogic.gdx.Game;
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
		System.out.println(Transverter.gameToGPS(new Vector2(0, 0)));
		System.out.println(Transverter.gameToGPS(new Vector2(9100, 0)));
		System.out.println(Transverter.gameToGPS(new Vector2(9100, 9600)));
		System.out.println(Transverter.gameToGPS(new Vector2(0, 9600)));

		setScreen(new IntroScreen(this));
	}

	public void dispose() {
		super.dispose();
		//AssetLoader.dispose();
	}
}
