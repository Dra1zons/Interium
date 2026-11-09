package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Camera.class)
public abstract class DeltaCameraRemovalMixin {
 @Inject(method="clipToSpace",at=@At("HEAD"),cancellable=true)
 private void interium$clip(float distance,CallbackInfoReturnable<Float> cir){if(DeltaVisuals.remove("clip",true))cir.setReturnValue(distance);}
}
