package interium.fabric.delta;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Initial 1.21.11 adapter for the original Delta MTSDF atlases. Not wired to the UI yet.
 * Geometry and median-distance smoothing follow Delta's Font / MsdfGlyph / text.fsh.
 * No outlines, fade, gradients or resource-reload handling have been ported yet.
 */
public final class DeltaFont {
    private record Atlas(@SerializedName("distanceRange") int range, int width, int height) {}
    private record Metrics(float lineHeight, float descender) {}
    private record Bounds(float left, float right, float top, float bottom) {}
    private record Glyph(int unicode, float advance, Bounds planeBounds, Bounds atlasBounds) {}
    private record Kerning(@SerializedName("unicode1") int left, @SerializedName("unicode2") int right, float advance) {}
    private record Data(Atlas atlas, Metrics metrics, Glyph[] glyphs, @SerializedName("kerning") Kerning[] kernings) {}
    private final Data data;
    private final Identifier texture;
    private final RenderPipeline pipeline;
    private final Map<Integer,Glyph> glyphs = new HashMap<>();
    private final Map<Long,Float> kerning = new HashMap<>();

    private DeltaFont(String name, Data d) throws IOException {
        if(d == null || d.atlas == null || d.metrics == null || d.glyphs == null || d.atlas.width <= 0 || d.atlas.height <= 0)
            throw new IOException("Invalid Delta font: "+name);
        data=d; texture=Identifier.of("delta","fonts/"+name+".png");
        for(Glyph g:d.glyphs) glyphs.put(g.unicode,g);
        if(d.kernings!=null) for(Kerning k:d.kernings) kerning.put(pair(k.left,k.right),k.advance);
        pipeline=RenderPipeline.builder()
            .withLocation(Identifier.of("interium","pipeline/delta_msdf_"+name))
            .withVertexShader(Identifier.of("minecraft","core/position_tex_color"))
            .withFragmentShader(Identifier.of("interium","core/delta_msdf"))
            .withUniform("DynamicTransforms",UniformType.UNIFORM_BUFFER)
            .withUniform("Projection",UniformType.UNIFORM_BUFFER)
            .withSampler("Sampler0").withShaderDefine("DISTANCE_RANGE",d.atlas.range)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR,VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();
    }
    public static DeltaFont load(MinecraftClient client, String name) throws IOException {
        if(!name.matches("[a-z_]+")) throw new IllegalArgumentException("Invalid font name");
        Identifier id=Identifier.of("delta","fonts/"+name+".json");
        try(var reader=new InputStreamReader(client.getResourceManager().open(id),StandardCharsets.UTF_8)) {
            return new DeltaFont(name,new Gson().fromJson(reader,Data.class));
        }
    }
    private static long pair(int a,int b){return ((long)a<<32)|(b&0xffffffffL);}
    private float kern(int a,int b,float size){return kerning.getOrDefault(pair(a,b),0f)*size;}
    public float width(String text,float size) {
        if(text==null) return 0;
        float w=0;int previous=-1;
        for(int cp:text.codePoints().toArray()) {Glyph g=glyphs.get(cp);if(g==null)continue;w+=kern(previous,cp,size)+g.advance*size;previous=cp;}
        return w;
    }
    /** Delta's first-glyph ink-box alignment, used by its category headers. */
    public float centeredY(String text,float size,float centerY) {
        if(text==null||text.isEmpty()) return centerY-size/2f;
        Glyph g=glyphs.get(text.codePointAt(0));
        if(g==null||g.planeBounds==null)return centerY-size/2f;
        Bounds p=g.planeBounds;
        return centerY-((data.metrics.lineHeight+data.metrics.descender-p.top)+(p.top-p.bottom)/2f)*size;
    }
    /** Text is submitted to the GUI render state; coordinates and size use scaled GUI pixels. */
    public float draw(DrawContext context,String text,float x,float y,float size,int color) {
        if(text==null||text.isEmpty()||size<=0)return 0;
        float start=x, baseline=y+(data.metrics.lineHeight+data.metrics.descender)*size;int previous=-1;
        for(int cp:text.codePoints().toArray()) {
            Glyph g=glyphs.get(cp);if(g==null)continue; x+=kern(previous,cp,size);
            Bounds p=g.planeBounds,a=g.atlasBounds;
            if(p!=null && a!=null) {
                int tw=Math.round(a.right-a.left),th=Math.round(a.top-a.bottom);
                float w=(p.right-p.left)*size,h=(p.top-p.bottom)*size;
                if(tw>0&&th>0&&w>0&&h>0){
                    var m=context.getMatrices();m.pushMatrix();
                    m.translate(x,baseline-p.top*size);m.scale(w/tw,h/th);
                    context.drawTexture(pipeline,texture,0,0,a.left,data.atlas.height-a.top,tw,th,tw,th,data.atlas.width,data.atlas.height,color);
                    m.popMatrix();
                }
            }
            x+=g.advance*size; previous=cp;
        }
        return x-start;
    }
}
