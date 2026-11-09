package interium.fabric;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryStack;
import java.util.*;
/** Actual original Delta five-octave FBM/ridged noise, masked by before/after hand depth.
 * RenderPass adaptation for 1.21.11, with resize/stop cleanup. Not a flat hand tint.
 */
public final class DeltaHandsShader {
 private static final RenderPipeline PIPELINE=RenderPipeline.builder().withLocation(Identifier.of("interium","pipeline/delta_hands"))
  .withVertexShader(Identifier.of("interium","core/delta_hands")).withFragmentShader(Identifier.of("interium","core/delta_hands"))
  .withUniform("HandsParams",UniformType.UNIFORM_BUFFER).withSampler("Sampler1").withSampler("Sampler2")
  .withVertexFormat(VertexFormats.EMPTY,VertexFormat.DrawMode.TRIANGLES).withBlend(BlendFunction.TRANSLUCENT)
  .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
 private static SimpleFramebuffer before;private static GpuBuffer params;private static GpuSampler sampler;private static boolean captured;
 public static void init(){ClientLifecycleEvents.CLIENT_STOPPING.register(c->close());}
 public static void before(){captured=false;var mc=MinecraftClient.getInstance();if(!DeltaVisualSettings.handsShader||mc.player==null||!mc.options.getPerspective().isFirstPerson())return;var main=mc.getFramebuffer();if(main.getDepthAttachment()==null)return;if(before==null||before.textureWidth!=main.textureWidth||before.textureHeight!=main.textureHeight){close();before=new SimpleFramebuffer("Interium Delta hands depth",main.textureWidth,main.textureHeight,true);}before.copyDepthFrom(main);captured=true;}
 public static void after(){if(!captured)return;captured=false;var main=MinecraftClient.getInstance().getFramebuffer();if(before==null||main.getDepthAttachmentView()==null)return;var device=RenderSystem.getDevice();if(params==null)params=device.createBuffer(()->"Interium Delta noise parameters",GpuBuffer.USAGE_UNIFORM|GpuBuffer.USAGE_COPY_DST,32);if(sampler==null)sampler=device.createSampler(AddressMode.CLAMP_TO_EDGE,AddressMode.CLAMP_TO_EDGE,FilterMode.NEAREST,FilterMode.NEAREST,1,OptionalDouble.empty());var commands=device.createCommandEncoder();int tint=DeltaVisualSettings.primary();try(var stack=MemoryStack.stackPush()){var data=stack.malloc(32);data.putFloat(((tint>>16)&255)/255f).putFloat(((tint>>8)&255)/255f).putFloat((tint&255)/255f).putFloat(DeltaVisualSettings.number("hands.opacity",.6f,0,1)).putFloat((System.currentTimeMillis()%100000)/1000f).putFloat(0).putFloat(0).putFloat(0).flip();commands.writeToBuffer(params.slice(),data);}try(var pass=commands.createRenderPass(()->"Interium original Delta hands noise",main.getColorAttachmentView(),OptionalInt.empty())){pass.setPipeline(PIPELINE);pass.setUniform("HandsParams",params);pass.bindTexture("Sampler1",main.getDepthAttachmentView(),sampler);pass.bindTexture("Sampler2",before.getDepthAttachmentView(),sampler);pass.draw(0,3);}}
 public static void close(){captured=false;if(before!=null){before.delete();before=null;}if(params!=null){params.close();params=null;}if(sampler!=null){sampler.close();sampler=null;}}
}
