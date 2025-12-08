#version 430

// Inputs
in float sphericalVertexDistance; // Fog
in float cylindricalVertexDistance; // Fog
in vec4 vertexColor; // Color multiplier for the vertex
in vec2 uv; // Passthrough UV value. Will be converted in the fragment shader
in vec2 lightUV; // UV coordinate in the lighting texture
in vec3 light0; // Direction towards Light0 in tangent space
in vec3 light1; // Direction towards Light1 in tangent space

// Samplers
uniform sampler2D Main;
uniform sampler2D NormalMap;
uniform sampler2D SpecularMap;
uniform sampler2D LightMap;

// Outputs
out vec4 fragColor;

// FOG
float linear_fog_value(float vertexDistance, float fogStart, float fogEnd) {
    if (vertexDistance <= fogStart) {
        return 0.0;
    } else if (vertexDistance >= fogEnd) {
        return 1.0;
    }
    return (vertexDistance - fogStart) / (fogEnd - fogStart);
}
float total_fog_value(float sphericalVertexDistance, float cylindricalVertexDistance, float environmentalStart, float environmantalEnd, float renderDistanceStart, float renderDistanceEnd) {
    return max(linear_fog_value(sphericalVertexDistance, environmentalStart, environmantalEnd), linear_fog_value(cylindricalVertexDistance, renderDistanceStart, renderDistanceEnd));
}
vec4 apply_fog(vec4 inColor, float sphericalVertexDistance, float cylindricalVertexDistance, float environmentalStart, float environmantalEnd, float renderDistanceStart, float renderDistanceEnd, vec4 fogColor) {
    float fogValue = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, environmentalStart, environmantalEnd, renderDistanceStart, renderDistanceEnd);
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

// LIGHT
#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)
float minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
    float light0 = max(0.0, dot(lightDir0, normal));
    float light1 = max(0.0, dot(lightDir1, normal));
    float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    return lightAccum;
}

// UNIFORMS
// FiguraUniforms is our custom one. The others are provided by MC for MC's functions.
layout(std140) uniform Lighting {
    vec3 Light0_Direction;
    vec3 Light1_Direction;
};
layout(std140) uniform Fog {
    vec4 FogColor;
    float FogEnvironmentalStart;
    float FogEnvironmentalEnd;
    float FogRenderDistanceStart;
    float FogRenderDistanceEnd;
    float FogSkyEnd;
    float FogCloudsEnd;
};
layout(std140) uniform Projection {
    mat4 ProjMat; // Convert from View space -> NDC
};

// Size = 224 bytes
layout(std140) uniform FiguraUniforms {
    // Matrices
    mat4 CamRelWorldMat; // Convert from Model space -> Camera-relative World space
    mat4 ViewMat; // Convert from Camera-relative World space -> View space
    // General uniforms
    vec4 OverlayColor; // Color of "overlay" value. Sent directly instead of being sampled.
    // UV modifiers for builtin textures
    vec4 Main_uvModifier;
    vec4 NormalMap_uvModifier;
    vec4 SpecularMap_uvModifier;
    vec4 Lightmap_uvModifier;
    // Screen size and game time, useful for custom fun shader effects
    vec2 ScreenSize;
    float GameTime;
    // 4 bytes padding
};

// FIGURA/MAIN

vec2 figura_convert_uv(in vec2 UV, in vec4 modifier) {
    return UV * modifier.zw + modifier.xy;
}

void main() {
    // Calculate main color, quit out if transparent.
    vec4 color = texture(Main, figura_convert_uv(uv, Main_uvModifier));
    color *= vertexColor;
    if (color.a < (1.0 / 510.0)) discard;
    // Apply normal-based lighting
    vec4 normalSample = texture(NormalMap, figura_convert_uv(uv, NormalMap_uvModifier)); // Sample the normal texture
    vec2 normalXY = normalSample.xy * 2.0 - 1.0; // Convert 0-1 range into -1 to 1
    vec3 normal = vec3(normalXY, sqrt(1.0 - dot(normalXY, normalXY))); // LabPBR spec says normal X/Y is stored in R/G components, Z component is reconstructed.
    float normalLight = minecraft_mix_light(normalize(light0), normalize(light1), normal, color);
    color.rgb *= normalLight;
    // Apply overlay color
    color.rgb = mix(OverlayColor.rgb, color.rgb, OverlayColor.a); // Yes, this mix is backwards. Minecraft's is backwards too. Should I fix?
    // Apply light level-based lighting
    vec3 lightMapColor = texture(LightMap, figura_convert_uv(lightUV, Lightmap_uvModifier)).rgb;
    vec4 specularSample = texture(SpecularMap, figura_convert_uv(uv, SpecularMap_uvModifier));
    float emissivity = specularSample.a; // LabPBR spec stores emissivity in the alpha channel of specular map
    lightMapColor = max(lightMapColor, emissivity);
    color.rgb *= lightMapColor;
    // Fog.
    color = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    // Output
    fragColor = color;
}
