#version 330
layout(std140) uniform DynamicTransforms { mat4 ModelViewMat; vec4 ColorModulator; vec3 ModelOffset; mat4 TextureMat; };
layout(std140) uniform Projection { mat4 ProjMat; };
in vec3 Position;in vec2 UV0;in vec4 Color;in ivec2 UV1;in ivec2 UV2;
out vec2 localUV;out vec4 tint;flat out vec4 shape;
void main(){gl_Position=ProjMat*ModelViewMat*vec4(Position,1.0);localUV=UV0;tint=Color*ColorModulator;shape=vec4(vec2(UV1)/4.0,vec2(UV2)/64.0);}
