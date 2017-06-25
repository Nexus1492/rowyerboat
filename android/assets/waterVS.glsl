uniform mat4 u_projView;
uniform mat4 u_worldTrans;

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

varying vec4 v_color;
varying vec2 v_texCoord0;

void main()
{
	v_texCoord0 = a_texCoord0;
    v_color = vec4(0.0, 0.7, 1.0, 0.75);
    gl_Position = u_projView * a_position;
} 