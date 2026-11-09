package interium.mixin;
import interium.fabric.DeltaGlass;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public class DeltaGlassMixin {
 @Inject(method="render",at=@At("HEAD")) private void interium$prepareGlass(CallbackInfo ci){DeltaGlass.beginFrame();}
 @Inject(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
 private void interium$captureGlass(CallbackInfo ci){DeltaGlass.capture();}
 @Inject(method="close",at=@At("HEAD")) private void interium$closeGlass(CallbackInfo ci){DeltaGlass.close();}
}
