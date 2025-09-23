#version 330 core
layout(location = 0) in vec2 position;

uniform mat4 projection;
uniform vec2 offset;
uniform float scale;

void main() {
    gl_Position = projection * vec4(position * scale + offset, 0.0, 1.0);
}
