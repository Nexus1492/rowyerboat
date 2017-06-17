package com.rowyerboat.screens;

import java.util.HashMap;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rowyerboat.gameworld.Campaign;
import com.rowyerboat.gameworld.Campaign.CampaignID;
import com.rowyerboat.gameworld.Mission;
import com.rowyerboat.gameworld.Mission.MissionID;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.HttpPoster;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Tracker;

public class MissionSelectionScreen implements Screen {

	Stage stage;
	Batch batch;
	
	Texture background;
	
	Mission lastlySelectedMission;
	MissionID missionID;
	
	public MissionSelectionScreen() {
		lastlySelectedMission = Settings.getMission();
		missionID = lastlySelectedMission.id;
		
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
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}
	
	private class MissionSelectionStage extends RYBStage {
		
		private ButtonGroup<TextButton> missionButtonUncheckGroup;
		private ButtonGroup<TextButton> campaignButtonUncheckGroup;
		private TextButton descriptionField;

		private HashMap<MissionID, TextButton> missionButtons;
		private HashMap<CampaignID, Table> missionButtonTables;
		private HashMap<CampaignID, TextButton> campaignButtons;
		
		private HashMap<CampaignID, MissionID> lastSelectedCampaignMission;
		
		float buttonPad;
		float missionButtonGroupWidth;
		
