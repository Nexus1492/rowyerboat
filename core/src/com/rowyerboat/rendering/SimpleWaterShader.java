package com.rowyerboat.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

/**
 * Helperclass to store and process a simplistic water shader
 * which alternates between three different textures to simulate ocean movement
 * 
 * @author Roman Lamsal
 *
 */
public class SimpleWaterShader implements Shader {

	public ShaderProgram program;
	private Camera camera;
	private RenderContext context;
	private int u_projView;
	private float time;

	@Override
	public void init() {
		ShaderProgram.pedantic = false;
		program = new ShaderProgram(Gdx.files.internal("waterVS.glsl"),
				Gdx.files.internal("waterFS.glsl"));
		if (!program.isCompiled())
			Gdx.app.log("SimpleWaterShader", program.getLog());
		u_projView = program.getUniformLocation("u_projView");
	}

	@Override
	public void begin(Camera camera, RenderContext context) {
		this.camera = camera;
		this.context = context;
		program.begin();
		program.setUniformMatrix(u_projView, camera.combined);
        this.context.setDepthTest(GL30.GL_LEQUAL);
        this.context.setCullFace(GL30.GL_BACK);
	}
	
	public void update(float delta) {
		time += delta;
		program.begin();
		program.setUniformf("u_time", time);
		program.setUniformi("u_texture0", 2);
		program.setUniformi("u_texture1", 3);
		program.setUniformi("u_texture2", 4);
		program.setUniformi("u_texture3", 5);
		program.setUniformf("u_lightPos", new Vector3(512, 512, 300f));
		if (camera != null)
			program.setUniformf("u_camPos", camera.position);
		program.end();
	}

	@Override
	public void render(Renderable renderable) {
        renderable.meshPart.render(program);
	}

	@Override
	public void end() {
		program.end();
	}
	
	@Override
	public void dispose() {
	}


	@Override
	public int compareTo(Shader other) {
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance) {
		return true;
	}

}
