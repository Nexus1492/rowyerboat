package com.rowyerboat.screens;

import java.io.IOException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rowyerboat.gameobjects.Location;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.HttpPoster;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.scientific.Transverter;

/**
 * Screen displaying a map of the gameworld. Handles the logic to display
 * certain shapes on the map, like the boat's path. Temporarily unbinds all
 * controls in order to avoid complications. Pressing a button/touching the
 * screen resets the map back to its former {@link InputProcessor} and
 * {@link Screen}
 * 
 * @author Roman Lamsal
 * 
 */
public class WorldMapScreen implements Screen {

	float time = 0;

	WorldMapStage stage;

	String timeTakenString;
	String recordString;

	public WorldMapScreen() {
		stage = new WorldMapStage();

		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		time += delta;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

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
	public void dispose() {
		stage.dispose();
	}

	private class WorldMapStage extends RYBStage {
		Actor mapLarge;
		Actor mapBoxed;

		float padding = 10f;

		float mapX = -1, mapY = -1;
		float mapWidth, mapHeight;

		float cellWidth = this.getWidth() - padding * 2;
		float cellHeight = this.getHeight() - 50 - padding * 3;

		float boxX, boxY;
		float boxWidth, boxHeight;

		private boolean isBoxed = false;
		
		private int inputDelay = 1;

		public WorldMapStage() {
			initMapTexes();
			
			TextButton quitButton = new TextButton("Quit", skin);
			quitButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					//Settings.game.returnToLastScreen(); TODO is this doing the trick?
					Settings.world.gameEnd(false);
				}
			});

			Table masterTable = new Table();
			masterTable.center().bottom().setPosition(getWidth() / 2, 0);
			addActor(masterTable);

