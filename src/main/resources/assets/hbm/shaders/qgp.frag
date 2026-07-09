#version 120

uniform sampler2D texture;
uniform float iTime;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 texColor = texture2D(texture, uv);
    
    if (texColor.a < 0.05) {
        discard;
    }
    
    float speed = 2.5;
    vec2 coord = gl_FragCoord.xy * 0.08 + uv * 8.0;
    
    float r = 0.5 + 0.5 * sin(coord.x * 0.5 + iTime * speed);
    float g = 0.5 + 0.5 * sin(coord.y * 0.6 - iTime * speed * 0.8 + 2.0);
    float b = 0.5 + 0.5 * sin((coord.x - coord.y) * 0.4 + iTime * speed * 1.2 + 4.0);
    
    float flash = 0.8 + 0.2 * sin(iTime * 15.0 + coord.x * 2.0);
    
    vec2 starCoord1 = gl_FragCoord.xy * 0.18;
    float stars1 = 0.0;
    vec2 ipos1 = floor(starCoord1);
    vec2 fpos1 = fract(starCoord1);
    float h1 = hash(ipos1);
    if (h1 > 0.95) {
        float starFlash = 0.5 + 0.5 * sin(iTime * 6.0 + h1 * 6.28);
        float dist = length(fpos1 - vec2(0.5));
        stars1 = smoothstep(0.18, 0.0, dist) * starFlash * 1.5;
    }

    vec2 starCoord2 = gl_FragCoord.xy * 0.35;
    float stars2 = 0.0;
    vec2 ipos2 = floor(starCoord2);
    vec2 fpos2 = fract(starCoord2);
    float h2 = hash(ipos2);
    if (h2 > 0.91) {
        float starFlash = 0.4 + 0.6 * sin(iTime * 12.0 + h2 * 6.28);
        float dist = length(fpos2 - vec2(0.5));
        stars2 = smoothstep(0.12, 0.0, dist) * starFlash * 0.8;
    }
    
    vec3 finalColor = vec3(r, g, b) * flash * 1.2 + vec3(stars1 + stars2);
    gl_FragColor = vec4(finalColor, texColor.a);
}
