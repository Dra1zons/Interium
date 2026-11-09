#version 330
uniform sampler2D Sampler0;uniform sampler2D Sampler1;
in vec2 localUV;in vec4 tint;flat in vec4 shape;out vec4 fragColor;
float sdf(vec2 p,vec2 halfSize,float r){vec2 q=abs(p)-halfSize+r;return min(max(q.x,q.y),0.0)+length(max(q,0.0))-r;}
void main(){
 vec2 size=max(shape.xy,vec2(1.0)),halfSize=size*0.5,p=(localUV-0.5)*size;
 float radius=clamp(shape.z,0.0,min(halfSize.x,halfSize.y));
 float dist=sdf(p,halfSize,radius),aa=max(fwidth(dist),0.01);
 float coverage=1.0-smoothstep(-aa,0.0,dist);if(coverage<=0.0)discard;
 vec2 pixel=1.0/vec2(textureSize(Sampler0,0));vec2 uv=gl_FragCoord.xy*pixel;
 // Gradient of the rounded boundary, including rounded corners. Screen Y is inverted.
 float epsilon=0.25;vec2 normal=normalize(vec2(sdf(p+vec2(epsilon,0),halfSize,radius)-sdf(p-vec2(epsilon,0),halfSize,radius),sdf(p+vec2(0,epsilon),halfSize,radius)-sdf(p-vec2(0,epsilon),halfSize,radius))+vec2(0.00001));
 float edge=1.0-smoothstep(0.0,max(2.0,min(radius,8.0)),-dist);
 vec2 bend=normal*vec2(1.0,-1.0)*pixel*shape.w*edge;
 vec2 safeUV=clamp(uv+bend,pixel*0.5,vec2(1.0)-pixel*0.5);
 vec3 blurred=texture(Sampler1,safeUV).rgb;
 vec3 sharp=texture(Sampler0,safeUV).rgb;
 vec3 background=mix(blurred,sharp,edge*0.18);
 vec3 color=mix(background,tint.rgb,0.25);
 float rim=1.0-smoothstep(0.0,1.0,-dist);
 float light=0.45+0.55*max(0.0,dot(normal,normalize(vec2(-0.5,-1.0))));
 color+=vec3(rim*light*0.24)+vec3(edge*0.025);
 fragColor=vec4(color,coverage*tint.a);
}
