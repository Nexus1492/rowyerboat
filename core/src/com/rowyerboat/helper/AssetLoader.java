package com.rowyerboat.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class AssetLoader {
	public static boolean finishedLoading = false;
	
	public static Texture arrow;
	public static Texture noise0, noise1, noise2, noise3;
	public static Texture arrowImage, circularArrowImage;
	
	public static Texture tick, tickTransp;
	
	public static Texture mapTex, mapBackground, mapTex_tut0, mapTex_tut1;
	
	public static Texture nexusLogo, titleScreen;
	
	public static Sound paddleSplash;
	
	private static BitmapFont font;
	
	public static Music gameMusic;
	
	public static Sound fx_targetReached;
	public static Sound fx_missionAccomplished;
	public static Sound fx_buttonClick;
	
	public static AssetManager manager;
	
	private static void loadTextures(String... files) {
		for (String fileName : files)
			manager.load(fileName, Texture.class);
	}
	
	private static void setFields(String[] files, String... fields) {
		for (int i = 0; i < files.length; ++i) {
			try {
				AssetLoader.class.getField(fields[i]).set(Texture.class,
						manager.get(files[i], Texture.class));
			} catch (IllegalArgumentException | IllegalAccessException 
					| NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void init() {
		manager = new AssetManager();
		final String[] files = new String[] {
				"arrow.png", "noise0.png", "noise1.png", "noise2.png", "noise3.png",
				"arrow.png", "circularArrow.png", "tick.png", "tick_transp.png",
				"LesserAntilles_Map.png", "map_background.png", "Tutorial0_map.png",
				"Tutorial1_map.png", "nexusLogo.png", "titleScreen.png"
		};
		loadTextures(files);
		manager.load("areito de maguana v1.mp3", Music.class);
		manager.load("FX_targetreached.wav", Sound.class);
		manager.load("FX_missionaccomplished.wav", Sound.class);
		manager.load("FX_buttonClick.wav", Sound.class);
		manager.load("paddleSplash.wav", Sound.class);
		manager.finishLoading();
		
		new Thread() {
			public void run() {
				setFields(files, "arrow", "noise0", "noise1", "noise2", "noise3", "arrowImage", "circularArrowImage",
						"tick", "tickTransp", "mapTex", "mapBackground", "mapTex_tut0", "mapTex_tut1",
						"nexusLogo", "titleScreen");
				gameMusic = manager.get("areito de maguana v1.mp3", Music.class);
				gameMusic.setLooping(true);
				fx_targetReached = manager.get("FX_targetreached.wav", Sound.class);
				fx_missionAccomplished = manager.get("FX_missionaccomplished.wav", Sound.class);
				fx_buttonClick = manager.get("FX_buttonClick.wav", Sound.class);
				paddleSplash = manager.get("paddleSplash.wav", Sound.class);
				finishedLoading = true;
				};
		}.start();
		
		font = getFont();
	}

	public static void dispose() {
		manager.dispose();
		
		/*if (!finishedLoading)
			return;

		arrow.dispose();
		
		noise0.dispose();
		noise1.dispose();
		noise2.dispose();
		noise3.dispose();
		
		arrowImage.dispose();
		circularArrowImage.dispose();
		
		tick.dispose();
		tickTransp.dispose();
		
		mapTex.dispose();
		mapBackground.dispose();
		mapTex_tut0.dispose();
		mapTex_tut1.dispose();

		nexusLogo.dispose();
		titleScreen.dispose();
		
		paddleSplash.dispose();
		
		font.dispose();
		
		gameMusic.dispose();
		fx_targetReached.dispose();
		fx_missionAccomplished.dispose();*/
	}
	
	public static BitmapFont getFont() {
		BitmapFont font = new BitmapFont(Gdx.files.internal("RYBfont.fnt"));
		font.getData().scale(0.25f);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return font;
	}
}
