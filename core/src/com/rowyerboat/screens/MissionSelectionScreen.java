package com.rowyerboat.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.gameworld.Mission.MissionID;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Transverter;

public class MissionSelectionScreen implements Screen {
	
	MainScreen lastScreen;

	Stage stage;
	SpriteBatch batch;
 
	Game game;
	
	Viewport viewport;
	
	Texture background;
	
	Mission selectedMission;
	MissionID missionID;
	
	public MissionSelectionScreen(Game g, MainScreen s) {
		game = g;
		lastScreen = s;

		batch = new SpriteBatch();
		viewport = new FitViewport(Settings.width, Settings.height);
		stage = new Stage(viewport, batch);
		
		background = AssetLoader.titleScreen;
		
		selectedMission = Settings.mission;
		missionID = Settings.mission.id;
		
		createStage();
		
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(stage.getCamera().combined);
		batch.begin();
		batch.draw(background, 0, 0, stage.getWidth(), stage.getHeight());
		batch.end();
		
		stage.act();
		stage.draw();
	}
	
	private void createStage() {
		Skin skin = new Skin();
		BitmapFont font = new BitmapFont();
		skin.add("default", font);
		
		Group group = new Group();
		final ButtonGroup<TextButton> bGroup = new ButtonGroup<TextButton>();
		bGroup.setUncheckLast(true);

		Color std = new Color(0.7f, 0.7f, 0.7f, 0.5f);
		std = Color.DARK_GRAY;
		std.a = 0.5f;
		Pixmap pixmap = new Pixmap(100, 50, Format.RGBA8888);
		pixmap.setColor(std);
		pixmap.fill();
		skin.add("mission", new Texture(pixmap));

		int width = Settings.width * 2/3, height = Settings.height/3;
		pixmap = new Pixmap(width, height, Format.RGBA8888);
		pixmap.setColor(std);
		pixmap.fill();
		skin.add("text", new Texture(pixmap));
		
		TextButtonStyle tbs = new TextButtonStyle();
		tbs.up = skin.newDrawable("mission", std);
		tbs.checked = skin.newDrawable("mission", Color.LIGHT_GRAY);
		tbs.font = skin.getFont("default");
		
		final TextButton mission01 = new TextButton("Mission01", tbs);
		final TextButton mission02 = new TextButton("Mission02", tbs);
		final TextButton mission03 = new TextButton("Mission03", tbs);
		final TextButton apply = new TextButton("Apply", tbs);
		final TextButton cancel = new TextButton("Cancel", tbs);
		
		bGroup.add(mission01, mission02, mission03);
		bGroup.uncheckAll();
		
		// inital checked
		switch (missionID) {
		case Pottery:
			mission01.setChecked(true);
			break;
		case JaguarTeeth:
			mission02.setChecked(true);
			break;
		case Placeholder:
			mission03.setChecked(true);
			break;
		}
		
		int misNum = 1;
		mission01.setPosition(0, height - (5 + 50) * misNum++);
		mission02.setPosition(0, height - (5 + 50) * misNum++);
		mission03.setPosition(0, height - (5 + 50) * misNum++);
		apply.setPosition(100 + width/2 - 150, -55);
		cancel.setPosition(100 + width/2 + 50, -55);

		group.addActor(mission01);
		group.addActor(mission02);
		group.addActor(mission03);
		
		group.addActor(apply);
		group.addActor(cancel);
		
		tbs = new TextButtonStyle();
		tbs.up = skin.newDrawable("text", Color.LIGHT_GRAY);
		tbs.font = skin.getFont("default");
		final TextButton textField = new TextButton("", tbs);
		
		textField.getLabel().setWrap(true);
		textField.setDisabled(true);
		
		mission01.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				bGroup.uncheckAll();
				mission01.setChecked(true);
				missionID = MissionID.Pottery;
				textField.setText(Mission.getDesc(missionID));
			}
		});
		mission02.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				bGroup.uncheckAll();
				mission02.setChecked(true);
				missionID = MissionID.JaguarTeeth;
				textField.setText(Mission.getDesc(missionID));
			}
		});
		mission03.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				bGroup.uncheckAll();
				mission03.setChecked(true);
				missionID = MissionID.Placeholder;
				textField.setText(Mission.getDesc(missionID));
			}
		});
		apply.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Settings.updateMission(new Mission(missionID));
				game.setScreen(lastScreen);
			}
		});
		cancel.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.setScreen(lastScreen);
			}
		});
		
		textField.setPosition(100, 0);
		group.addActor(textField);
		
		group.setPosition((stage.getWidth() - width)/2 - 100, (stage.getHeight() - height)/2 + 50);
		
		stage.addActor(group);
		
		textField.setText(Mission.getDesc(missionID));
	}

	/*public static Mission mission01() {
		Settings.initialBoatPos = Transverter.textureToGame(new Vector2(449, 275), true);
		Settings.initialBoatDir = new Vector2(1, -1);
		Mission mis = new Mission("Jaguar Teeth");
		mis.addTargets(new Location("target0", Transverter.textureToGame(new Vector2(447, 372), true)),
				new Location("target1", Transverter.textureToGame(new Vector2(551, 384), true)),
				new Location("target2", Transverter.textureToGame(new Vector2(449, 275), true))
				);
		return mis;
	}*/

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
}
