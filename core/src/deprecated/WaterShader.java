package com.rowyerboat.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.rowyerboat.gameworld.GameWorld;

public class WaterShader implements Shader {
	public ShaderProgram program;
	Camera camera;
	RenderContext context;

	@Override
	public void init() {
    		String vert = Gdx.files.internal("waterVertex.glsl").readString();
    		String frag = Gdx.files.internal("waterFragment.glsl").readString();
			program = new ShaderProgram(vert, frag);
    		if (!program.isCompiled())
    			throw new GdxRuntimeException(program.getLog());
    		else {
    			program.pedantic = false;
    			program.begin();
    			program.setUniformf("u_renderHeight", 1);
    			program.setUniformf("u_kabelWater", 1);
    			program.setUniformf("u_waveWater", 1);
    			program.setUniformf("u_lightPos", new Vector3(2000, 1500, 1000));
    			program.end();
    		}
	}

	@Override
	public void begin(Camera camera, RenderContext context) {
		this.camera = camera;
		this.context = context;
		program.begin();
		program.setUniformMatrix("u_projViewTrans", camera.combined);
        context.setDepthTest(GL30.GL_LEQUAL);
        context.setCullFace(GL30.GL_BACK);
	}

	public void begin(Camera camera, RenderContext context, float time) {
		this.camera = camera;
		this.context = context;
		program.begin();
		program.setUniformMatrix("u_projViewTrans", camera.combined);
		program.setUniformf("u_time", time);
        context.setDepthTest(GL30.GL_LEQUAL);
        context.setCullFace(GL30.GL_BACK);
	}

	@Override
	public void render(Renderable renderable) {
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        renderable.meshPart.render(program);
	}

	public void update(float time, Camera camera, GameWorld world) {
		program.begin();
		program.setUniformf("u_time", time);
		program.setUniformf("u_cameraPos", new Vector3(camera.position.x, camera.position.y, camera.position.z));
		program.setUniformf("u_targetPos", new Vector3(1800f, 1300f, 0f));
		program.end();
	}

	public void updateGerstner(float[] amp, float[] waveLen, float[] speed, float[] steepness, Array<Vector2> dir) {
		int numWaves = amp.length;
		program.begin();
		program.setUniformf("numWaves", numWaves);
		program.setUniform1fv("amplitude[0]", amp, 0, numWaves);
		program.setUniform1fv("wavelength[0]", speed, 0, numWaves);
		program.setUniform1fv("speed[0]", waveLen, 0, numWaves);
		program.setUniform1fv("steepness[0]", steepness, 0, numWaves);
		float maxAmp = 0;
		for (int i = 0; i < numWaves; i++) {
			maxAmp = amp[i] > maxAmp ? amp[i] : maxAmp;
			program.setUniformf("direction["+i+"]", dir.get(0));
		}
		program.setUniformf("maxAmp", maxAmp);
		program.end();
	}

	@Override
	public int compareTo(Shader other) {
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance) {
		return true;
	}

	@Override
	public void end() {
		program.end();
	}

	@Override
	public void dispose() {
		program.dispose();
	}
}
