package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public abstract class DeltaHurtCameraMixin {
 @Inject(method="tiltViewWhenHurt",at=@At("HEAD"),cancellable=true)
 private void interium$hurt(MatrixStack m,float delta,CallbackInfo ci){if(DeltaVisuals.remove("hurt",true))ci.cancel();}
}
