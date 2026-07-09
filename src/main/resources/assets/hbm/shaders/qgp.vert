#version 120

uniform float iTime;

void main() {
    vec4 pos = gl_Vertex;
    pos.x += sin(pos.y * 3.0 + iTime * 4.0) * 0.03;
    pos.y += cos(pos.x * 3.0 + iTime * 4.0) * 0.03;
    gl_Position = gl_ModelViewProjectionMatrix * pos;
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}
