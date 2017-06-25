package com.rowyerboat.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.gameobjects.Boat;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.rendering.GameRenderer.CameraMode;
import com.rowyerboat.rendering.GameRenderer.ShaderIDName;
import com.rowyerboat.screens.WorldMapScreen;

public class GameUI {
	// UI-related
	private Stage stage;
	private Viewport viewport;
	private SpriteBatch batch;

	private BitmapFont font;
	private Texture arrowImage;
	private Texture circularArrowImage;
	private Texture minimapBackground;
	
	private Texture tick;
	
	private float width;
	private float height;
	
	private float delta;
	
	// GameRenderer-related
	public Boat boat;
	private Camera camera3D;
	private CameraMode cameraMode;
	private Vector2 boatToTarget;
	private Vector2 cameraDirV2;

	/**
	 * 
	 * @param b {@link Boat} instance being referenced
	 * @param c {@link Camera} used for calculating relative directions, not for the stage being used
	 * @param s {@link SpriteBatch} for the HUD (at best the same as the parent's SpriteBatch)
	 * @param f {@link Font} used for the HUD
	 */
	public GameUI(Boat b, Camera c, SpriteBatch s) {
		boat = b;
		camera3D = c;
		batch = s;
		font = AssetLoader.getFont();
		
		arrowImage = AssetLoader.arrow;
		circularArrowImage = AssetLoader.circularArrowImage;
		
		tick = AssetLoader.tick;
		
		viewport = new FitViewport(Settings.width, Settings.height);
		stage = new Stage(viewport);

		width = viewport.getWorldWidth();
		height = viewport.getWorldHeight();
		
		createUI();

		InputMultiplexer inputMulti = (InputMultiplexer) Gdx.input.getInputProcessor();
		inputMulti.getProcessors().reverse();
		inputMulti.addProcessor(stage);
		inputMulti.getProcessors().reverse();
		Gdx.input.setInputProcessor(inputMulti);
	}
	
	public void render(float delta, Vector2 boatToTarget, Vector2 cameraDirV2) {
		this.delta = delta;
		
		width = viewport.getWorldWidth();
		height = viewport.getWorldHeight();
		
		this.cameraDirV2 = cameraDirV2;
		this.boatToTarget = boatToTarget;
		
		stage.act();
		stage.draw();
		if (Settings.hud) {
			renderHUD(delta);
		}
	}
	
	public void renderHUD(float delta) {
		cameraMode = Settings.renderer.cameraMode;
		
		batch.begin();
		
		bottomUpHUD("BoatPos: " + boat.getMid(),
				"CameraPos: " + camera3D.position,
				"BoatMid: " + (int)boat.getMid().x + ", " + (int)boat.getMid().y,
				"Current Speed: " + String.format("%2.1f, with %.3f / %.3f",
						boat.currSpeed[2], boat.currSpeed[0], boat.currSpeed[1]),
				"Boatdimensions: " 
						+ String.format("%.1f", boat.width) + "px x "
						+ String.format("%.1f", boat.height) + "px ~= "
						+ String.format("%.1f", boat.width * 110f) + "m x "
						+ String.format("%.1f", boat.height * 110f) + "m",
				"Speed: ["+ (int)(boat.getRelativeSpeed() * 100f) + "%] " 
						+ boat.getRelativeSpeed() * boat.getMaxSpeed(),
				"Rota: " + (int)Math.abs(boat.getDir().angle(boat.getDirOverGround()))
				);
		
		topDownHUD("FPS: " + ((int)(Gdx.graphics.getFramesPerSecond())),
				"CameraMode: " + cameraMode.toString(),
				"ShaderMode: " + ShaderIDName.values()[Settings.shaderID],
				"Resolution: " + width + " x " + height,
				"Ratio: " + (width/height),
				"LocationScale: " + boat.locationScale
				);
		
		if (cameraMode != CameraMode.ortho) {
			// Arrow res is 16x16
			batch.setColor(Color.RED);
			batch.draw(arrowImage, viewport.getWorldWidth()/2 - 16, viewport.getWorldHeight()/2 - 16,
					16, 16, 32, 32, 1, 1, boat.getCurrentDir().angle() - 90 - 180, 0, 0, 32, 32, false, false);
			batch.setColor(Color.WHITE);
		}
		batch.end();
	}
	

	// ********************************** UTILITY **********************************
	/** Convenience method to ensure clean ordering of outputted strings from top-left to bottom-left */
	private void topDownHUD(String... strings) {
		for (int i = 0; i < strings.length; ++i) {
			font.draw(batch, strings[i], 5, viewport.getWorldHeight() - 20 * i - 5);
		}
	}

	/** Convenience method to ensure clean ordering of outputted strings from bottom-left to top-left */
	private void bottomUpHUD(String... strings) {
		for (int i = 0; i < strings.length; ++i) {
			font.draw(batch, strings[i], 5, 20 * i);
		}
	}
	
	public void createUI() {
		int btnSize = 60;
		Skin skin = new Skin();
		Pixmap pixmap = new Pixmap(btnSize, btnSize, Format.RGBA8888);
		pixmap.setColor(new Color(1, 1, 1, 0.25f));
		pixmap.fill();
 
		skin.add("white", new Texture(pixmap));
 
		BitmapFont bfont = AssetLoader.getFont();
		bfont.getData().scale(2);
		bfont.setColor(Color.BLACK);
		skin.add("default", bfont);
 
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.LIGHT_GRAY);
 
		textButtonStyle.font = skin.getFont("default");
 
		skin.add("default", textButtonStyle);
 
