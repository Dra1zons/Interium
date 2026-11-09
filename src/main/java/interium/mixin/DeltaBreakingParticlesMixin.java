package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientWorld.class)
public class DeltaBreakingParticlesMixin {
 @Inject(method={"addBlockBreakParticles","spawnBlockBreakingParticle"},at=@At("HEAD"),cancellable=true)
 private void interium$removeBlockParticles(CallbackInfo ci){if(DeltaVisuals.remove("breakParticles",false))ci.cancel();}
}
