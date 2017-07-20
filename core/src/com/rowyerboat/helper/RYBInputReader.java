package com.rowyerboat.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input.Keys;
import com.rowyerboat.gameworld.*;
import com.rowyerboat.rendering.GameRenderer;
import com.rowyerboat.screens.WorldMapScreen;

/**
 * Processes all the game controls; uses a instantiates a {@link GestureDetector} to 
 * implement gesture detection.
 * 
 * @author Roman Lamsal
 *
 */
public class RYBInputReader implements InputProcessor {
	public GestureDetector ges;
	
	private Game game;
	
	private GameWorld world;
	private GameRenderer renderer;
	
	Vector2 touched;
	float timeTouched;
	
	boolean ctrl = false;
	
	public RYBInputReader() {
		game = Settings.game;
		
		ges = new GestureDetector(new MyGestureListener());
		ges.setLongPressSeconds(0.1f);
	}
	
	public RYBInputReader init(GameWorld world, GameRenderer renderer) {
		this.world = world;
		this.renderer = renderer;
		
		return this;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		
		case (Keys.PLUS):
			if (Settings.userData.getBoolean("superUser")) {
				renderer.islandScale += 0.25f;
				return true;
			} else
				return false;
			
		case (Keys.MINUS):
			if (Settings.userData.getBoolean("superUser")) {
				renderer.islandScale -= 0.25f;
				return true;
			} else
				return false;

		// Boat Controls
		case (Keys.LEFT):
			if (ctrl)
				world.boatLeftStop();
			else
				world.boatLeftSwing();
			return true;

		case (Keys.RIGHT):
			if (ctrl)
				world.boatRightStop();
			else
				world.boatRightSwing();
			return true;
			
		case (Keys.UP):
			world.boatLeftStop();
			return true;
			
		case (Keys.DOWN):
			world.boatRightStop();
			return true;
		
		case (Keys.ENTER):
			//world.gameEnd(true);
			return true;
			
		case (Keys.BACKSPACE):
		case (Keys.BACK):
			world.resetBoat();
			return true;
			
		case (Keys.CONTROL_LEFT):
		case (Keys.CONTROL_RIGHT):
			ctrl = true;
			return true;
			
		// Interface
		case(Keys.D):
			if (Settings.userData.getBoolean("superUser")) {
				Settings.debug = !Settings.debug;
				return true;
			} else
				return false;
		
		case(Keys.H):
			if (Settings.userData.getBoolean("superUser")) {
				Settings.hud = !Settings.hud;
				return true;
			} else
				return false;
			
		case(Keys.M):
			game.setScreen(new WorldMapScreen());
			return true;
		
		case (Keys.F1):
			if (Settings.userData.getBoolean("superUser")) {
				switchCameraNext();
				return true;
			} else
				return false;
		
		case (Keys.F2):
			if (Settings.userData.getBoolean("superUser")) {
				switchCameraPrevious();
				return true;
			} else
				return false;
		
		case (Keys.F3):
			if (Settings.userData.getBoolean("superUser")) {
				switchCameraDistance();
				return true;
			} else
				return false;

		// ShaderID
		case (Keys.NUM_1):
			Settings.shaderID = 0;
			return true;
		case (Keys.NUM_2):
			Settings.shaderID = 1;
			return true;
		case (Keys.NUM_3):
			Settings.shaderID = 2;
			return true;
		case (Keys.NUM_4):
			Settings.shaderID = 3;
			return true;
		case (Keys.NUM_5):
			Settings.shaderID = 4;
			return true;
		case (Keys.NUM_6):
			Settings.shaderID = 5;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch(keycode) {
		case(Keys.UP):
			world.getBoat().stopping = false;
			return true;

		case(Keys.DOWN):
			world.getBoat().stopping = false;
			return true;
			
		case(Keys.LEFT):
		case(Keys.RIGHT):
			world.getBoat().stopping = false;
			return true;
			
		case(Keys.CONTROL_LEFT):
		case(Keys.CONTROL_RIGHT):
			ctrl = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		world.getBoat().stopping = false;
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		float ticks = 4f;
		if (amount > 0) {
			renderer.camDistNear += ticks;
			renderer.camDistFar += ticks;
			renderer.camDistOrtho += 50f * ticks;
		} else {
			renderer.camDistNear -= ticks;
			renderer.camDistFar -= ticks;
			renderer.camDistOrtho -= 50f * ticks;
		}
		return true;
	}
	
	private void switchCameraNext() {
		if (renderer.cameraMode != null) {
			renderer.cameraMode = renderer.cameraMode.next();
		}
	}
	
	private void switchCameraPrevious() {
		if (renderer.cameraMode != null)
			renderer.cameraMode = renderer.cameraMode.previous();
	}
	
	private void switchCameraDistance() {
		renderer.camDistNear = 30f * Settings.boatScale;
		renderer.camDistFar = 130f * Settings.boatScale;
		if (renderer.cameraMode != null)
			renderer.cameraMode = renderer.cameraMode.changeDistance();
	}

	public class MyGestureListener implements GestureListener {
		
		@Override
		public boolean touchDown(float x, float y, int pointer, int button) {
			touched = new Vector2(x, y);
			if (Gdx.app.getType().equals(ApplicationType.Android)) {
				touched.x *= (float)Settings.width / (float)Gdx.graphics.getWidth();
				touched.y *= (float)Settings.height / (float)Gdx.graphics.getHeight();
			}
			return false;
		}

		@Override
		public boolean tap(float x, float y, int count, int button) {
			return true;
		}

		@Override
		public boolean longPress(float x, float y) {
			
			if (touched != null) {
				if (touched.x < Settings.width/2)
					world.boatLeftStop();
				else
					world.boatRightStop();
				touched = null;
				return true;
			}
			return false;
		}

		@Override
		public boolean fling(float velocityX, float velocityY, int button) {
			if (velocityY > 1 && touched != null) {
				if (touched.x < Settings.width/2)
					world.boatLeftSwing();
				else
					world.boatRightSwing();
				touched = null;
				return true;
			}
			return false;
		}

		@Override
		public boolean pan(float x, float y, float deltaX, float deltaY) {
			return false;
		}

		@Override
		public boolean panStop(float x, float y, int pointer, int button) {
			return false;
		}

		@Override
		public boolean zoom(float initialDistance, float distance) {
			return false;
		}

		@Override
		public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
			return false;
		}
	}
}
