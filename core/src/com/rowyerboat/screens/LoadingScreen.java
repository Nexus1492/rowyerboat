package com.rowyerboat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rowyerboat.helper.AssetLoader;
import com.rowyerboat.helper.Settings;

public class LoadingScreen implements Screen {
	
	private ShapeRenderer shaper;
	
	LoadingStage stage = new LoadingStage();
	
	public LoadingScreen() {
	}

	@Override
	public void show() {
		//Settings.game.init();
	}

	@Override
	public void render(final float delta) {
		stage.draw(delta);
		//if (Settings.game.init && AssetLoader.finishedLoading)
		//	Settings.game.setScreen(new MainScreen());
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().setScreenSize(width, height);
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
		shaper.dispose();
		stage.dispose();
	}

	private class LoadingStage extends RYBStage {
		
		final int frameWidth = 150;
		final int frameHeight = 100;
		final float frameTime = 0.18f;
		
		Animation anim;
		TextureRegion boatTex;
		Texture waterTex;
		
		private float elapsedTime = 0;
		
		BitmapFont font = AssetLoader.getFont();
		private float stringWidth;
		
		public LoadingStage() {GlyphLayout layout = new GlyphLayout(font, "Loading");
			stringWidth = layout.width;
			TextureRegion[][] texRegions = TextureRegion.split(
					new Texture(Gdx.files.internal("boat_pixelart.png")),
						frameWidth, frameHeight);
			
			boatTex = texRegions[0][0];
			
			TextureRegion[] animFrames = new TextureRegion[6];
			for (int i = 0; i < 6; ++i)
				animFrames[i] = texRegions[(i + 8) / 4][(i + 8) % 4];
			anim = new Animation(frameTime, animFrames);
			anim.setPlayMode(PlayMode.LOOP);
			
			shaper = new ShapeRenderer();
			
			Pixmap pix = new Pixmap(1, 1, Format.RGBA8888);
			pix.setColor(new Color(0.4f, 0.4f, 1f, 1f));
			pix.fill();
			waterTex = new Texture(pix);
			pix.dispose();
		}
		
		public void draw(float delta) {
			Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT); // Clear screen
			
			Batch batch = this.getBatch();
			
			elapsedTime += delta;
			float waveRiding = MathUtils.sin(elapsedTime/(frameTime * 12f) * MathUtils.PI * 2) * 2f;
			float boatPos = (getWidth() + frameWidth) * (elapsedTime % 6) / 6f - frameWidth;
			
			batch.begin();
			
			batch.draw(boatTex, boatPos,
					getHeight()/2 - frameHeight/2 + waveRiding);
			
			batch.setColor(new Color(1, 1, 1, 0.6f));
			batch.draw(waterTex, 0, getHeight()/2f - 10f - frameHeight/12, getWidth(), 10f);
			batch.setColor(new Color(1, 1, 1, 1f));
			
			batch.draw(anim.getKeyFrame(elapsedTime), boatPos,
					getHeight()/2 - frameHeight/2 + waveRiding);
			
			batch.setColor(new Color(1, 1, 1, 0.6f));
			batch.draw(waterTex, 0, 0, getWidth(), getHeight()/2f - 10f - frameHeight/12);
			batch.setColor(new Color(1, 1, 1, 1f));
			
			String loadingString = "Loading";
			for (int i = 0; i < (int)(elapsedTime % 4); ++i)
				loadingString += ".";
			font.draw(batch, loadingString,
					getWidth()/2 - stringWidth/2, getHeight() * 0.67f);

			batch.end();
			super.draw();
		}
		
		@Override
		public void dispose() {
			waterTex.dispose();
			font.dispose();
			super.dispose();
		}
	}
}
