package com.rowyerboat.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.rowyerboat.game.RowYerBoat;

public class RowYerBoatDesktop {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		int resFactor = 65;
		config.width = resFactor * 16;
		config.height = resFactor * 9;
		new LwjglApplication(new RowYerBoat(), config);
	}
}
