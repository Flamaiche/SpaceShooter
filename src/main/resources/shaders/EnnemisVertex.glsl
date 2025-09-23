#version 330 core

layout(location = 0) in vec3 aPos;      // position du vertex
layout(location = 1) in vec3 aColor;    // couleur du vertex
layout(location = 2) in vec2 aTexCoord; // coordonnée texture

out vec3 vertexColor; // couleur transmise au fragment shader

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model; // optionnel, pour transformations individuelles

void main()
{
    vertexColor = aColor;
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}
