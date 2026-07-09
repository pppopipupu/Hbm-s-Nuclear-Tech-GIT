#version 120

uniform sampler2D texture;
uniform sampler2D u_screenTexture;
uniform float iTime;
uniform float u_screenWidth;
uniform float u_screenHeight;
uniform int u_hasFbo;
uniform vec4 u_iconUvRange;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    
    vec2 center = vec2(0.5, 0.5);
    vec2 toCenter = uv - center;
    float dist = length(toCenter);
    
    float mask = 1.0 - smoothstep(1.15, 1.35, dist);
    
    vec4 origTexColor = vec4(0.0);
    if (uv.x >= 0.0 && uv.x <= 1.0 && uv.y >= 0.0 && uv.y <= 1.0) {
        vec2 iconGlobalUv = u_iconUvRange.xz + uv * (u_iconUvRange.yw - u_iconUvRange.xz);
        origTexColor = texture2D(texture, iconGlobalUv);
    }
    
    float speed = 0.75;
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
        stars1 = smoothstep(0.18, 0.0, length(fpos1 - vec2(0.5))) * starFlash * 1.5;
    }

    vec2 starCoord2 = gl_FragCoord.xy * 0.35;
    float stars2 = 0.0;
    vec2 ipos2 = floor(starCoord2);
    vec2 fpos2 = fract(starCoord2);
    float h2 = hash(ipos2);
    if (h2 > 0.91) {
        float starFlash = 0.4 + 0.6 * sin(iTime * 12.0 + h2 * 6.28);
        stars2 = smoothstep(0.12, 0.0, length(fpos2 - vec2(0.5))) * starFlash * 0.8;
    }
    
    vec3 qgpColor = vec3(r, g, b) * flash * 1.2 + vec3(stars1 + stars2);
    
    float rainbowMask = origTexColor.a * (1.0 - smoothstep(0.18, 0.22, dist));
    
    vec3 blackholeColor;
    if (u_hasFbo == 1) {
        vec2 screenUv = gl_FragCoord.xy / vec2(u_screenWidth, u_screenHeight);
        float radius = 1.35;
        vec2 refractionOffset = vec2(0.0);
        if (dist < radius) {
            float force = pow((radius - dist) / radius, 2.5) * 0.15 * mask;
            refractionOffset = -normalize(toCenter) * force;
        }
        vec2 sampleCoord = clamp(screenUv + refractionOffset, vec2(0.001), vec2(0.999));
        vec3 background = texture2D(u_screenTexture, sampleCoord).rgb;
        blackholeColor = mix(background, qgpColor, rainbowMask * 0.35);
    } else {
        blackholeColor = qgpColor;
    }
    
    if (dist < 0.09) {
        blackholeColor = vec3(0.0);
    } else {
        float ring = smoothstep(0.0, 0.08, dist - 0.09) * (1.0 - smoothstep(0.08, 0.45, dist - 0.09));
        vec3 glowColor = mix(vec3(1.0, 0.8, 0.3), qgpColor * 1.5, 0.6);
        float glowMask = origTexColor.a * (1.0 - smoothstep(0.35, 0.45, dist));
        blackholeColor += glowColor * ring * 1.2 * glowMask;
    }
    
    vec3 mixedColor;
    if (u_hasFbo == 1) {
        vec2 screenUv = gl_FragCoord.xy / vec2(u_screenWidth, u_screenHeight);
        vec3 cleanBg = texture2D(u_screenTexture, screenUv).rgb;
        mixedColor = mix(cleanBg, blackholeColor, mask);
    } else {
        mixedColor = mix(vec3(0.0), blackholeColor, mask * rainbowMask);
        if (mask * rainbowMask < 0.05) {
            discard;
        }
    }
    
    gl_FragColor = vec4(mixedColor, 1.0);
}
