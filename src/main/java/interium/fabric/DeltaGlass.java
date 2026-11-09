package interium.fabric;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.*;
import interium.fabric.delta.DeltaRect;
import interium.mixin.DeltaDrawContextAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;
import java.util.*;

/** Current-frame scene copy, half-resolution separable Gaussian blur, refractive rounded panels.
 * Never samples the live render attachment. One capture/two blur passes per requested frame,
 * not per panel. Existing fonts, item icons and geometry are left untouched.
 */
public final class DeltaGlass {
 private static final VertexFormat FORMAT=VertexFormat.builder().add("Position",VertexFormatElement.POSITION).add("UV0",VertexFormatElement.UV0).add("Color",VertexFormatElement.COLOR).add("UV1",VertexFormatElement.UV1).add("UV2",VertexFormatElement.UV2).build();
 private static final RenderPipeline GLASS=RenderPipeline.builder().withLocation(Identifier.of("interium","pipeline/liquid_glass"))
  .withVertexShader(Identifier.of("interium","core/liquid_glass")).withFragmentShader(Identifier.of("interium","core/liquid_glass"))
  .withUniform("DynamicTransforms",UniformType.UNIFORM_BUFFER).withUniform("Projection",UniformType.UNIFORM_BUFFER)
  .withSampler("Sampler0").withSampler("Sampler1").withBlend(BlendFunction.TRANSLUCENT)
  .withVertexFormat(FORMAT,VertexFormat.DrawMode.QUADS).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
 private static final RenderPipeline BLUR=RenderPipeline.builder().withLocation(Identifier.of("interium","pipeline/glass_blur"))
  .withVertexShader(Identifier.of("interium","core/glass_blur")).withFragmentShader(Identifier.of("interium","core/glass_blur"))
  .withUniform("BlurParams",UniformType.UNIFORM_BUFFER).withSampler("Sampler0")
  .withVertexFormat(VertexFormats.EMPTY,VertexFormat.DrawMode.TRIANGLES).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
 private static SimpleFramebuffer scene,a,b;
 private static GpuSampler sampler;
 private static GpuBuffer horizontal,vertical;
 private static boolean requested,captured;
 private static int divisor=0,idleFrames;
 private static TextureSetup glassTextures;
 private static float cachedRadius,cachedGlow,cachedRim,cachedBend,cachedPulse;
 private static int cachedAccent,quality;
 public static boolean hasResources(){return scene!=null;}
 private static boolean likelyVisible(){var c=MinecraftClient.getInstance();var s=c.currentScreen;return s instanceof InteriumTitleScreen||s instanceof DeltaScreen||s instanceof ThemeEditorScreen||s instanceof PerformanceScreen||s instanceof DeltaVisualScreen||s instanceof DeltaHudEditor||(c.world!=null&&DeltaVisualSettings.hud&&!c.options.hudHidden);}

