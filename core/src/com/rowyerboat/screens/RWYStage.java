package com.rowyerboat.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;

public class RWYStage extends Stage {

	protected Skin skin;
	protected TextButtonStyle tbs;
	
	public RWYStage() {
		this(new FitViewport(Settings.width, Settings.height), new SpriteBatch());
	}
	
	public RWYStage(FitViewport fitViewport, SpriteBatch spriteBatch) {
		super(fitViewport, spriteBatch);
		skin = new Skin();
		
		Pixmap pixmap = new Pixmap(100, 50, Format.RGBA8888);
		pixmap.setColor(0.2f, 0.2f, 1f, 0.5f);
		pixmap.fill();

		skin.add("default", new Texture(pixmap));

		BitmapFont bfont = AssetLoader.font;
		skin.add("default", bfont);

		tbs = new TextButtonStyle();
		tbs.up = skin.newDrawable("default", Color.LIGHT_GRAY);
		tbs.checked = skin.newDrawable("default", Color.DARK_GRAY);
		tbs.over = skin.newDrawable("default", Color.BLUE);

		tbs.font = skin.getFont("default");

		skin.add("default", tbs);
	}
	
	public void dispose() {
		super.dispose();
		skin.dispose();
	}

}
