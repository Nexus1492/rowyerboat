package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.HttpPoster;
import com.rowyerboat.helper.Settings;

/**
 * Is called everytime the game is started.
 * Upon the first start of the game, the {@link TutorialScreen} is displayed.
 * 
 * @author Roman Lamsal
 *
 */
public class IntroScreen implements Screen {
	Game game;
	
	SpriteBatch batch;
	BitmapFont font;
	
	int state = 0;
	
	Stage stage;
	
	public IntroScreen(Game g) {
		game = g;
		
		batch = new SpriteBatch();
		font = AssetLoader.font;
		
		stage = new Stage(new FitViewport(Settings.width, Settings.height), batch);
		Image logo = new Image(AssetLoader.nexusLogo);
		//logo.setColor(0.5f, 0.5f, 0.5f, 0.5f);
		logo.setPosition((stage.getWidth() - logo.getWidth())/2, (stage.getHeight() - logo.getHeight())* 0.67f);
		stage.addActor(logo);
		
		String str = "This game was developed as part of the ERC-Synergy Project NEXUS1492.\n"
				+ "By using it you agree that your User-ID and the tracks of your boat are recorded \n"
				+ "and used for research purposes within the project.\n"
				+ "Tap to agree.";
		TextButtonStyle tbs = new TextButtonStyle();
		tbs.font = AssetLoader.font;
		
		TextButton text = new TextButton(str, tbs);
		text.bottom().right();
		text.getLabel().setWrap(true);
		text.setPosition((stage.getWidth() - text.getWidth())/2, (logo.getY() - text.getHeight()));
		stage.addActor(text);
		
		Gdx.input.setCatchBackKey(true);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		switch (state){
		case 0: //showDisclaimer
			stage.draw();
			if (Gdx.input.justTouched()) state++;
			if (Gdx.input.justTouched() && !Settings.userID.equals(""))
				state = 4;
			break;
		case 1: //await username
			MyTextInputListener listener = new MyTextInputListener();
			Gdx.input.getTextInput(listener, "Please enter your name", "", "");
			state++;
			break;
		case 2: //do nothing and wait for input
			break;
		case 3: //show tutorial
			state++;
			if (!Settings.userData.getBoolean("tutorialDisplayed"))
				game.setScreen(new TutorialScreen(game, this));
			break;
		case 4: //switch to game
			game.setScreen(new MainScreen(game));
			break;
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}

	@Override
	public void show() {
		HttpPoster.checkConnection();
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
		//batch.dispose();
	}

	public class MyTextInputListener implements TextInputListener {
		@Override
		public void input(String text) {
			if (text.equals(""))
				canceled();
			else {
				Gdx.app.log("UserID", text);
				Settings.userID = text;
				Settings.userData.putString("userID", text);
				Settings.userData.flush();
				state++;
			}
		}

		@Override
		public void canceled() {
			state--;
		}
			
	};
}
