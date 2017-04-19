package com.rowyerboat.screens;

import javax.swing.GroupLayout.Alignment;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.gameworld.Mission.MissionID;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;

public class MissionSelectionScreen implements Screen {
	
	MainScreen lastScreen;

	Stage stage;
	Batch batch;
 
	Game game;
	
	Texture background;
	
	Mission lastlySelectedMission;
	MissionID missionID;
	
	public MissionSelectionScreen(Game g, MainScreen s) {
		game = g;
		lastScreen = s;
		lastlySelectedMission = Settings.mission;
		missionID = Settings.mission.id;
		
		stage = new MissionSelectionStage();
		batch = stage.getBatch();
		
		background = AssetLoader.titleScreen;
		
		
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
	public void show() {
	}

	@Override
	public void dispose() {
		stage.dispose();
	}
	
	private class MissionSelectionStage extends RWYStage {
		
		private ButtonGroup<TextButton> missionButtonUncheckGroup;
		private TextButton descriptionField;
		
		public MissionSelectionStage() {
			super();
			//Color std = new Color(0.7f, 0.7f, 0.7f, 0.5f);
			Color std = Color.DARK_GRAY;
			std.a = 0.5f;
			Pixmap pixmap = new Pixmap(100, 50, Format.RGBA8888);
			pixmap.setColor(std);
			pixmap.fill();
			skin.add("mission", new Texture(pixmap));

			// Background of the description field
			int descFieldWidth = Settings.width * 2/3, descFieldHeight = Settings.height/3 + 20; //TODO rubbish
			pixmap = new Pixmap(descFieldWidth, descFieldHeight, Format.RGBA8888);
			pixmap.setColor(std);
			pixmap.fill();
			skin.add("text", new Texture(pixmap));
			
			tbs = new TextButtonStyle();
			tbs.up = skin.newDrawable("mission", std);
			tbs.checked = skin.newDrawable("mission", Color.LIGHT_GRAY);
			tbs.font = skin.getFont("default");
			
			final TextButton campaign01 = new TextButton("Campaign01", tbs);
			final TextButton campaign02 = new TextButton("Campaign02", tbs);
			final TextButton mission01 = new TextButton("Mission01", tbs);
			final TextButton mission02 = new TextButton("Mission02", tbs);
			final TextButton mission03 = new TextButton("Mission03", tbs);
			final TextButton apply = new TextButton("Apply", tbs);
			final TextButton cancel = new TextButton("Cancel", tbs);
			final TextButton showMap = new TextButton("Show on map", tbs);
			
			missionButtonUncheckGroup = new ButtonGroup<TextButton>();
			missionButtonUncheckGroup.setUncheckLast(true);
			missionButtonUncheckGroup.add(mission01, mission02, mission03);
			missionButtonUncheckGroup.uncheckAll();
			
			ButtonGroup<TextButton> campaignButtonUncheckGroup = new ButtonGroup<TextButton>();
			campaignButtonUncheckGroup.setUncheckLast(true);
			campaignButtonUncheckGroup.add(campaign01, campaign02);
			campaignButtonUncheckGroup.uncheckAll();
			
			tbs = new TextButtonStyle();
			tbs.up = skin.newDrawable("text", Color.LIGHT_GRAY);
			tbs.font = skin.getFont("default");

			descriptionField = new TextButton("", tbs);
			descriptionField.getLabel().setWrap(true);
			descriptionField.setDisabled(true);
			descriptionField.setPosition(100, 0);
			
			setMissionButtonListener(mission01, MissionID.Pottery);
			setMissionButtonListener(mission02, MissionID.JaguarTeeth);
			setMissionButtonListener(mission03, MissionID.Placeholder);
			apply.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					game.setScreen(lastScreen);
				}
			});
			cancel.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.updateMission(lastlySelectedMission.id);
					game.setScreen(lastScreen);
				}
			});
			showMap.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.tracker = new Tracker();
					game.setScreen(new WorldMapScreen(game.getScreen()));
					showMap.setChecked(false);
				}
			});

			float buttonPad = 2f;
			float missionButtonGroupWidth = 100 + buttonPad;
			
			Table campaignButtonGroup = new Table();
			campaignButtonGroup.add(campaign01.pad(buttonPad)).pad(buttonPad).padBottom(0f).padLeft(10f);
			campaignButtonGroup.add(campaign02.pad(buttonPad)).pad(buttonPad).padBottom(0f);
			campaignButtonGroup.bottom().left();

			final Table missionButtonGroup1 = new Table();
			missionButtonGroup1.add(mission01).pad(buttonPad).padRight(0).padTop(10f);
			missionButtonGroup1.row();
			missionButtonGroup1.add(mission02).pad(buttonPad).padRight(0);
			missionButtonGroup1.row();
			missionButtonGroup1.add(mission03).pad(buttonPad).padRight(0);
			missionButtonGroup1.top().right().setPosition(0, 0);
			
			final Table missionButtonGroup2 = new Table();
			missionButtonGroup2.add(mission03).pad(buttonPad).padRight(0).padTop(10f);
			missionButtonGroup2.top().right().setPosition(0, 0);
			missionButtonGroup2.setVisible(false);

			WidgetGroup campaignMissions = new WidgetGroup();
			campaignMissions.setWidth(missionButtonGroupWidth);
			campaignMissions.addActor(missionButtonGroup1);
			campaignMissions.addActor(missionButtonGroup2);
			campaignMissions.setWidth(missionButtonGroupWidth);
			
			Table controlButtonGroup = new Table();
			float controlButtonPad = 10f;
			controlButtonGroup.add(apply).pad(controlButtonPad);
			controlButtonGroup.add(cancel).pad(controlButtonPad);
			controlButtonGroup.add(showMap.pad(10f)).pad(controlButtonPad);
			controlButtonGroup.setPosition(getWidth()/2, getHeight()/2);
			
			float descriptionFieldHeight = getHeight() * 0.67f;
			// masterTable holds all other tables/groups
			// due to some bug, the first column has to be empty so the
			// WidgetGroup "campaignMissions" is showing
			Table masterTable = new Table();

			addActor(masterTable);
			masterTable.center().setPosition(getWidth()/2, getHeight()/2);
			masterTable.add();
			masterTable.add(campaignButtonGroup).left().bottom();
			masterTable.row();
			masterTable.add(campaignMissions).top().right();
			masterTable.add(descriptionField.pad(10f)).height(descriptionFieldHeight).top().left();
			masterTable.row();
			masterTable.add();
			masterTable.add(controlButtonGroup).center();
			
			campaign01.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					missionButtonGroup1.setVisible(true);
					missionButtonGroup2.setVisible(false);
					TextButton btn = (TextButton) missionButtonGroup1.getChildren().get(0);
					fireButton(btn);
				}
			});
			campaign02.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					missionButtonGroup1.setVisible(false);
					missionButtonGroup2.setVisible(true);
					TextButton btn = (TextButton) missionButtonGroup2.getChildren().get(0);
					fireButton(btn);
				}
			});
			
			// inital checking
			switch (missionID) {
			case Pottery:
				mission01.setChecked(true);
				campaign01.setChecked(true);
				break;
			case JaguarTeeth:
				mission02.setChecked(true);
				campaign01.setChecked(true);
				break;
			case Placeholder:
				mission03.setChecked(true);
				campaign02.setChecked(true);
				missionButtonGroup2.setVisible(true);
				missionButtonGroup1.setVisible(false);
				break;
			}
			
			descriptionField.setText(Mission.getDesc(missionID));
		}
		
		private void fireButton(TextButton btn) {
			InputEvent event = new InputEvent();
			event.setType(Type.touchDown);
			btn.fire(event);
			event = new InputEvent();
			event.setType(Type.touchUp);
			btn.fire(event);
		}
		
		private void setMissionButtonListener(final TextButton btn, final MissionID id) {
			btn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					missionButtonUncheckGroup.uncheckAll();
					btn.setChecked(true);
					missionID = id;
					descriptionField.setText(Mission.getDesc(missionID));
					Settings.updateMission(missionID);
				}
			});
		}
	}
}
