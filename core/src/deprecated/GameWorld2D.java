package deprecated;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class GameWorld2D {
	
	private GameScreen2D gameScreen;
	private GameRenderer2D renderer;

	private Boat2D boat;
	private Island2D target;
	private Rectangle worldRect;
	
	public int width;
	public int height;
	
	private boolean startRowing;
	
	public GameWorld2D(GameScreen2D screen) {
		width = 4000;
		height = 3000;
		this.gameScreen = screen;

		this.target = new Island2D(2000f, 2000f, 198f);
		
		this.boat = new Boat2D(this, 1000f, 1000f);
		this.boat.setDir(new Vector2(0, 1));
		
		target.setDir(new Vector2(0, 1).rotate(MathUtils.random.nextFloat() * 360 - 180));
		Gdx.app.log("GameWorld", "Succes creating random Boat and TargetIsland");
		Gdx.app.log("TargetIsland", "" + target.getPos());
		
		worldRect = new Rectangle(0, 0, width, height);

		startRowing = true;
	}

	public void update(float delta) {
		if (startRowing)
			boat.update(delta);
		
		if (Gdx.input.isKeyJustPressed(Keys.LEFT))
			boatLeftswing();
		if (Gdx.input.isKeyJustPressed(Keys.RIGHT))
			boatRightswing();
		
		if (Intersector.overlapConvexPolygons(boat.getHitbox(), target.getHitbox()) ||
				Gdx.input.isKeyJustPressed(Keys.ENTER))
			gameWin();
	}

	public void boatLeftswing() {
		startRowing = true;
		boat.leftRow();
	}

	public void boatRightswing() {
		startRowing = true;
		boat.rightRow();
	}
	
	public void gameWin() {
		gameScreen.win();
	}
	
	public void gameLose() {
		gameScreen.lose();
	}
	
	public Boat2D getBoat() {
		return boat;
	}
	
	public Island2D getTarget() {
		return target;
	}
	
	public Rectangle getRect() {
		return worldRect;
	}
	public void dispose() {
		
	}
	
	public void testShaper(ShapeRenderer shaper) {
		shaper.begin(ShapeType.Line);
		shaper.rect(200f, 200f, 200f, 200f);
		shaper.end();
	}
}
