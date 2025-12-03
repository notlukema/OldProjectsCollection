
#version 330 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec2 uv;

uniform float sin;
uniform float cos;

out vec2 f_pos;
out vec2 f_uv;

void main() {
	gl_Position = vec4(vec3(pos, 0.0), 1.0);
	f_uv = uv;

    float nx = pos.x * cos - pos.y * sin;
    float ny = pos.x * sin + pos.y * cos;
	f_pos = vec2(nx, ny);
}
