package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;

/**
 * Simple text-based tutorial following a certain flow to explain the UI.
 * 
 * @author Roman Lamsal
 *
 */
public class TutorialScreen implements Screen {
	Game game;
	Screen lastScreen;

	Stage stage;
	ShapeRenderer shaper;
	
	int state = 0;
	
	Texture bg;
	
	TextButton btn;
	
	public TutorialScreen(Game g, Screen s) {
		game = g;
		lastScreen = s;
		shaper = new ShapeRenderer();
		
		createStage();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		stage.draw();

		shaper.setProjectionMatrix(stage.getCamera().combined);
		shaper.begin(ShapeType.Line);
		shaper.setColor(Color.RED);
		if (0 < state && state < 3)
			fatRect(0, stage.getHeight() - 1, 5 + 300 + 5, -(30 * 2 + 5 - 1), 3); //bars
		else if (2 < state && state < 6)
			fatRect(10, 10, 136, 136 + 5, 3); //compass
		else if (state == 6)
			fatRect(stage.getWidth() - 1, stage.getHeight() - 1, -(60 + 10 - 1), -(5 + 6 * 60 + 5 - 1), 3); //buttons
		shaper.end();
		
		if (state == 8) {
			Settings.userData.putBoolean("tutorialDisplayed", true);
			Settings.userData.flush();
			game.setScreen(lastScreen);
			this.dispose();
		}
		
		if (Gdx.input.justTouched()) {
			state++;
			updateBtn();
		}
	}
	
	private void fatRect(float x, float y, float width, float height, float lineWidth) {
		for (int i = 0; i <= lineWidth; ++i)
			shaper.rect(x + i, y + i, width - i * 2, height - i * 2);
	}
	
	private void createStage() {
		stage = new Stage(new FitViewport(Settings.width, Settings.height));
		bg = new Texture(Gdx.files.internal("tut.png"));
		
		
		Actor tex = new Actor() {
			@Override
			public void draw(Batch batch, float alpha) {
				batch.setColor(Color.WHITE);
				batch.draw(bg, 0, 0, stage.getWidth(), stage.getHeight());
			}
		};
		tex.setPosition(0, 0);
		stage.addActor(tex);
		
		TextButtonStyle tbs = new TextButtonStyle();
		tbs.font = new BitmapFont();
		tbs.font.setColor(Color.BLUE);
		
		btn = new TextButton("Hello and thank you for playing RowYerBoat!\n"
				+ "First some explanations regarding the game.", tbs);
		btn.bottom().left();
		btn.getLabel().setWrap(true);

		btn.setWidth(btn.getWidth() + 10);
		btn.setHeight(btn.getHeight() + 10);
		
		Pixmap map = new Pixmap((int)btn.getLabel().getWidth(), (int)btn.getLabel().getHeight(), Format.RGBA8888);
		map.setColor(Color.BLUE);
		map.fill();
		btn.getStyle().up = new TextureRegionDrawable(new TextureRegion(new Texture(map)));
		map.dispose();
		btn.setPosition((stage.getWidth() - btn.getWidth())/2, (stage.getHeight() - btn.getHeight())/2);
		
		stage.addActor(btn);
	}
	
	private void updateBtn() {
		GlyphLayout layout = new GlyphLayout();
		switch (state) {
		case 1:
			btn.setText("These are your speed and energy bar. "
					+ "The speed bar shows your speed relative to your maximum speed."
					+ "The energy bar shows your remaining energy.");
			btn.setPosition(10, stage.getHeight() - btn.getHeight() - 100);
			btn.setWidth(btn.getWidth() + 100);
			break;
		case 2:
			btn.setText("The faster you are, the worse you are able to maneuver. "
					+ "Also, your energy consumption rises the faster you are. "
					+ "This is indicated by the change in color of your energy bar "
					+ "from yellow (low) to red (high).");
			break;
		case 3:
			btn.setText("This is your compass. The circle around the compass always "
					+ "points in the direction of what you want the most...");
			btn.setPosition(10, 151 + 10);
			break;
		case 4:
			btn.setText("Your next target.");
			break;
		case 5:
			btn.setText("The arrow inside the compass shows the direction of the currents "
					+ "you are inside.");
			break;
		case 6:
			btn.setText("These are some buttons for adjusting the camera, "
					+ "turning debug mode on and off "
					+ "turning the HUD with extra information on and off "
					+ "and showing the map, which also pauses the game.");
			btn.setPosition(stage.getWidth() - btn.getWidth() - 60 - 10 - 10, stage.getHeight() - 75 - 5);
			break;
		case 7:
			btn.setText("Now enjoy! Swipe on the left or right half of the screen "
					+ "to paddle and hold down to brake and turn.");
			btn.setPosition((stage.getWidth() - btn.getWidth())/2, (stage.getHeight() - btn.getHeight())/2);
			break;
		}
		layout.setText(btn.getStyle().font, btn.getText(), Color.WHITE, btn.getWidth(), Align.center, true);
		btn.setHeight(Math.max(layout.height + 10, 39));
		
		Pixmap map = new Pixmap((int)(btn.getLabel().getWidth()), (int)(btn.getLabel().getHeight()), Format.RGBA8888);
		map.setColor(Color.BLUE);
		map.fill();
		btn.getStyle().up = new TextureRegionDrawable(new TextureRegion(new Texture(map)));
		map.dispose();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		bg.dispose();
		stage.dispose();
	}
	
	@Override
	public void show() {

	}

}