		public MissionSelectionStage() {
			super();
			lastSelectedCampaignMission = new HashMap<CampaignID, MissionID>();
			
			//skin = new Skin();
			skin.remove("default", TextButtonStyle.class);
			skin.remove("default", Texture.class);
			skin.add("default", AssetLoader.getFont());
			//Color std = new Color(0.7f, 0.7f, 0.7f, 0.5f);
			Color std = Color.DARK_GRAY;
			std.a = 0.5f;
			Pixmap pixmap = new Pixmap(100, 50, Format.RGBA8888);
			pixmap.setColor(std);
			pixmap.fill();
			skin.add("default", new Texture(pixmap));
			
			// default button style
			TextButtonStyle tbs = new TextButtonStyle();
			tbs.up = skin.newDrawable("default", std);
			tbs.checked = skin.newDrawable("descField");
			tbs.font = skin.getFont("default");
			skin.add("default", tbs);

			// accomplished mission button style
			tbs = new TextButtonStyle();
			tbs.up = skin.newDrawable("default", std);
			tbs.down = skin.newDrawable("default", Color.DARK_GRAY);
			tbs.checked = skin.newDrawable("descField");
			tbs.font = skin.getFont("default");
			tbs.fontColor = Color.GREEN;
			skin.add("accomplished", tbs);
			
			skin.add("descBackground", AssetLoader.mapBackground);

			missionButtons = new HashMap<MissionID, TextButton>();
			missionButtonTables = new HashMap<CampaignID, Table>();
			campaignButtons = new HashMap<CampaignID, TextButton>();
			
			final TextButton apply = new TextButton("Apply", skin);
			final TextButton cancel = new TextButton("Cancel", skin);
			final TextButton showMap = new TextButton("Show Map", skin);
			final TextButton showHighscores = new TextButton("Show Highscores", skin);
			
			missionButtonUncheckGroup = new ButtonGroup<TextButton>();
			missionButtonUncheckGroup.setUncheckLast(true);
			missionButtonUncheckGroup.uncheckAll();
			missionButtonUncheckGroup.setMinCheckCount(1);
			campaignButtonUncheckGroup = new ButtonGroup<TextButton>();
			campaignButtonUncheckGroup.setUncheckLast(true);
			campaignButtonUncheckGroup.uncheckAll();
			
			int descFieldWidth = Settings.width * 2/3, descFieldHeight = Settings.height/3 + 20; //TODO rubbish
			descriptionField = new TextField(descFieldWidth, descFieldHeight);

			apply.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.game.setScreen(new GameScreen(), false);
				}
			});
			cancel.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.setMission(lastlySelectedMission.id);
					Settings.game.returnToLastScreen();
				}
			});
			showMap.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Settings.game.setScreen(new WorldMapScreen());
					showMap.setChecked(false);
				}
			});
			showHighscores.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					//game.setScreen(new HighscoreScreen(game, game.getScreen(), "schnitzel-1320454\n300\nhacker-19875\n200"));
					HttpPoster.showOnlyHighscores(missionID, Settings.useEnergy);
					showHighscores.setChecked(false);
				}
			});

			buttonPad = 2f;
			missionButtonGroupWidth = 100 + buttonPad;
			
			Table campaignButtonTable = createCampaignTable();
			/*campaignButtonTable.add(campaign01.pad(buttonPad)).pad(buttonPad).padBottom(0f).padLeft(10f);
			campaignButtonTable.add(campaign02.pad(buttonPad)).pad(buttonPad).padBottom(0f);
			campaignButtonTable.bottom().left();*/

			WidgetGroup campaignMissions = new WidgetGroup();
			campaignMissions.setWidth(missionButtonGroupWidth);
			for (CampaignID id : missionButtonTables.keySet())
				campaignMissions.addActor(missionButtonTables.get(id));
			
			Table controlButtonGroup = new Table();
			float controlButtonPad = 10f;
			controlButtonGroup.add(apply.pad(10f)).pad(controlButtonPad);
			controlButtonGroup.add(cancel.pad(10f)).pad(controlButtonPad);
			controlButtonGroup.add(showMap.pad(10f)).pad(controlButtonPad);
			controlButtonGroup.add(showHighscores.pad(10f)).pad(controlButtonPad);
			controlButtonGroup.setPosition(getWidth()/2, getHeight()/2);
			
			// masterTable holds all other tables/groups
			// due to some bug, the first column has to be empty so the
			// WidgetGroup "campaignMissions" is showing
			float descriptionFieldHeight = getHeight() * 0.67f;
			Table masterTable = new Table();

			addActor(masterTable);
			masterTable.center().setPosition(getWidth()/2, getHeight()/2);
			masterTable.add();
			masterTable.add(campaignButtonTable).left().bottom();
			masterTable.row();
			masterTable.add(campaignMissions).top().right();
			masterTable.add(descriptionField.pad(10f))
				.height(descriptionFieldHeight).width(descFieldWidth)
				.top().left();
			masterTable.row();
			masterTable.add();
			masterTable.add(controlButtonGroup).center();
			
			fireButton(campaignButtons.get(lastlySelectedMission.campaignID));
			fireButton(missionButtons.get(lastlySelectedMission.id));
			
			descriptionField.setText(Mission.getDesc(missionID));
		}
		
		private Table createCampaignTable() {
			Table campaignTable = new Table();
			for (CampaignID id : CampaignID.values()) {
				if (Settings.campaignProgress.getBoolean(id.toString() + "_UNLOCKED", false)) {
					final TextButton campaignButton = addCampaignButton(id);
					if (campaignButton != null)
						campaignTable.add(campaignButton.pad(10f)).pad(buttonPad).padBottom(0f);
					
					campaignButtons.put(id, campaignButton);
				}
			}
			campaignTable.getCells().get(0).padLeft(10f);
			campaignTable.bottom().left();
			
			return campaignTable;
		}
		
		/**
		 * add campaign by ID (read code how it's processed, not that hard).
		 * 
		 * @param id
		 * @return TextButton for the campaign
		 */
		private TextButton addCampaignButton(CampaignID id) {
			Campaign campaign = Campaign.getCampaign(id);
			if (campaign.campaignMissions.size() == 0)
				return null;
			// if all missions are accomplished, color in green
			TextButtonStyle style = campaign.accomplishedMissions == campaign.campaignMissions.size() ?
					skin.get("accomplished", TextButtonStyle.class) : skin.get(TextButtonStyle.class);
			final TextButton campaignButton = new TextButton(id.getName(), style);
			setCampaignButtonListener(campaignButton, id);

			Table missionButtonTable = new Table();
			
			for (int i = 0; i < campaign.campaignMissions.size(); ++i) {
				missionButtonTable.row();

				Mission mis = campaign.campaignMissions.get(i);
				
				style = Settings.campaignProgress.getString(mis.id.toString(), null) != null ?
						skin.get("accomplished", TextButtonStyle.class) : skin.get(TextButtonStyle.class);
				final TextButton misButton = new TextButton(String.format("Mission%2d", i+1), style);
				setMissionButtonListener(misButton, mis.id);
				
				missionButtonTable.add(misButton).pad(buttonPad).padRight(0);
				missionButtonUncheckGroup.add(misButton);
				missionButtons.put(mis.id, misButton);
			}
			missionButtonTable.getCells().get(0).padTop(10f);
			missionButtonTable.top().right();
			
			missionButtonTables.put(id, missionButtonTable);
			campaignButtonUncheckGroup.add(campaignButton);
			
			return campaignButton;
		}
		
		private void setCampaignButtonListener(final TextButton btn, final CampaignID id) {
			btn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				// set the first missionButtonGroup visible, the others invisible
				for (CampaignID cID : missionButtonTables.keySet()) {
					if (cID != id)
						missionButtonTables.get(cID).setVisible(false);
					else
						missionButtonTables.get(id).setVisible(true);
						
				}
				Campaign campaign = Campaign.getCampaign(id);
				MissionID missionID = lastSelectedCampaignMission.get(campaign.id);
				if (campaign.isFinished && missionID == null)
					missionID = campaign.campaignMissions.get(0).id;
				else if (missionID == null) {
					for (Mission mis : campaign.campaignMissions)
						if (Settings.campaignProgress.getString(mis.id.toString(), null) == null) {
							missionID = mis.id;
							break;
						}
				}
				fireButton(missionButtons.get(missionID));
			}
		});
		}
		
		private void setMissionButtonListener(final TextButton btn, final MissionID id) {
			btn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					//missionButtonUncheckGroup.uncheckAll();
					btn.setChecked(true);
					missionID = id;
					descriptionField.setText(Mission.getDesc(missionID));
					Settings.setMission(missionID);
					lastSelectedCampaignMission.put(id.getCampaignID(), id);
				}
			});
		}
	}
}
