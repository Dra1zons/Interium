package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LightmapTextureManager.class)
public abstract class DeltaDarknessRemovalMixin {
 @Inject(method="getDarkness",at=@At("HEAD"),cancellable=true)
 private void interium$darkness(LivingEntity e,float factor,float delta,CallbackInfoReturnable<Float> cir){if(DeltaVisuals.remove("darkness",true))cir.setReturnValue(0f);}
}
