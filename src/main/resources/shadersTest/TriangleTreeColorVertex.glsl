layout(location = 0) in vec3 aPos;   // position
layout(location = 1) in vec3 aColor; // couleur
layout(location = 2) in vec2 aTexCoord; // coordonnée texture

out vec3 ourColor; // on envoie la couleur au fragment shader

uniform mat4 model;

void main() {
    gl_Position = model * vec4(aPos, 1.0);
    ourColor = aColor; // envoie la couleur
}
