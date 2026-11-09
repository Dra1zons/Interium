package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.block.enums.CameraSubmersionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(FogRenderer.class)
public class DeltaSubmersionRemovalMixin {
 @Inject(method="getCameraSubmersionType",at=@At("RETURN"),cancellable=true)
 private void interium$clearSubmersion(CallbackInfoReturnable<CameraSubmersionType> cir){
  if(DeltaVisuals.remove("water",false)&&(cir.getReturnValue()==CameraSubmersionType.WATER||cir.getReturnValue()==CameraSubmersionType.LAVA))cir.setReturnValue(CameraSubmersionType.NONE);
 }
}
