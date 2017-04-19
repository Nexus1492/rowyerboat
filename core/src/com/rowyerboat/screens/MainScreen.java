package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.HttpPoster;
import com.rowyerboat.helper.Settings;

public class MainScreen implements Screen {

	Stage stage;
	Batch batch;

	NexusActor nexusActor;

	Game game;

	boolean win = false;

	float time = 0;
	float checkConnectionTimeout = 0;
	Texture nexusLogo;
	Texture background;

	BitmapFont font;

	TextButton missionButton;
	TextButton checkConnectionButton;

	float timeLastTouched;
	int timesTouched = 0;

	public MainScreen(Game g) {
		this.game = g;

		nexusLogo = AssetLoader.nexusLogo;
		background = AssetLoader.titleScreen;
		font = AssetLoader.font;

		nexusActor = new NexusActor();

		stage = new MainStage();
		batch = stage.getBatch();

		Gdx.input.setCatchBackKey(true);
	}

	public void render(float delta) {
		if (Gdx.input.isKeyJustPressed(Keys.ENTER)
				|| (Gdx.input.justTouched() ? Gdx.input.getX() < 40 && Gdx.input.getY() < 40 : false)) {
			timeLastTouched = time;
			timesTouched++;
		}
		if (time > timeLastTouched + 1f)
			timesTouched = 0;
		if (timesTouched > 3) {
			timesTouched = 0;
			Gdx.app.log("Highscores", "All highscores deleted.");
			for (String key : Settings.highscores.get().keySet()) {
				Settings.highscores.remove(key);
			}
			Settings.highscores.flush();
		}
		missionButton.setText("Mission: " + Settings.mission.name);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(stage.getCamera().combined);
		batch.begin();
		batch.setColor(Color.WHITE);
		batch.draw(background, 0, 0, stage.getWidth(), stage.getHeight());
		batch.end();

		float threshold = 1f;
		time += delta; // here, time
		nexusActor.update(Math.min(time / threshold, 1));

		stage.draw();
		stage.act();

		if (checkConnectionButton.isChecked() && time > checkConnectionTimeout) {
			checkConnectionButton.setText(Settings.online ? "Online" : "Offline");
			checkConnectionButton.setChecked(false);
			checkConnectionButton.setDisabled(false);
		}

		if (time > threshold) {
			Gdx.input.setInputProcessor(stage);
			Gdx.input.setCatchBackKey(true);
		}

		if (Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.BACKSPACE))
			game.setScreen(new TutorialScreen(game, this));
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	public void show() {
		if (Settings.online)
			HttpPoster.checkStack();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	private class MainStage extends RWYStage {
		
		public MainStage() {
			super();
			Group grp = new Group();
			// Create a button with the "default" TextButtonStyle. A 3rd parameter
			// can be used to specify a name other than "default".
			TextButton playButton = new TextButton("Play", tbs);
			playButton.setPosition(nexusActor.width / 2 - 125, 0);
			playButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (!win) {
						/*
						 * switch (Settings.mission.id) { default: case Pottery:
						 * case JaguarTeeth: Settings.map = new
						 * GameMap(TimeUtils.millis(), MapID.lesserAntilles); break;
						 * case Placeholder: Settings.map = new
						 * GameMap(TimeUtils.millis(), MapID.caribbean); break; }
						 */
						Settings.mission.reset();
						game.setScreen(new GameScreen(game));
					} else
						win = !win;
				}
			});
			grp.addActor(playButton);

			final TextButton energyButton = new TextButton("Energy\nON", tbs);
			energyButton.setPosition(nexusActor.width / 2 + 25, 0);
			energyButton.setChecked(!Settings.useEnergy);
			energyButton.setText("Energy\n" + (energyButton.isChecked() ? "OFF" : "ON"));
			energyButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					energyButton.setChecked(energyButton.isChecked());
					energyButton.setText("Energy\n" + (energyButton.isChecked() ? "OFF" : "ON"));
					Settings.updateEnergy(!energyButton.isChecked());
				}
			});
			grp.addActor(energyButton);

			missionButton = new TextButton("", tbs);
			missionButton.setWidth(100 * 2 + 50);
			missionButton.setPosition(playButton.getX(), -55);
			missionButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					missionButton.setChecked(false);
					if (!win) {
						game.setScreen(new MissionSelectionScreen(game, (MainScreen) game.getScreen()));
					} else
						win = !win;
				}
			});
			grp.addActor(missionButton);

			checkConnectionButton = new TextButton(Settings.online ? "Online" : "Offline", tbs);
			checkConnectionButton.setPosition(getWidth() - 100 - 25, getHeight() - 50 - 25);
			checkConnectionButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (time > checkConnectionTimeout) {
						checkConnectionButton.setText(". . .");
						HttpPoster.checkConnection();
						checkConnectionTimeout = time + 3;
						checkConnectionButton.setDisabled(true);
					}
				}
			});
			addActor(checkConnectionButton);

			nexusActor.setPosition(0, 25);
			grp.addActor(nexusActor);
			grp.setPosition((getWidth() - nexusActor.width) / 2, 120);

			addActor(grp);
		}
		
	}

	protected class NexusActor extends Actor {
		float alpha = 0;
		float width = nexusLogo.getWidth();
		float height = nexusLogo.getHeight();

		@Override
		public void draw(Batch batch, float parentAlpha) {
			batch.setColor(alpha, alpha, alpha, alpha);
			batch.draw(nexusLogo, this.getX(), this.getY());
			batch.setColor(Color.WHITE);
		}

		public void update(float alpha) {
			this.alpha = alpha;
		}
	}
}