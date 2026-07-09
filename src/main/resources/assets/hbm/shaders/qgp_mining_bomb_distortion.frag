#version 120

uniform sampler2D texture;
uniform sampler2D u_screenTexture;
uniform float iTime;
uniform float u_screenWidth;
uniform float u_screenHeight;
uniform int u_hasFbo;
uniform vec4 u_iconUvRange;
uniform float u_progress;

void main() {
    vec2 uv = gl_TexCoord[0].st;
    
    vec2 center = vec2(0.5, 0.5);
    vec2 toCenter = uv - center;
    
    float aspect = u_screenWidth / u_screenHeight;
    vec2 correctedToCenter = toCenter * vec2(aspect, 1.0);
    float dist = length(correctedToCenter);
    
    vec3 finalColor;
    
    if (u_hasFbo == 1) {
        vec2 screenUv = gl_FragCoord.xy / vec2(u_screenWidth, u_screenHeight);
        
        float waveRadius = u_progress * 1.2;
        float waveWidth = 0.15;
        float distToWave = abs(dist - waveRadius);
        
        vec2 refractionOffset = vec2(0.0);
        
        if (distToWave < waveWidth) {
            float factor = (waveWidth - distToWave) / waveWidth;
            float force = sin((dist - waveRadius) * 45.0) * 0.05 * factor * (1.0 - u_progress);
            refractionOffset = normalize(toCenter) * force;
        }
        
        vec2 sampleCoord = clamp(screenUv + refractionOffset, vec2(0.001), vec2(0.999));
        finalColor = texture2D(u_screenTexture, sampleCoord).rgb;
        
        if (distToWave < waveWidth) {
            float factor = (waveWidth - distToWave) / waveWidth;
            float r = 0.5 + 0.5 * sin(dist * 10.0 + iTime * 5.0);
            float g = 0.5 + 0.5 * sin(dist * 10.0 + iTime * 5.0 + 2.0);
            float b = 0.5 + 0.5 * sin(dist * 10.0 + iTime * 5.0 + 4.0);
            vec3 rainbow = vec3(r, g, b) * 0.15 * factor * (1.0 - u_progress);
            finalColor += rainbow;
        }
    } else {
        finalColor = texture2D(texture, uv).rgb;
    }
    
    gl_FragColor = vec4(finalColor, 1.0);
}
