#version 120

uniform sampler2D texture;
uniform float iTime;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

mat2 rotate2D(float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return mat2(c, -s, s, c);
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 texColor = texture2D(texture, uv);
    
    if (texColor.a < 0.05) {
        discard;
    }
    
    vec2 centerUv = uv - vec2(0.5);
    
    float distToCenter = length(centerUv);
    float angle = atan(centerUv.y, centerUv.x);
    
    float swirlSpeed = iTime * 2.2;
    float spiral = angle - distToCenter * 9.0 + swirlSpeed;
    
    float density1 = sin(spiral * 2.0) * 0.5 + 0.5;
    float density2 = sin(spiral * 3.0 + 3.14) * 0.5 + 0.5;
    float nebula = (density1 + density2) * 0.5;
    
    float core = 1.0 - smoothstep(0.0, 0.35, distToCenter);
    
    vec3 colNebula = mix(vec3(0.02, 0.04, 0.25), vec3(0.35, 0.05, 0.55), nebula);
    vec3 colCore = mix(vec3(0.9, 0.5, 0.1), vec3(1.0, 0.98, 0.85), core * core);
    vec3 galaxyColor = mix(colNebula * (nebula * 1.8 + 0.2), colCore, core * 0.95);
    
    vec2 rotatedUv = rotate2D(iTime * 0.45 - distToCenter * 3.0) * centerUv;
    vec2 starCoord = rotatedUv * 26.0;
    vec2 ipos = floor(starCoord);
    vec2 fpos = fract(starCoord);
    float h = hash(ipos);
    float star = 0.0;
    if (h > 0.91) {
        float starFlash = 0.3 + 0.7 * sin(iTime * 6.0 + h * 6.28);
        float d = length(fpos - vec2(0.5));
        star = (1.0 - smoothstep(0.0, 0.18, d)) * starFlash * 1.5;
    }
    
    vec3 finalGalaxy = galaxyColor + vec3(star);
    
    float maskX = smoothstep(0.20, 0.26, uv.x) * (1.0 - smoothstep(0.74, 0.80, uv.x));
    float maskY = smoothstep(0.20, 0.26, uv.y) * (1.0 - smoothstep(0.74, 0.80, uv.y));
    float canvasMask = maskX * maskY;
    
    vec3 canvasColor = finalGalaxy * (texColor.rgb * 1.6 + vec3(0.1));
    
    float innerBorder = smoothstep(0.0, 0.05, canvasMask) * (1.0 - smoothstep(0.08, 0.25, canvasMask));
    canvasColor += vec3(0.4, 0.15, 0.7) * innerBorder * 0.5;
    
    vec3 finalColor = mix(texColor.rgb, canvasColor, canvasMask);
    
    gl_FragColor = vec4(finalColor, texColor.a);
}
