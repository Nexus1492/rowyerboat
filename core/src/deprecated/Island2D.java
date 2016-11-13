package deprecated;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rowyerboat.gameworld.GameWorld;

public class Island2D {
	private int seed;
	
	public String name;
	
	private Polygon hitbox;
	
	private Vector2 pos;
	private Vector2 dir;
	
	public float x;
	public float y;
	public boolean isConvex = false;
	
	public Island2D(Vector2 vec, float radius) {
		this(vec.x, vec.y, radius);
	}
	
	public Island2D(float x, float y, float radius) {
		MathUtils.random.setSeed(seed);
		
		this.x = x;
		this.y = y;
		
		pos = new Vector2(x, y);
		
		islandGenerator(radius);
		
		dir = new Vector2();
	}
	
	// Generates hitbox and circle
	private void islandGenerator(float radius) {
		Vector2 rotationVec = new Vector2(0, 1),
				scaledVec = new Vector2();
		int numOfPoints = 512;
		float[] vertices = new float[numOfPoints*2];
		
		for (int i = 0; i < (numOfPoints*2); i += 2) {
			scaledVec = rotationVec.cpy();
			vertices[i] = scaledVec.setLength(radius).x;
			vertices[i+1] = scaledVec.y;
			rotationVec.rotate(360f / (float)numOfPoints);
		}
		
		hitbox = new Polygon(vertices.clone());
		hitbox.setPosition(x, y);
	}
	
	public Vector2 getPos() {
		return pos;
	}
	
	public Polygon getHitbox() {
		return hitbox;
	}
	
	public Vector2 getDir() {
		return dir;
	}
	
	public void setDir(Vector2 dir) {
		this.dir = dir;
	}
}