 public static boolean enabled(){return DeltaVisualSettings.flag("glass.enabled",true);}
 public static void beginFrame(){
  requested=false;captured=false;quality=DeltaPerformance.quality();
  cachedRadius=DeltaVisualSettings.number("glass.radius",8,4,14);cachedGlow=DeltaVisualSettings.number("theme.glow",.32f,0,1);
  cachedRim=DeltaVisualSettings.number("theme.rim",.65f,0,1);cachedBend=DeltaVisualSettings.number("glass.refraction",5,0,24);cachedPulse=DeltaVisualSettings.pulse();cachedAccent=DeltaVisualSettings.primary()&0xffffff;
  if(!enabled()||quality==0){close();return;}
  if(!likelyVisible()){if(++idleFrames>120)close();return;}idleFrames=0;
  var main=MinecraftClient.getInstance().getFramebuffer();
  if(main.textureWidth<1||main.textureHeight<1)return;
  if(scene==null||scene.textureWidth!=main.textureWidth||scene.textureHeight!=main.textureHeight||divisor!=DeltaPerformance.blurDivisor()){
   close();scene=new SimpleFramebuffer("Interium glass scene",main.textureWidth,main.textureHeight,false);
   divisor=DeltaPerformance.blurDivisor();int w=Math.max(1,main.textureWidth/divisor),h=Math.max(1,main.textureHeight/divisor);
   a=new SimpleFramebuffer("Interium glass blur horizontal",w,h,false);b=new SimpleFramebuffer("Interium glass blur vertical",w,h,false);
   var d=RenderSystem.getDevice();sampler=d.createSampler(AddressMode.CLAMP_TO_EDGE,AddressMode.CLAMP_TO_EDGE,FilterMode.LINEAR,FilterMode.LINEAR,1,OptionalDouble.empty());
   horizontal=d.createBuffer(()->"Glass horizontal parameters",GpuBuffer.USAGE_UNIFORM|GpuBuffer.USAGE_COPY_DST,16);
   vertical=d.createBuffer(()->"Glass vertical parameters",GpuBuffer.USAGE_UNIFORM|GpuBuffer.USAGE_COPY_DST,16);
   glassTextures=TextureSetup.of(scene.getColorAttachmentView(),sampler,b.getColorAttachmentView(),sampler);
  }
 }
 public static void capture(){
  if(!requested||captured||scene==null)return;captured=true;
  var main=MinecraftClient.getInstance().getFramebuffer();
  if(main.textureWidth!=scene.textureWidth||main.textureHeight!=scene.textureHeight)return;
  var encoder=RenderSystem.getDevice().createCommandEncoder();
  encoder.copyTextureToTexture(main.getColorAttachment(),scene.getColorAttachment(),0,0,0,0,0,scene.textureWidth,scene.textureHeight);
  float amount=DeltaVisualSettings.number("glass.blur",5,0,16);
  try(var stack=MemoryStack.stackPush()){
   var data=stack.malloc(16);data.putFloat(amount/scene.textureWidth).putFloat(0).putFloat(0).putFloat(0).flip();encoder.writeToBuffer(horizontal.slice(),data);
   data=stack.malloc(16);data.putFloat(0).putFloat(amount/scene.textureHeight).putFloat(0).putFloat(0).flip();encoder.writeToBuffer(vertical.slice(),data);
  }
  blur(scene,a,horizontal);blur(a,b,vertical);
 }
 private static void blur(SimpleFramebuffer from,SimpleFramebuffer to,GpuBuffer params){
  try(var pass=RenderSystem.getDevice().createCommandEncoder().createRenderPass(()->"Interium glass Gaussian blur",to.getColorAttachmentView(),OptionalInt.empty())){
   pass.setPipeline(BLUR);pass.setUniform("BlurParams",params);pass.bindTexture("Sampler0",from.getColorAttachmentView(),sampler);pass.draw(0,3);
  }
 }
 private record Panel(Matrix3x2f matrix,float x,float y,float w,float h,float radius,float bend,int color,TextureSetup textureSetup,ScreenRect scissorArea,ScreenRect bounds) implements SimpleGuiElementRenderState {
  @Override public RenderPipeline pipeline(){return GLASS;}
  @Override public void setupVertices(VertexConsumer v){point(v,x,y,0,0);point(v,x,y+h,0,1);point(v,x+w,y+h,1,1);point(v,x+w,y,1,0);}
  private void point(VertexConsumer v,float x,float y,float u,float t){Vector2f p=matrix.transformPosition(x,y,new Vector2f());v.vertex(p.x,p.y,0).texture(u,t).color(color).overlay(interium.core.VisualMath.packed(w,4),interium.core.VisualMath.packed(h,4)).light(interium.core.VisualMath.packed(radius,64),interium.core.VisualMath.packed(bend,64));}
 }
 public static void draw(DrawContext c,float x,float y,float w,float h,float radius,int color){
  if(w<=0||h<=0||(color>>>24)==0)return;
  radius=Math.min(Math.min(w,h)*.5f,Math.max(radius,cachedRadius));
  float visibility=(color>>>24)/255f,pulse=cachedPulse;
  float glow=cachedGlow*pulse;
  int accent=cachedAccent;
  // Three bounded rounded strokes, not a per-panel post-processing pass.
  DeltaRect.draw(c,x-2,y-1,w+4,h+4,radius+2,1.5f,(Math.round(14*visibility)<<24));
  if(glow>.001f)for(int i=DeltaPerformance.glowLayers();i>=1;i--)DeltaRect.draw(c,x-i*.7f,y-i*.7f,w+i*1.4f,h+i*1.4f,radius+i*.7f,.7f,(Math.round(9*glow*visibility)<<24)|accent);
  float rim=cachedRim;
  DeltaRect.draw(c,x-.3f,y-.3f,w+.6f,h+.6f,radius+.3f,.45f,(Math.round((18+48*rim*pulse)*visibility)<<24)|0xddd7ef);
  if(quality==0||!enabled()||scene==null){DeltaRect.draw(c,x,y,w,h,radius,0,color);return;}
  requested=true;
  Matrix3x2f matrix=new Matrix3x2f(c.getMatrices());
  Vector2f point=new Vector2f();float minX=Float.POSITIVE_INFINITY,minY=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX;
  for(int corner=0;corner<4;corner++){matrix.transformPosition(x+((corner&1)==0?0:w),y+((corner&2)==0?0:h),point);minX=Math.min(minX,point.x);minY=Math.min(minY,point.y);maxX=Math.max(maxX,point.x);maxY=Math.max(maxY,point.y);}
  ScreenRect bounds=new ScreenRect((int)Math.floor(minX),(int)Math.floor(minY),(int)Math.ceil(maxX)-(int)Math.floor(minX),(int)Math.ceil(maxY)-(int)Math.floor(minY));
  var access=(DeltaDrawContextAccess)c;
  access.interium$state().addSimpleElement(new Panel(matrix,x,y,w,h,Math.min(radius,Math.min(w,h)/2),cachedBend,color,glassTextures,access.interium$scissors().peekLast(),bounds));
 }
 public static void close(){requested=false;glassTextures=null;if(scene!=null){scene.delete();scene=null;}if(a!=null){a.delete();a=null;}if(b!=null){b.delete();b=null;}if(sampler!=null){sampler.close();sampler=null;}if(horizontal!=null){horizontal.close();horizontal=null;}if(vertical!=null){vertical.close();vertical=null;}}
}
