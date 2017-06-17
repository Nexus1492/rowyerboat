package com.rowyerboat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.rendering.GameRenderer;

public class DebugWorldScreen extends GameRenderer {
	
	CameraInputController camControl;
	
	private int debugMode = 0;
	
	public DebugWorldScreen(GameWorld world) {
		super(world);
		camera = new PerspectiveCamera();
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(world.width/2, world.height/2, 1000f);
        camera.near = 1f;
        camera.far = camera.position.z + 100;
        camera.lookAt(camera.position.x, camera.position.y, 0);
		camera.up.set(0, 1, 0);
		camera.update();
		camControl = new MapScroller(camera);
		Gdx.input.setInputProcessor(camControl);
		shaderID = 1;
	}
	
	public void render(float delta) {
    	Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		
		processCamera();
		
		islandInstances.get(0).transform.scale(1f, islandScale, 1f);
		islandScale = 1f;
		
		waterShader.update(delta);
		modelBatch.begin(camera);
		modelBatch.render(waterInstances.get(0), waterShader);
		modelBatch.render(boatInstance, environment);
		for (int i = 0; i < islandInstances.size; ++i)
			modelBatch.render(islandInstances.get(i), environment);
		modelBatch.end();
		
		if (debugMode != 0) {
			shaper.setProjectionMatrix(camera.combined);

			if (debugMode > 0) {

				shaper.begin(ShapeType.Line);
				shaper.setAutoShapeType(true);
				shaper.identity();
				shaper.setColor(Color.RED);
				shaper.setColor(Color.FIREBRICK);
				for (int i = 0; i < locations.size; ++i) {
					shaper.polygon(locations.get(i).getHitbox().getTransformedVertices());
				}
				shaper.end();
			}		
			if (debugMode > 1)
				debugCurrentGrid();
		}
	}
	
	protected void processCamera() {
		cameraMode = CameraMode.ortho;
        camera.far = camera.position.z + 100;
		camera.up.set(0, 1, 0);
		camera.update();
		camControl.update();
	}
	
	private class MapScroller extends CameraInputController {
		
		int key = -1;

		public MapScroller(Camera camera) {
			super(camera);
			this.forwardButton = Keys.UP;
			this.scrollFactor = -50;
		}
		
		public void update() {
			super.update();
			if (key != -1)
				keyDown(key);
		}
		
		public boolean keyDown(int keycode) {
			key = keycode;
			float scrolling = 50f;
			switch (keycode){
			case Keys.LEFT:
				camera.position.x -= scrolling;
				return true;
			case Keys.RIGHT:
				camera.position.x += scrolling;
				return true;
			case Keys.DOWN:
				camera.position.y -= scrolling;
				return true;
			case Keys.UP:
				camera.position.y += scrolling;
				return true;
			case Keys.D:
				debugMode = (debugMode + 1) % 3;
				key = -1;
				System.out.println("DebugMode switched to " + debugMode);
				return true;
			}
			return false;
		}
		
		public boolean keyUp(int keycode) {
			key = -1;
			return false;
		}
	}
}
