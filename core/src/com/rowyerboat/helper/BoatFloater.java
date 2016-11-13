package com.rowyerboat.helper;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * utility class to process Gerstner Waves on the cpu; not implemented, as Gerstner Waves are not used anymore
 * 
 * @author Roman Lamsal
 */
public class BoatFloater {
	
	private float time;
	private float[] amp;
	private float[] waveLen;
	private float[] speed;
	private float[] steepness;
	private Array<Vector2> dir;
	
	public BoatFloater(float[] amp, float[] waveLen, float[] speed, float[] steepness, Array<Vector2> dir) {
		this.amp = amp;
		this.waveLen = waveLen;
		this.speed = speed;
		this.steepness = steepness;
		this.dir = dir;
	}
	
	public void update(float newTime) {
		this.time = newTime;
	}
	
	/*
	 * 01 - 11
	 *  |    |
	 * 00 - 10
	 */
	public Vector3 getHeight(float x, float y, Vector3 norm) {
		Vector3 vec = new Vector3(x, y, 0f);
		float pi = 3.1415926f;
		for (int i = 0; i < amp.length; ++i) {
			float w_i = 2 * pi / waveLen[i];
			float degCos = dir.get(i).cpy().scl(w_i).dot(new Vector2(x, y)) + speed[i] * w_i * time;
			float newPosXY = steepness[i] * amp[i] * MathUtils.cos(degCos);
			//vec.x += dir.get(i).x * newPosXY;
			//vec.y += dir.get(i).y * newPosXY;
			vec.z += amp[i] * MathUtils.sin(degCos);
		}
		for (int i = 0; i < amp.length && norm != null; ++i) {
			float w_i = 2 * pi / waveLen[i];
			Vector2 helpVec = dir.get(i).cpy();
			float degSinCos = (new Vector3(helpVec.x, helpVec.y, 0)).dot(vec.cpy()) * w_i + speed[i] * w_i * time;
			float WA = w_i * amp[i];
			norm.x -= dir.get(i).x * WA * MathUtils.cos(degSinCos);
			norm.y -= dir.get(i).y * WA * MathUtils.cos(degSinCos);
			norm.z -= steepness[i] * WA * MathUtils.sin(degSinCos);
		}
		if (norm != null)
			norm.nor();
		return vec;
	}
}
