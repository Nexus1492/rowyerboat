varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
	vec4 tex = texture2D(u_texture, v_texCoords);
	float val = (tex.r + tex.g + tex.b)/3;          
	gl_FragColor = vec4(vec3(val), 1);
}