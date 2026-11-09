#version 330
// Adapted from DeltaClient assets/delta/shaders/core/text/text.fsh.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
uniform sampler2D Sampler0;
in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;
float median3(vec3 c) { return max(min(c.r,c.g),min(max(c.r,c.g),c.b)); }
void main() {
    float dist = median3(texture(Sampler0, texCoord0).rgb) - 0.5;
    vec2 derivatives = vec2(dFdx(texCoord0.x), dFdy(texCoord0.y)) * textureSize(Sampler0,0);
    float pixels = float(DISTANCE_RANGE) * inversesqrt(max(dot(derivatives,derivatives), 1e-12));
    float alpha = smoothstep(-0.5,0.5,dist*pixels);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha) * ColorModulator;
    if (fragColor.a <= 0.0) discard;
}
