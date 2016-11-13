package com.rowyerboat.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class AssetLoader {
	public static Texture arrow;
	public static Texture noise0, noise1, noise2, noise3;
	public static Texture arrowImage, circularArrowImage;
	
	public static Texture mapTex, mapBackground;
	
	public static Texture nexusLogo, titleScreen;
	
	public static Sound paddleSplash;
	
	public static BitmapFont font;
	
	public static void load() {
		arrow = new Texture(Gdx.files.internal("arrow.png"));
		
		noise0 = new Texture(Gdx.files.internal("noise0.png"));
		noise1 = new Texture(Gdx.files.internal("noise1.png"));
		noise2 = new Texture(Gdx.files.internal("noise2.png"));
		noise3 = new Texture(Gdx.files.internal("noise3.png"));
		
		arrowImage = new Texture(Gdx.files.internal("arrow.png"));
		circularArrowImage = new Texture(Gdx.files.internal("circularArrow.png"));
		
		mapTex = new Texture(Gdx.files.internal("LesserAntilles.png"));
		mapBackground = new Texture(Gdx.files.internal("map_background.png"));
		
		nexusLogo = new Texture(Gdx.files.internal("nexusLogo.png"));
		titleScreen = new Texture(Gdx.files.internal("titleScreen.png"));
		
		paddleSplash = Gdx.audio.newSound(Gdx.files.internal("paddleSplash.wav"));
		
		font = new BitmapFont();
		font.getData().scale(0.25f);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	public static void dispose() {
		arrow.dispose();
		
		noise0.dispose();
		noise1.dispose();
		noise2.dispose();
		noise3.dispose();
		
		arrowImage.dispose();
		circularArrowImage.dispose();
		
		mapTex.dispose();
		mapBackground.dispose();

		nexusLogo.dispose();
		titleScreen.dispose();
		
		paddleSplash.dispose();
		
		font.dispose();
	}
}
