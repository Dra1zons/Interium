package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MinecraftClient.class)
public abstract class DeltaGlowRemovalMixin {
 @Inject(method="hasOutline",at=@At("HEAD"),cancellable=true)
 private void interium$glow(Entity e,CallbackInfoReturnable<Boolean> cir){if(DeltaVisuals.remove("glow",true))cir.setReturnValue(false);}
}
