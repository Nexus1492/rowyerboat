
const float pi = 3.1415926;

attribute vec3 a_position;

uniform vec3 u_cameraPos;
uniform vec3 u_lightPos;
uniform vec3 u_targetPos;
uniform float u_time;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;

uniform float u_renderHeight;
uniform float u_kabbelWater;
uniform float u_waveWater;

// Gerstner variables
uniform int numWaves;
uniform float maxAmp;
uniform float amplitude[16];
uniform float wavelength[16];
uniform float speed[16];
uniform float steepness[16];
uniform vec2 direction[16];

varying vec3 v_fragCol;
varying float v_factor;
varying vec3 v_normal;
varying vec3 v_pos;

float rand(vec2 n) {
  return 0.5 + 0.5 * 
     fract(sin(dot(n.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 generateNormal(vec3 P) {
	vec3 normal = vec3(0, 0, 0);
	for (int i = 0; i < numWaves; ++i) {
		float w_i = 2.0 * pi / wavelength[i];
		float degSinCos = dot(vec3(w_i * direction[i], 0), P) + speed[i] * w_i * u_time;
		float WA = w_i * amplitude[i];
		normal.x += direction[i].x * WA * cos(degSinCos);
		normal.y += direction[i].y * WA * cos(degSinCos);
		normal.z += steepness[i] * WA * sin(degSinCos);
	}
	return normalize(vec3(-normal.x, -normal.y, 1.0 - normal.z));
}

vec3 wave(vec2 p) {
	vec3 newPos = vec3(p, 0);
	for (int i = 0; i < numWaves; ++i) {
		float w_i = 2.0 * pi / wavelength[i];
		float degCos = dot(w_i * direction[i], p) + speed[i] * w_i * u_time;
		float newPosXY = steepness[i] * amplitude[i] * cos(degCos);
		newPos.x += direction[i].x * newPosXY;
		newPos.y += direction[i].y * newPosXY;
		newPos.z += amplitude[i] * sin(degCos);
	}
	return newPos;
}

void waveWater() {
	v_pos = wave(a_position.xy);

	float toCamDist = length(a_position.xy - u_cameraPos.xy);
	
	v_factor = v_pos.z / 10.0;

    v_fragCol = vec3(0.2 + 0.2 * v_factor,
    		0.2 + 0.2 * v_factor,
    		0.95 + v_factor * 0.05);
    		
	v_normal = generateNormal(v_pos);
	
	// Provide lighting
	float lighting = dot(normalize(u_cameraPos), v_normal);
    if (lighting < 0.0)
    	v_fragCol.xy *= 1.0 + lighting * 4.0;
    else
    	v_fragCol.xy *= 1.0 - lighting / 4.0;

	// Flat horizon
    float heightDistFactor;
    if (toCamDist > 900.0)
    	heightDistFactor = 0.0;
    else
    	heightDistFactor = 1.0;
    	
    if (length(v_pos - u_targetPos) < 140.0) {
    	v_pos.z = 0.0;
    	v_fragCol *= 1.5;
    }
	if (u_renderHeight < 1.0)
		heightDistFactor = 0.0;
	
	float r;
	if (u_kabbelWater > 0.0) {
		r = (sin(rand(a_position.xy) * u_time * 15.0) + 1.0) / 2.0;
		v_fragCol *= 1.0 + r / 10.0;
	} else
		r = 0.5;

	gl_Position = u_projViewTrans * u_worldTrans * vec4(v_pos.xy, (v_pos.z + (r - 0.5) * 6.0 - 3.0) * heightDistFactor, 1.0);
}

void kabbelWater() {
	v_factor = (sin(rand(a_position.xy) * u_time * 5.0) + 1.0) / 2.0;
	
    v_fragCol = vec3(0.3 + 0.1 * v_factor,
    		0.3 + 0.1 * v_factor,
    		0.95 + v_factor * 0.05);
	
	gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position.xy, v_factor * u_renderHeight, 1.0);
}

void main() {
	if (length(a_position.xy - u_cameraPos.xy) > 2000.0) {
		v_fragCol = vec3(0.3, 0.3, 0.95);
		gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position.xy, 0.0, 1.0);
	} else if (u_waveWater > 0.0)
		waveWater();
	else
		kabbelWater();
}