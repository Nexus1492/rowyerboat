package com.rowyerboat.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;

public class RYBStage extends Stage {

	protected Skin skin;
	
	public RYBStage() {
		this(new FitViewport(Settings.width, Settings.height), new SpriteBatch());
	}
	
	public RYBStage(FitViewport fitViewport, SpriteBatch spriteBatch) {
		super(fitViewport, spriteBatch);
		skin = new Skin();

		skin.add("default", AssetLoader.getFont());
		
		Pixmap pixmap = new Pixmap(100, 50, Format.RGBA8888);
		pixmap.setColor(0.2f, 0.2f, 1f, 0.5f);
		pixmap.fill();

		skin.add("default", new Texture(pixmap));
		pixmap.dispose();

		// Background of the description field
		pixmap = new Pixmap(1, 1, Format.RGBA8888);
		Color std = Color.DARK_GRAY;
		std.a = 0.5f;
		pixmap.setColor(std);
		pixmap.fill();
		skin.add("descField", new Texture(pixmap));

		TextButtonStyle tbs = new TextButtonStyle();
		tbs.up = skin.newDrawable("default", Color.LIGHT_GRAY);
		tbs.checked = skin.newDrawable("default", Color.DARK_GRAY);
		tbs.down = skin.newDrawable("default", Color.BLACK);
		tbs.font = skin.getFont("default");

		skin.add("default", tbs);

		// desc field style (mainly the height/widht are changed
		tbs = new TextButtonStyle();
		tbs.up = skin.newDrawable("descField", Color.WHITE);
		tbs.font = skin.getFont("default");
		skin.add("descField", tbs);
	}
	
	protected void fireButton(Button btn) {
		InputEvent event = new InputEvent();
		event.setType(Type.touchDown);
		btn.fire(event);
		event = new InputEvent();
		event.setType(Type.touchUp);
		btn.fire(event);
	}
	
	public void dispose() {
		super.dispose();
		skin.dispose();
	}
	
	protected class TextField extends TextButton {

		public TextField(int width, int height) {
			super("", skin, "descField");
			getLabel().setWrap(true);
			setDisabled(true);
			setTouchable(Touchable.disabled);
			setWidth(width);
			setHeight(height);
		}

		public TextField(float width, float height) {
			this((int) width, (int) height);
		}
	}
	
	protected class BackButton extends TextButton {

		public BackButton (String text, Skin skin) {
			this(text, skin.get(TextButtonStyle.class));
			setSkin(skin);
		}
		
		public BackButton(String text, Skin skin, String styleName) {
			this(text, skin.get(styleName, TextButtonStyle.class));
			setSkin(skin);
		}

		public BackButton (String text, TextButtonStyle style) {
			super(text, style);
			addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.game.returnToLastScreen();
				}
			});
		}
	}

}
