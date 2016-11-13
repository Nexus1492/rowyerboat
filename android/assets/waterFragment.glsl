#ifdef GL_ES 
precision mediump float;
#endif

uniform float u_time;

varying vec3 v_normal;
varying vec3 v_fragCol;
varying float v_factor;

void main() {
    gl_FragColor = vec4(v_fragCol, 0.5);
}