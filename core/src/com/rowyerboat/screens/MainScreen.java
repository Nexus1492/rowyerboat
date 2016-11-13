package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.gameworld.GameMap;
import com.rowyerboat.gameworld.GameMap.MapID;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Transverter;
 
public class MainScreen implements Screen {
	
	Skin skin;
	Stage stage;
	SpriteBatch batch;
	
	NexusActor nexusActor;
 
	Game game;
	
	Viewport viewport;
	
	boolean win = false;
	
	float time = 0;
	Texture nexusLogo;
	Texture background;
	
	BitmapFont font;
	
	TextButton missionButton;
	
	float timeLastTouched;
	int timesTouched = 0;
	
	public MainScreen(Game g){
		this.game = g;
		
		nexusLogo = AssetLoader.nexusLogo;
		background = AssetLoader.titleScreen;
		font = AssetLoader.font;
		
		nexusActor = new NexusActor();
		
		create();
	}
	
	public void render (float delta) {
		if (Gdx.input.isKeyJustPressed(Keys.ENTER) ||
				(Gdx.input.justTouched() ? Gdx.input.getX() < 40 && Gdx.input.getY() < 40 : false)) {
			timeLastTouched = time;
			timesTouched++;
		}
		if (time > timeLastTouched + 1f)
			timesTouched = 0;
		if (timesTouched > 3) {
			timesTouched = 0;
			Gdx.app.log("Highscores", "All highscores deleted.");
			Settings.highscores.remove("Mission01OFF");
			Settings.highscores.remove("Mission01ON");
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
		time += delta;
		nexusActor.update(Math.min(time/threshold, 1));
		
		stage.draw();
		stage.act();
		
		if (time > threshold)
			Gdx.input.setInputProcessor(stage);
	}
	
	public void create(){
		batch = new SpriteBatch();
		viewport = new FitViewport(Settings.width, Settings.height);
		stage = new Stage(viewport, batch);
		
		Group grp = new Group();
 
		// A skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but strongly
		// recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
		skin = new Skin();
		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(100, 50, Format.RGBA8888);
		pixmap.setColor(0.2f, 0.2f, 1f, 0.5f);
		pixmap.fill();
 
		skin.add("blue", new Texture(pixmap));
 
		// Store the default libgdx font under the name "default".
		BitmapFont bfont = AssetLoader.font;
		//bfont.scale(1);
		skin.add("default", bfont);
 
		// Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("blue", Color.LIGHT_GRAY);
		textButtonStyle.down = skin.newDrawable("blue", Color.BLUE);
		textButtonStyle.checked = skin.newDrawable("blue", Color.DARK_GRAY);
		textButtonStyle.over = skin.newDrawable("blue", Color.BLUE);
 
		textButtonStyle.font = skin.getFont("default");
 
		skin.add("default", textButtonStyle);
 
		// Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
		TextButton playButton = new TextButton("Play", textButtonStyle);
		playButton.setPosition(nexusActor.width/2 - 125, 0);
		playButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!win) {
					Settings.map = new GameMap(TimeUtils.millis(), MapID.lesserAntilles);
					Settings.mission.reset();
					game.setScreen(new GameScreen(game));
				} else
					win = !win;
			}
		});
		grp.addActor(playButton);

		final TextButton energyButton = new TextButton("Energy\nON", textButtonStyle);
		energyButton.setPosition(nexusActor.width/2 + 25, 0);
		energyButton.setChecked(!Settings.useEnergy);
		energyButton.setText("Energy\n" + (energyButton.isChecked() ? "OFF" : "ON"));
		energyButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				energyButton.setChecked(energyButton.isChecked());
				energyButton.setText("Energy\n" + (energyButton.isChecked() ? "OFF" : "ON"));
				Settings.updateEnergy(!energyButton.isChecked());
			}
		});
		grp.addActor(energyButton);
		
		missionButton = new TextButton("", textButtonStyle);
		missionButton.setWidth(100 * 2 + 50);
		missionButton.setPosition(playButton.getX(), -55);
		missionButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				missionButton.setChecked(false);
				if (!win) {
					game.setScreen(new MissionSelectionScreen(game, (MainScreen)game.getScreen()));
				} else
					win = !win;
			}
		});
		grp.addActor(missionButton);
		
		nexusActor.setPosition(0, 25);
		grp.addActor(nexusActor);
		grp.setPosition((stage.getWidth() - nexusActor.width)/2, 120);
		
		stage.addActor(grp);
	}

	@Override
	public void resize (int width, int height) {
		viewport.update(width, height);
	}
	
	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}
 
	@Override
	public void show() {
 
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
	
	public class NexusActor extends Actor {
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