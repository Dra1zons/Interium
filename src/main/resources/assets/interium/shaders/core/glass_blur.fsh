#version 330
uniform sampler2D Sampler0;
layout(std140) uniform BlurParams { vec4 direction; };
in vec2 uv;
out vec4 fragColor;
void main(){vec2 d=direction.xy;vec3 c=texture(Sampler0,uv).rgb*0.2270270270;
c+=(texture(Sampler0,uv+d*1.3846153846).rgb+texture(Sampler0,uv-d*1.3846153846).rgb)*0.3162162162;
c+=(texture(Sampler0,uv+d*3.2307692308).rgb+texture(Sampler0,uv-d*3.2307692308).rgb)*0.0702702703;
fragColor=vec4(c,1.0);}
