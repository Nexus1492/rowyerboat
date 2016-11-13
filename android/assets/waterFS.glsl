#ifdef GL_ES
	precision highp float;
#endif

uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
uniform sampler2D u_texture2;
uniform sampler2D u_texture3;
uniform vec3 u_camPos;
uniform vec3 u_lightPos;
uniform float u_time;

varying vec4 v_color;
varying vec2 v_texCoord0;

#define PI 3.1415926535897932384626433832795

float mean(vec3 colVec)
{
	return (colVec.r + colVec.g + colVec.b)/3.0;
}

float rand(vec2 n)
{
  return 0.5 + 0.5 * 
     fract(sin(dot(n.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

float rand(float x, float y)
{
	return rand(vec2(x, y));
}

void main()
{
	float factor = 64.0;// factor = 1.0;
	vec2 pos = v_texCoord0 * factor;
	
	float timeOffset = rand(floor(pos)) * 10.;
	
	vec2 texCoord = vec2(pos - floor(pos));
	vec4 tex0 = texture2D(u_texture0, texCoord);
	vec4 tex1 = texture2D(u_texture1, texCoord);
	vec4 tex2 = texture2D(u_texture2, texCoord);
	vec4 tex3 = texture2D(u_texture3, texCoord);
	
	float time = u_time * 2.0 + timeOffset;
	vec3 color0;
	if (time/PI < 2.0)
		color0 = tex0.rgb;
	else
		color0 = tex1.rgb;
	vec3 color1 = tex2.rgb;
	
	float blendVal = 1.0 - (cos(time)/2.0 + 0.5);
	vec3 offset = min(max(vec3(color0 * blendVal + color1 * (1.0 - blendVal)), 0.85), 1.0);
	
	gl_FragColor = vec4(v_color.rgb * offset, 0.75);
}

/*void main2simpleNormalMapping() {
	float texOffset = 500.0;
	vec4 color = texture(u_texture0, mod(v_texCoord0 * texOffset, 1.0));
	
	vec3 normal = texture(u_texture1, mod(v_texCoord0 * texOffset, 1.0)).rgb;
	normal = normalize(normal * 2.0 - 1.0);
	float dotFac = max(normalize(dot(u_camPos - gl_FragCoord.xyz, normal)), 0.0);
	
	gl_FragColor = vec4((v_color * color * (0.8 + dotFac/5.0)).rgb, 0.75f);
	
	//gl_FragColor = vec4((v_color * color).rgb, 0.75f);
}

void main3normalMapping() {
	float texOffset = 200.0;
	vec4 color = texture(u_texture0, mod(v_texCoord0 * texOffset, 1.0));
	
	vec3 normal = texture(u_texture1, mod(v_texCoord0 * texOffset, 1.0)).rgb;
	normal = normalize(normal * 2.0 - 1.0);
	
    // Ambient
    vec3 ambient = 0.1 * color.rgb;
    
    // Diffuse
    vec3 lightDir = normalize(u_lightPos - gl_FragCoord.xyz);
    vec3 diffuse = color.rgb * max(normalize(dot(lightDir, normal)), 0.0);
    
    // Specular
    vec3 viewDir = normalize(u_camPos - gl_FragCoord.xyz);
    vec3 idealDir = normalize(u_lightPos + u_camPos);
    float angle = pow(max(dot(normal, idealDir), 0.0), 32.0);
	vec3 specular = vec3(0.2f, 0.2f, 0.2f) * angle;
	
	gl_FragColor = vec4(diffuse + ambient + specular, 0.75f);
}*/