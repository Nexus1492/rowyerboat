#ifdef GL_ES 
	precision mediump float;
#endif

uniform vec3 u_cameraDir;

varying vec3 v_color;
varying vec3 v_normal;

vec3 getColor(float height) {
    vec3 colorVec = vec3(0.0, 0.0, 0.0);
    vec3 floorVec;
    vec3 ceilVec;
    
    float sandLimit = .015;
    float grassLimit = 0.04;
    float jungleLimit = 0.075;
    float volcanoLimit = 0.1;
    
    float fac = 0.0;
    
    vec3 sandCol = vec3(178.0/255.0, 184.0/255.0, 0);
    vec3 grassCol = vec3(0, 160.0/255.0, 0);
    vec3 jungleCol = vec3(0, 145.0/255.0, 0);
    vec3 volcanoCol = vec3(0.35, 0.35, 0.35);
    
    if (height < sandLimit) {
        floorVec = sandCol;
        ceilVec = grassCol;
        fac = mix(sandLimit, grassLimit, height);
    } else if (height < grassLimit) {
    	floorVec = grassCol;
        ceilVec = jungleCol;
        fac = mix(grassLimit, jungleLimit, height);
    } else if (height < jungleLimit) {
    	floorVec = jungleCol;
        ceilVec = volcanoCol;
        fac = mix(jungleLimit, volcanoLimit, height);
    } else {
    	floorVec = volcanoCol;
    	ceilVec = volcanoCol;
    	fac = 0.0;
   	}
    
    return floorVec * (1.0 - fac) + ceilVec * fac;
}

void main() {
	gl_FragColor = vec4(getColor(v_color.z) * (0.75-dot(v_normal, u_cameraDir)/2.0), 1.0);
}