			masterTable.add();
			masterTable.row();
			masterTable.add(new BackButton("Back", skin)).padBottom(padding).center();
			if (Settings.game.getScreen().getClass().equals(GameScreen.class)) {
				masterTable.getCells().get(0).colspan(2);
				masterTable.getCells().get(1).right();
				masterTable.add(quitButton).left().padBottom(padding).padLeft(padding * 10);
			}
			switchMapLayout();
		}
		
		public void act() {
			super.act();
			// after 1 second, accept input
			if (time > inputDelay && Gdx.input.isKeyJustPressed(-1)) {
				stage.switchMapLayout();
			}
		}

		@SuppressWarnings("unchecked")
		public void switchMapLayout() {
			isBoxed = mapBoxed != null ? !isBoxed : false;
			Cell<Actor> cell = ((Table) getActors().get(0)).getCells().get(0);
			if (isBoxed) {
				Vector2 scaledVec = Scaling.fit.apply(boxWidth, boxHeight,
						cellWidth, cellHeight);
				cell.setActor(mapBoxed).center().width(scaledVec.x).height(scaledVec.y).pad(padding);
			} else {
				cell.setActor(mapLarge).center().width(mapWidth).height(mapHeight).pad(padding);
			}
		}

		public void initMapTexes() {
			Texture map = Settings.map.mapTex;
			
			float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = 0, maxY = 0;
			for (Vector2 vec : Settings.tracker.getPoints()) {
				minX = Math.min(vec.x, minX);
				minY = Math.min(vec.y, minY);
				maxX = Math.max(vec.x, maxX);
				maxY = Math.max(vec.y, maxY);
			}
			for (Vector3 vec : Settings.tracker.targetsReached) {
				minX = Math.min(vec.x, minX);
				minY = Math.min(vec.y, minY);
				maxX = Math.max(vec.x, maxX);
				maxY = Math.max(vec.y, maxY);
			}
			float boxPad = 100f;
			boolean notFitting = true;
			int iter = 0, maxIter = 1000;
			while (notFitting && iter++ < maxIter) {
				Vector2 bottomLeft = Transverter.gameToTexture(new Vector2(minX, minY),
						map.getWidth(), map.getHeight()).sub(boxPad, boxPad);
				
				Vector2 topRight = Transverter.gameToTexture(new Vector2(maxX, maxY),
						map.getWidth(), map.getHeight()).add(boxPad, boxPad);
				
				boxX = bottomLeft.x;
				boxY = bottomLeft.y;
				boxWidth = topRight.x - bottomLeft.x;
				boxHeight = topRight.y - bottomLeft.y;
				float widthRatio = boxWidth / cellWidth;
				float heightRatio = boxHeight / cellHeight;
				if (widthRatio < heightRatio) {
					boxWidth = cellWidth * heightRatio;
				} else if (widthRatio > heightRatio) {
					boxHeight = cellHeight * widthRatio;
				}
				topRight = bottomLeft.cpy().add(boxWidth, boxHeight);
				boxX = bottomLeft.x - Math.max(topRight.x - map.getWidth(), 0);
				boxY = bottomLeft.y - Math.max(topRight.y - map.getHeight(), 0);
				notFitting = (boxX < 0 || boxY < 0);
				if (notFitting)
					boxPad *= 0.95f;
			}
			Gdx.app.log("BoxPad", String.valueOf(boxPad));
			
			Vector2 scaledVec = Scaling.fit.apply(map.getWidth(), map.getHeight(),
					cellWidth, cellHeight); //dimensions of the bottom row: 100 x 50 (default textbutton)
			mapWidth = scaledVec.x;
			mapHeight = scaledVec.y;
			
			final Sprite spriteLarge = getExcerpt(0, 0, map.getWidth(), map.getHeight());

			mapLarge = new Actor() {
				@Override
				public void draw (Batch batch, float parentAlpha) {
					if (mapX < 0 && mapY < 0) {
						mapX = getX();
						mapY = getY();
					}
					batch.draw(spriteLarge, getX(), getY(), getWidth(), getHeight());
				}
			};
			mapLarge.addListener(new ClickListener() {
				public void clicked (InputEvent event, float x, float y) {
					if (time > inputDelay)
						stage.switchMapLayout();
				}
			});

			if (iter < maxIter) { // => correct excerpt could be found in maxIter iterations
				final Sprite spriteBox = getExcerpt(boxX, boxY, boxWidth, boxHeight);
				
				mapBoxed = new Actor() {
					@Override
					public void draw (Batch batch, float parentAlpha) {
						if (mapX < 0 && mapY < 0) {
							mapX = getX();
							mapY = getY();
						}
						batch.draw(spriteBox, getX(), getY(), getWidth(), getHeight());
					}
				};
				mapBoxed.addListener(new ClickListener() {
					public void clicked (InputEvent event, float x, float y) {
						if (time > inputDelay)
							stage.switchMapLayout();
					}
				});
			}
		}

		private Sprite getExcerpt(float x, float y, float width, float height) {
			Texture map = Settings.map.mapTex;
			Texture background = AssetLoader.mapBackground;
			
			FrameBuffer buffer = new FrameBuffer(Format.RGBA8888, MathUtils.nextPowerOfTwo(map.getWidth()),
					MathUtils.nextPowerOfTwo(map.getHeight()), false);
			ShapeRenderer shaper = new ShapeRenderer();
			shaper.setAutoShapeType(true);
			SpriteBatch batch = new SpriteBatch();
			Camera cam = new OrthographicCamera(width, height);
			cam.position.set(x + width/2, y + height/2, 0);
			cam.update();

			// first the standard maptex
			buffer.begin();
			batch.setProjectionMatrix(cam.combined);
			batch.begin();
			batch.draw(background, 0, 0, map.getWidth(), map.getHeight());
			batch.setColor(Color.ORANGE);
			batch.draw(map, 0, 0, map.getWidth(), map.getHeight());
			batch.setColor(Color.WHITE);
			batch.end();

			shaper.setProjectionMatrix(cam.combined);
			shaper.begin(ShapeType.Filled);
			// draw boatpath
			Array<Vector2> pts = Settings.tracker.getPoints();
			shaper.setColor(Color.BLUE);
			int offset = Math.max(1, pts.size / 1000); // with >2000 points, only draw each "size/1.000"th point
			for (int i = 0; i < pts.size; i += offset) {
				Vector2 vec = Transverter.gameToTexture(pts.get(i), map.getWidth(), map.getHeight());
				shaper.circle(vec.x, vec.y, 3f);
				if (i + offset >= pts.size)
					i = pts.size - 1;
			}
			shaper.setColor(Color.WHITE);

			// draw targetstuff
			for (int i = 0; i < Settings.getMission().targetSize(); ++i) {
				Vector3 vec3 = Settings.tracker.targetsReached[i];
				shaper.setColor(Settings.tracker.targetsReached[i].z > 0 ? Color.GREEN : Color.RED);
				Vector2 vec = Transverter.gameToTexture(new Vector2(vec3.x, vec3.y),
						map.getWidth(), map.getHeight());
				shaper.circle(vec.x, vec.y, 3);
			}
			shaper.setColor(Color.WHITE);
			shaper.set(ShapeType.Line);
			shaper.rect(x, y, width - 0.25f, height - 0.25f);
			shaper.end();

			buffer.end();

			final Sprite sprite = new Sprite(buffer.getColorBufferTexture());
			sprite.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			sprite.flip(false, true);

			return sprite;
		}
	}
}