		// define actors
		TextButton zoomIn = new TextButton("+", textButtonStyle);
		TextButton zoomOut = new TextButton("-", textButtonStyle);
		TextButton debug = new TextButton("D", textButtonStyle);
		TextButton hud = new TextButton("H", textButtonStyle);
		TextButton mapButton = new TextButton("M", textButtonStyle);
		
		// BARS
		Actor bars = new Actor() {
			ShapeRenderer shaper;
			final float width = 300f;
			final float height = 30f;
			private float time = 0f;
			
			@Override
			public void draw(Batch batch, float parentAlpha) {
				batch.end();
				float rel = boat.getRelativeSpeed();
				float border = 5f;
				float level1 = 0.5f, level2 = 0.85f;
				time = rel < level1 ? 0 : time + delta;
				if (shaper == null)
					shaper = new ShapeRenderer();
				shaper.setProjectionMatrix(batch.getProjectionMatrix());
				shaper.begin(ShapeType.Filled);
				shaper.setColor(Color.BLACK);
				shaper.rect(this.getX(), this.getY(),
						width, Settings.useEnergy ? -2 * (height) + border : -height); // Background
				
				// speedbar
				shaper.setColor(new Color(Math.max((rel - 0.5f) * 2, 0.0f),
						rel > level2 ? 1/level2 - boat.getRelativeSpeed() * 1/level2 : 1f, // works for level2 = 0.75 || 0.5
						0, 1.0f));
				shaper.setColor(Color.GREEN);
				shaper.rect(this.getX() + border, this.getY() + border - height,
						Math.max(width * boat.getRelativeSpeed() - 2 * border, 2f), height - 2 * border);
				// energybar
				if (Settings.useEnergy) {
					shaper.setColor(rel < level1 ? Color.YELLOW : rel < level2 ? Color.ORANGE : Color.RED);
					shaper.rect(this.getX() + border, this.getY() + 2 * (border - height),
							Math.max(boat.getEnergy()/100f * width - 2 * border, 0f), height - 2 * border);
				}
				shaper.end();
				batch.begin();
			}
		};

		final int res = 136;
		Pixmap map = new Pixmap(res, res, Format.RGBA8888);
		map.setColor(Color.BLACK);
		map.fillCircle(res/2, res/2, 62);
		minimapBackground = new Texture(map);
		map.dispose();
		
		// Minimap
		Actor minimap = new Actor() {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				float x = this.getX(), y = this.getY();
				batch.draw(minimapBackground, x, y);
				batch.draw(circularArrowImage, x, y, res/2, res/2, res, res, 1, 1,
						cameraDirV2.angle(boatToTarget), 0, 0, res, res, false, false);
				
				float forward = Math.abs(boat.getDir().angle(boat.getDirOverGround()));
				Color arrowCol = new Color(
						forward > 90 ? -MathUtils.cosDeg(forward) : 0,
						forward < 90 ?  MathUtils.cosDeg(forward) : 0,
						MathUtils.sinDeg(forward),
						1.0f);
				batch.setColor(arrowCol);
				float stretchFactor = 2.0f; //Math.max(ratio, 1.0f);
				if (boat.getCurrentDir().len2() != 0)
					batch.draw(arrowImage, x + res/2 - 16, y + res/2 - 16,
							16, 16, 32, 32, stretchFactor, 1, -boat.getCurrentDir().angle(cameraDirV2) + 90,
							0, 0, 32, 32, false, false);
				batch.setColor(Color.WHITE);
			}
		};
		
		Actor ticks = new Actor() {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				int num = Settings.getMission().targetSize();
				for (int i = 0; i < num; ++i) {
					if (i < Settings.tracker.targetsReachedPointer) {
						batch.setColor(Color.GREEN);
						batch.draw(tick, this.getX() + i * tick.getWidth(), this.getY());
					} else {
						batch.setColor(Color.WHITE);
						batch.draw(AssetLoader.tickTransp, this.getX() + i * tick.getWidth(), this.getY());
					}
					batch.setColor(Color.WHITE);
				}
			}
		};
		
		// define positions
		bars.setPosition(5, viewport.getWorldHeight() - 5);
		minimap.setPosition(10, viewport.getWorldHeight() - 40 - 10 - AssetLoader.circularArrowImage.getHeight());
		ticks.setPosition((stage.getWidth() - Settings.getMission().targetSize() * tick.getWidth())/2,
				viewport.getWorldHeight() - tick.getHeight() - 5);
		
		zoomIn.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Settings.renderer.camDistNear -= 4 * Settings.boatScale;
				Settings.renderer.camDistFar -= 4 * Settings.boatScale;
				return true;
			}
		});
		zoomOut.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Settings.renderer.camDistNear += 4 * Settings.boatScale;
				Settings.renderer.camDistFar += 4 * Settings.boatScale;
				return true;
			}
		});
		debug.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				Settings.debug = !Settings.debug;
			}
		});
		hud.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				Settings.hud = !Settings.hud;
			}
		});
		mapButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Settings.game.setScreen(new WorldMapScreen());
			}
		});
		
		Table buttonTable = new Table();
		buttonTable.add(zoomIn).pad(5f).row();
		buttonTable.add(zoomOut).pad(5f).row();
		buttonTable.add(debug).pad(5f).row();
		buttonTable.add(hud).pad(5f).row();
		buttonTable.add(mapButton).pad(5f).row();
		buttonTable.top().right().setPosition(stage.getWidth(), stage.getHeight());
		
		// add actors
		stage.addActor(buttonTable);
		stage.addActor(minimap);
		stage.addActor(bars);;
		stage.addActor(ticks);
	}
	
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	public void dispose() {
		stage.dispose();
	}
}
