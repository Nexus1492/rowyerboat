package deprecated;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.rowyerboat.gameworld.GameMap;
import com.rowyerboat.gameworld.GameWorld;
import com.rowyerboat.helper.InputReader;
import com.rowyerboat.helper.Settings;
import com.rowyerboat.rendering.GameRenderer;

public class GameScreen2D implements Screen {
	
	private Game game;

	private GameWorld2D world;
	private GameRenderer2D renderer;
	
	private boolean doRender;

	public GameScreen2D(Game game) {
		this.game = game;
		this.world = new GameWorld2D(this);
		this.renderer = new GameRenderer2D(world);
		doRender = true;
	}

	@Override
	public void render(float delta) {
		world.update(delta);
		if (doRender)
			renderer.render(delta);
	}
	
	public void win() {
		doRender = false;
		game.setScreen(new GameScreen2D(game));
	}
	
	public void lose() {
		doRender = false;
		game.setScreen(new GameScreen2D(game));
	}
	
	@Override
	public void show() {
		
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		world.dispose();
		renderer.dispose();
	}

}
