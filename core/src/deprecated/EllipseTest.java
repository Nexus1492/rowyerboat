package deprecated;

import java.awt.geom.Point2D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class EllipseTest implements Screen {
	
	public enum ApproxMode {
		inner, outer, rects;
		
		public ApproxMode next() {
			return ApproxMode.values()[(this.ordinal() + 1) % ApproxMode.values().length];
		}
	}
	
	private OrthographicCamera camera;
	private ShapeRenderer shaper;
	
	private float time = 0f;
	private float width = 200f;
	private float height = 100f;
	private ApproxMode approxMode = ApproxMode.rects;
	private float polyNum = 32;
	
	private float x2 = 400f;
	private float y2 = 300f;
	
	private BitmapFont font;
	private SpriteBatch batch;
	
	public EllipseTest() {
		shaper = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800f, 600f);
		camera.position.set(0f, 0f, 0f);
		camera.up.set(0f, 1f, 0f);
		
		font = new BitmapFont();
		
    	batch = new SpriteBatch();
    	batch.setProjectionMatrix(camera.combined);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}
	
	public void render(float delta) {
		renderModes(delta);
	}

	public void renderTest(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        width = 400f;
        height = 200f;
        
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_8))
			y2 += 1;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_2))
			y2 -= 1;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_6))
			x2 += 1;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_4))
			x2 -= 1;
        
        shaper.begin(ShapeType.Line);
        shaper.setColor(Color.WHITE);
        shaper.rect(100, 100, width, height);
        shaper.ellipse(100, 100, width, height);
        shaper.line(100 + width * 3/4, 100 + height, 100 + width, 100 + height * 3/4);
        
        shaper.setColor(Color.RED);
        shaper.rect(100 + width/4, 100, width/2, height);
        shaper.triangle(100 + width/4, 100, 100, 100 + height/2, 100 + width/4, 100 + height);
        
        shaper.setColor(Color.ORANGE);
        shaper.rect(x2, y2, width * 1.3f, height * 2.4f);
        shaper.ellipse(x2, y2, width * 1.3f, height * 2.4f);
        shaper.line(x2 + width * 1/4f * 1.3f, y2, x2, y2 + height * 1/4f * 2.4f);
        shaper.end();
	}
	
	public void renderModes(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_8))
			height += 1;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_2))
			height -= 1;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_6))
			width += 1;
		if (Gdx.input.isKeyPressed(Keys.NUMPAD_4))
			width -= 1;
		
		if (Gdx.input.isKeyJustPressed(Keys.NUMPAD_9))
			polyNum += 1;
		if (Gdx.input.isKeyJustPressed(Keys.NUMPAD_7) && polyNum > 0)
			polyNum -= 1;
		if (Gdx.input.isKeyJustPressed(Keys.NUMPAD_5))
			polyNum = 32;
		
		if (Gdx.input.isKeyJustPressed(Keys.SPACE))
			approxMode = approxMode.next();
		
		
		float x = camera.position.x, y = camera.position.y;
		x = 400;
		y = 300;
		float scl = 0.5f;

		float[] vertices = new float[]{
				x - width * scl, y + height,
				x - width, y + height * scl,
				x - width, y - height * scl,
				x - width * scl, y - height,
				x + width * scl, y - height,
				x + width, y - height * scl,
				x + width, y + height * scl,
				x + width * scl, y + height
		};
		
		if (approxMode == ApproxMode.inner) {
			vertices = new float[(int) (2 * polyNum)];
			int dex = 0;
			for (int k = 0; k < polyNum; k++) {
				vertices[dex++] = x + width * MathUtils.cos(k/polyNum * 2 * MathUtils.PI) * 1.0f;
				vertices[dex++] = y + height * MathUtils.sin(k/polyNum * 2 * MathUtils.PI) * 1.0f;
			}
		}
		
		Vector2 focL;
		Vector2 focR;
		float heightSq = (float) Math.pow(height, 2),
				widthSq = (float) Math.pow(width, 2),
				major = height > width ? height : width;
		double c = Math.sqrt(Math.abs(Math.pow(height, 2) - Math.pow(width,  2)));
		if (height > width) {
			//float c = (float) Math.sqrt(heightSq - widthSq);
			focL = new Vector2(x, y - (float)c);
			focR = new Vector2(x, y + (float)c);
		} else {
			//float c = (float) Math.sqrt(widthSq - heightSq);
			focL = new Vector2(x - (float)c, y);
			focR = new Vector2(x + (float)c, y);
		}
		
		Vector2 runner;
		switch(approxMode) {
		case rects:
			shaper.setProjectionMatrix(camera.combined);
			shaper.begin(ShapeType.Line);
			shaper.setColor(Color.WHITE);
			shaper.ellipse(x - width, y - height, 2 * width, 2 * height);
			runner = new Vector2(x + MathUtils.cos(time) * width, y + MathUtils.sin(time) * height);
			shaper.circle(runner.x, runner.y, 5);
			shaper.setColor(Color.ORANGE);
			/*shaper.rect(x - width*1/2,   y - height,       width,       2 * height);
			shaper.rect(x -   width * 3/4, y - height * 3/4, width * 6/4, height * 6/4);
			shaper.rect(x -   width,       y - height/2,     2 * width,   height);
			*/
			float n = 12f;
			for (float i = 1; i <= n; ++i) {
				float xOff = width * 0.4f + width * (i/n) * 0.6f,
						yOff = height * 0.4f + height * ((n - i + 1)/n) * 0.6f;
				shaper.rect(x - xOff, y - yOff, xOff * 2, yOff * 2);
			}
			
			shaper.end();
			break;
		
		default:
			shaper.setProjectionMatrix(camera.combined);
			shaper.begin(ShapeType.Line);
			shaper.setColor(Color.WHITE);
			shaper.ellipse(x - width, y - height, 2 * width, 2 * height);
			runner = new Vector2(x + MathUtils.cos(time) * width, y + MathUtils.sin(time) * height);
			shaper.circle(runner.x, runner.y, 5);
			shaper.setColor(Color.ORANGE);
			shaper.polygon(vertices);
			shaper.setColor(Color.RED);
			shaper.circle(focL.x, focL.y, 2);
			shaper.circle(focR.x, focR.y, 2);
			shaper.line(focL, runner);
			shaper.line(focR, runner);
			shaper.end();
			break;
		}
		
		time += 1 * delta;
		
		
		//******************************** DEBUG TEXT ****************************
		batch.begin();
		font.draw(batch, "polyNum: " + polyNum, 5, 20);
		font.draw(batch, "distance: " + (int)(runner.dst(focL) + runner.dst(focR) - 2 * major), 5, 40);
		
		font.draw(batch, "in circle: " + liesInside(400, 300, Gdx.input.getX(), Gdx.input.getY()), 5, 60);
		batch.end();
	}
	
    public boolean liesInside(double x1, double y1, double x2, double y2) {
        double a = 0.0, b = 0.0; // a = added width, b = added height
        double majorAxis;
        double c = Math.sqrt(Math
                .abs(Math.pow(width, 2) - Math.pow(height, 2)));
        if (height > width) {
            b = c;
            majorAxis = height;
        } else {
            a = c;
            majorAxis = width;
        }
        if (Point2D.distance(x1 - a, y1 - b, x2, y2)
                + Point2D.distance(x1 + a, y1 + b, x2, y2) <= 2.0
                        * majorAxis)
            return true;
        else
            return false;
    }

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
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
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
