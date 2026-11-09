package interium.mixin;
import interium.fabric.DeltaVisualSettings;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GameRenderer.class)
public abstract class DeltaAspectMixin {
 @Inject(method="getBasicProjectionMatrix",at=@At("RETURN"),cancellable=true)
 private void interium$aspect(float fov,CallbackInfoReturnable<Matrix4f> cir){
  if(!DeltaVisualSettings.aspectRatio)return;
  var w=MinecraftClient.getInstance().getWindow();
  float nativeAspect=(float)w.getFramebufferWidth()/Math.max(1,w.getFramebufferHeight());
  Matrix4f result=new Matrix4f(cir.getReturnValue());result.m00(result.m00()*nativeAspect/DeltaVisuals.aspectRatio());cir.setReturnValue(result);
 }
}
