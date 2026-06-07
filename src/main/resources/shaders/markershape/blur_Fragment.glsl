#version 330 core
out vec4 FragColor;

uniform sampler2D uScreenTex;
uniform vec2 uScreenSize;
uniform float uAlpha;
uniform vec3 uTint;

void main() {
    vec2 texelSize = 1.0 / uScreenSize;
    vec2 uv = gl_FragCoord.xy / uScreenSize;

    // 5x5 box blur (25 samples)
    vec4 color = vec4(0.0);
    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            color += texture(uScreenTex, uv + vec2(x, y) * texelSize);
        }
    }
    color /= 25.0;

    // desaturate to gray
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), 0.7);

    // apply tint
    FragColor = vec4(color.rgb * uTint, uAlpha);
}
