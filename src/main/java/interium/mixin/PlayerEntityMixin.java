package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method="clipAtLedge", at=@At("HEAD"), cancellable=true)
    private void interium$preventWalkingOffEdge(CallbackInfoReturnable<Boolean> result) {
        if(InteriumClient.useSafeWalk((PlayerEntity)(Object)this)) result.setReturnValue(true);
    }
}
