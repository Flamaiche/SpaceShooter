#version 330 core
out vec4 FragColor;

uniform vec4 ourColor; // couleur reçue depuis Java

void main() {
    FragColor = ourColor;
}