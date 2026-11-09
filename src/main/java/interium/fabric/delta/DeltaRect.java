package interium.fabric.delta;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.*;
import interium.mixin.DeltaDrawContextAccess;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
/** Delta rounded-rectangle SDF with ONE pipeline, not one shader per animated rectangle.
 * Geometry parameters travel as per-vertex UV1/UV2 shorts; no growing GPU pipeline cache.
 */
public final class DeltaRect {
 private static final VertexFormat FORMAT=VertexFormat.builder().add("Position",VertexFormatElement.POSITION).add("UV0",VertexFormatElement.UV0).add("Color",VertexFormatElement.COLOR).add("UV1",VertexFormatElement.UV1).add("UV2",VertexFormatElement.UV2).build();
 private static final RenderPipeline PIPELINE=RenderPipeline.builder().withLocation(Identifier.of("interium","pipeline/delta_rect"))
  .withVertexShader(Identifier.of("interium","core/delta_rect")).withFragmentShader(Identifier.of("interium","core/delta_rect"))
  .withUniform("DynamicTransforms",UniformType.UNIFORM_BUFFER).withUniform("Projection",UniformType.UNIFORM_BUFFER)
  .withBlend(BlendFunction.TRANSLUCENT).withVertexFormat(FORMAT,VertexFormat.DrawMode.QUADS).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
 private record Rect(Matrix3x2f matrix,float x,float y,float w,float h,float radius,float outline,int color,ScreenRect scissorArea,ScreenRect bounds) implements SimpleGuiElementRenderState {
  @Override public RenderPipeline pipeline(){return PIPELINE;}
  @Override public TextureSetup textureSetup(){return TextureSetup.empty();}
  @Override public void setupVertices(VertexConsumer v){point(v,x,y,0,0);point(v,x,y+h+1.2f,0,1);point(v,x+w+1.2f,y+h+1.2f,1,1);point(v,x+w+1.2f,y,1,0);}
  private void point(VertexConsumer v,float x,float y,float u,float t){Vector2f p=matrix.transformPosition(x,y,new Vector2f());v.vertex(p.x,p.y,0).texture(u,t).color(color).overlay(interium.core.VisualMath.packed(w,4),interium.core.VisualMath.packed(h,4)).light(interium.core.VisualMath.packed(radius,64),interium.core.VisualMath.packed(outline,64));}
 }
 public static void draw(DrawContext c,float x,float y,float w,float h,float radius,float outline,int color){if(w<=0||h<=0||(color>>>24)==0)return;float half=outline*.5f;float left=x-half-.6f,top=y-half-.6f,width=w+outline,height=h+outline;Matrix3x2f matrix=new Matrix3x2f(c.getMatrices());float minX=Float.POSITIVE_INFINITY,minY=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX;for(float[] p:new float[][]{{left,top},{left+width+1.2f,top},{left,top+height+1.2f},{left+width+1.2f,top+height+1.2f}}){Vector2f v=matrix.transformPosition(p[0],p[1],new Vector2f());minX=Math.min(minX,v.x);minY=Math.min(minY,v.y);maxX=Math.max(maxX,v.x);maxY=Math.max(maxY,v.y);}ScreenRect bounds=new ScreenRect((int)Math.floor(minX),(int)Math.floor(minY),(int)Math.ceil(maxX)-(int)Math.floor(minX),(int)Math.ceil(maxY)-(int)Math.floor(minY));var access=(DeltaDrawContextAccess)c;access.interium$state().addSimpleElement(new Rect(matrix,left,top,width,height,radius+half,outline,color,access.interium$scissors().peekLast(),bounds));}
}
