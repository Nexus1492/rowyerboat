attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;

varying vec3 v_color;
varying vec3 v_normal;

void main() {
    v_color = vec3(0.0, 0.0, a_position.z + sin(a_position.x)/200.0 + sin(a_position.y)/200.0);
    v_normal = normalize(a_normal);

    gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);
}
