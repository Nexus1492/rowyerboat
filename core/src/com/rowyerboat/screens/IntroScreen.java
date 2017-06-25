package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
	
	public IntroScreen() {
		game = Settings.game;
		
		batch = new SpriteBatch();
		font = AssetLoader.getFont();
		
		Table masterTable = new Table();
		
		stage = new RYBStage(new FitViewport(Settings.width, Settings.height), batch);
		Image logo = new Image(new Texture(Gdx.files.internal("nexusLogo.png")));
		masterTable.add(logo).width(logo.getWidth()).height(logo.getHeight()).row();
		
		String str = "This game was developed as part of the ERC-Synergy Project NEXUS1492. "
				+ "By using it you agree that your User-ID and the tracks of your boat are recorded "
				+ "and used for research purposes within the project.\n"
				+ "Music courtesy of Boynayel.\n"
				+ "Tap to agree.";
		TextButtonStyle tbs = new TextButtonStyle();
		tbs.font = AssetLoader.getFont();
		
		TextButton text = new TextButton(str, tbs);
		text.bottom().right();
		text.getLabel().setWrap(true);
		masterTable.add(text).width(stage.getWidth() - 100);
		masterTable.center().setPosition(stage.getWidth()/2, stage.getHeight()/2);

		stage.addActor(masterTable);
		Gdx.input.setCatchBackKey(true);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		stage.draw();

		switch (state){
		case 0: //showDisclaimer
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
		case 3: //blackout the screen
			Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
			state++;
			break;
		case 4: //switch to loading
			Settings.game.init();
			break;
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}

	@Override
	public void show() {
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
		stage.dispose();
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
