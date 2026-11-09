package interium.fabric.delta;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

/** The uniform-radius, solid-color path from Delta rect.fsh, adapted to GUI render state. */
public final class DeltaRect {
    private static final Identifier WHITE=Identifier.of("interium","textures/gui/white.png");
    private static final Map<Key,RenderPipeline> PIPELINES=new HashMap<>();
    private record Key(int w,int h,int r,int outline) {}
    private static int quantize(float value){return Math.round(value*100);}
    private static RenderPipeline pipeline(Key k){return PIPELINES.computeIfAbsent(k,key -> RenderPipeline.builder()
        .withLocation(Identifier.of("interium","pipeline/delta_rect_"+key.w+"_"+key.h+"_"+key.r+"_"+key.outline))
        .withVertexShader(Identifier.of("minecraft","core/position_tex_color"))
        .withFragmentShader(Identifier.of("interium","core/delta_rect"))
        .withUniform("DynamicTransforms",UniformType.UNIFORM_BUFFER).withUniform("Projection",UniformType.UNIFORM_BUFFER)
        .withShaderDefine("RECT_WIDTH",key.w).withShaderDefine("RECT_HEIGHT",key.h)
        .withShaderDefine("RECT_RADIUS",key.r).withShaderDefine("RECT_OUTLINE",key.outline)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR,VertexFormat.DrawMode.QUADS)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());}
    public static void draw(DrawContext c,float x,float y,float w,float h,float radius,float outline,int color){
        if(w<=0||h<=0||(color>>>24)==0)return;
        // Delta expands its quad by 1.2 px for antialiasing; outlines grow by half their width.
        float half=outline*.5f;
        Key k=new Key(quantize(w+outline),quantize(h+outline),quantize(radius+half),quantize(outline));
        var m=c.getMatrices();m.pushMatrix();m.translate(x-half-.6f,y-half-.6f);m.scale(w+outline+1.2f,h+outline+1.2f);
        c.drawTexture(pipeline(k),WHITE,0,0,0f,0f,1,1,1,1,1,1,color);m.popMatrix();
    }
}
