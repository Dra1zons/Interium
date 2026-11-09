#version 330
// Port of Delta's rect.fsh and common.glsl rdist, with dimensions specialized per pipeline.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
in vec2 texCoord0;
flat in vec4 rectParams;
in vec4 vertexColor;
out vec4 fragColor;
float rdist(vec2 pos, vec2 size, float radius) {
    vec2 v = abs(pos) - size + radius;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius;
}
void main() {
    vec2 size = rectParams.xy;
    float radius = rectParams.z;
    float outline = rectParams.w;
    vec2 center = size * 0.5;
    vec2 p = center - texCoord0 * size;
    float outer = 1.0 - smoothstep(0.2, 1.0, rdist(p, center - 1.0, radius));
    float alpha = outer;
    if (outline > 0.001) {
        float inner = 1.0 - smoothstep(0.2, 1.0, rdist(p, center - 1.0 - outline, max(radius - outline, 0.01)));
        alpha = outer * (1.0 - inner);
    }
    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha) * ColorModulator;
    if (fragColor.a == 0.0) discard;
}
