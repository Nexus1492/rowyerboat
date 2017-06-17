package com.rowyerboat.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class AssetLoader {
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
	
	public static void load() {
		arrow = new Texture(Gdx.files.internal("arrow.png"));
		
		noise0 = new Texture(Gdx.files.internal("noise0.png"));
		noise1 = new Texture(Gdx.files.internal("noise1.png"));
		noise2 = new Texture(Gdx.files.internal("noise2.png"));
		noise3 = new Texture(Gdx.files.internal("noise3.png"));
		
		arrowImage = new Texture(Gdx.files.internal("arrow.png"));
		circularArrowImage = new Texture(Gdx.files.internal("circularArrow.png"));
		
		tick = new Texture(Gdx.files.internal("tick.png"));
		tickTransp = new Texture(Gdx.files.internal("tick_transp.png"));
		
		mapTex = new Texture(Gdx.files.internal("LesserAntilles.png"));
		mapBackground = new Texture(Gdx.files.internal("map_background.png"));
		mapTex_tut0 = new Texture(Gdx.files.internal("Tutorial0_map.png"));
		mapTex_tut1 = new Texture(Gdx.files.internal("Tutorial1_map.png"));
		
		nexusLogo = new Texture(Gdx.files.internal("nexusLogo.png"));
		titleScreen = new Texture(Gdx.files.internal("titleScreen.png"));
		
		paddleSplash = Gdx.audio.newSound(Gdx.files.internal("paddleSplash.wav"));
		
		font = getFont();
		
		gameMusic = Gdx.audio.newMusic(Gdx.files.internal("areito de maguana v1.mp3"));
		gameMusic.setLooping(true);
		
		fx_targetReached = Gdx.audio.newSound(Gdx.files.internal("FX_targetreached.wav"));
		fx_missionAccomplished = Gdx.audio.newSound(Gdx.files.internal("FX_missionaccomplished.wav"));
	}

	public static void dispose() {
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
		fx_missionAccomplished.dispose();
	}
	
	public static BitmapFont getFont() {
		BitmapFont font = new BitmapFont();
		font.getData().scale(0.25f);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return font;
	}
}
