package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.HttpPoster;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;

public class MainScreen implements Screen {

	Stage stage;
	Batch batch;
	private Stage mainStage;
	private RegisterStage registerStage;

	NexusActor nexusActor;

	Game game;

	boolean win = false;

	float time = 0;
	Texture nexusLogo;
	Texture background;

	TextButton playButton;

	float timeLastTouched;
	int timesTouched = 0;

	public MainScreen() {
		this.game = Settings.game;

		nexusLogo = AssetLoader.nexusLogo;
		background = AssetLoader.titleScreen;

		nexusActor = new NexusActor();

		mainStage = new MainStage();
		registerStage = new RegisterStage();
		stage = mainStage;

		batch = mainStage.getBatch();

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

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(mainStage.getCamera().combined);
		batch.begin();
		batch.setColor(Color.WHITE);
		batch.draw(background, 0, 0, mainStage.getWidth(), mainStage.getHeight());
		batch.end();

		float threshold = 1f;
		time += delta; // here, time
		nexusActor.update(Math.min(time / threshold, 1));

		stage.draw();
		stage.act();

		if (time > threshold) {
			Gdx.input.setInputProcessor(stage);
			Gdx.input.setCatchBackKey(true);
		}

		if (Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			Gdx.app.exit();
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
		Settings.tracker = new Tracker();
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

	protected void switchStages() {
		if (stage.getClass().equals(MainStage.class))
			stage = registerStage;
		else
			stage = mainStage;
	}

	private class MainStage extends RYBStage {

		public TextButton registerButton;

		public MainStage() {
			super();
			Table masterTable = new Table();

			final TextButton energyButton = new RYBButton("Energy\nON", skin);
			energyButton.setChecked(!Settings.useEnergy);
			energyButton.setText("Energy\n" + (energyButton.isChecked() ? "OFF" : "ON"));
			energyButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					energyButton.setChecked(energyButton.isChecked());
					energyButton.setText("Energy\n" + (energyButton.isChecked() ? "OFF" : "ON"));
					//Settings.updateEnergy(!energyButton.isChecked());
				}
			});
			// grp.addActor(energyButton);

			playButton = new RYBButton("Play", skin);
			playButton.setWidth(100 * 2 + 50);
			playButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					playButton.setChecked(false);
					if (!win) {
						game.setScreen(new MissionSelectionScreen());
					} else
						win = !win;
				}
			});

			final TextButton checkConnectionButton = new TextButton(". . .", skin);
			checkConnectionButton.setPosition(getWidth() - 100 - 25, getHeight() - 50 - 25);
			checkConnectionButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					checkConnectionButton.setText(". . .");
					checkConnectionButton.setDisabled(true);
					checkConnectionButton.setChecked(true);
					HttpPoster.checkConnection(new Runnable() {
						@Override
						public void run() {
							checkConnectionButton.setDisabled(false);
							checkConnectionButton.setChecked(false);
							checkConnectionButton.setText("Online");
						}
					}, new Runnable() {
						@Override
						public void run() {
							checkConnectionButton.setDisabled(false);
							checkConnectionButton.setChecked(false);
							checkConnectionButton.setText("Offline");
						}
					});
				}
			});
			addActor(checkConnectionButton);

			registerButton = new RYBButton("Register Online", skin);
			registerButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					registerButton.setChecked(false);
					HttpRequest req = new HttpRequest(HttpMethods.GET);
					req.setUrl("http://www.nexus1492.eu/boatlogs/getRegisterText.php");
					Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
						@Override
						public void handleHttpResponse(final HttpResponse httpResponse) {
							registerStage.update(httpResponse.getResultAsString());
							switchStages();
						}

						@Override
						public void failed(Throwable t) {
							t.printStackTrace();
						}

						@Override
						public void cancelled() {
						}
						
					});
				}
			});

			masterTable.add(nexusActor).width(nexusLogo.getWidth()).height(nexusLogo.getHeight()).row();
			
			//masterTable.add(playButton.padLeft(5f).padRight(5f)).pad(5f).width(nexusLogo.getWidth()).row();
			masterTable.add(playButton.padLeft(5f).padRight(5f)).pad(5f).width(nexusLogo.getWidth()).row();
			masterTable.add(registerButton.padLeft(5f).padRight(5f)).pad(5f).width(nexusLogo.getWidth());
			
			masterTable.setPosition(getWidth() / 2, getHeight() / 2);
			addActor(masterTable);

			fireButton(checkConnectionButton);
		}

		@Override
		public void draw() {
			super.draw();
			if (!Settings.online) {
				registerButton.setChecked(true);
				registerButton.setTouchable(Touchable.disabled);
			} else {
				registerButton.setChecked(false);
				registerButton.setTouchable(Touchable.enabled);
			}

		}
	}

	public class RegisterStage extends RYBStage {
		String emailString = "";
		TextButton textField;

		public RegisterStage() {
			super();
			final String email = Settings.userData.getString("UserMail", null);
			Table mastertable = new Table();
			
			if (email != null)
				emailString = "You have currently registered the following email adress: " + email +".\n"
						+ "By accepting again and entering a new email adress, this value will be overwritten.";
			textField = new TextField(getWidth() * 0.875f, getHeight() * 0.875f);
			
			final TextButton declineButton = new RYBButton(email == null ? "Decline" : "Back", skin);
			declineButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					declineButton.setChecked(false);
					switchStages();
				}
			});
			
			final TextButton acceptButton = new RYBButton("Accept", skin);
			acceptButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					acceptButton.setChecked(false);
					Gdx.input.getTextInput(new TextInputListener() {
						@Override
						public void input(String text) {
							if (text.matches(".+@.+\\..+")) { // f@o.o
								HttpPoster.registerEmail(text.replace("\r", "").replace("\n", ""));
								Settings.userData.putString("UserMail", text);
								Settings.userData.flush();
								emailString = "You have currently registered the following email adress: "
										+ text +".\nBy accepting again and entering a new email adress, "
										+ "this value will be overwritten.";
								switchStages();
								declineButton.setText("Back");
							} else
								canceled();
						}

						@Override
						public void canceled() {
						}
						
					}, "Enter your email adress", "", "john@doe.com");
				}
			});
			
			mastertable.add(textField.pad(5f)).width(textField.getWidth())
				.height(textField.getHeight()).pad(5f).colspan(2);
			mastertable.row();
			mastertable.add(acceptButton.padLeft(5f).padRight(5f)).padRight(25f).right();
			mastertable.add(declineButton.padLeft(5f).padRight(5f)).padLeft(25f).left();
			
			mastertable.center().setPosition(getWidth()/2, getHeight()/2);
			addActor(mastertable);
		}
		
		public void update(String text) {
			textField.setText(text + emailString);
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