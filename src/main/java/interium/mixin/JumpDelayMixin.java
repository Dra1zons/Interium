package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class JumpDelayMixin {
    @Shadow private int jumpingCooldown;
    @Inject(method="tickMovement",at=@At("HEAD"))
    private void interium$removeJumpDelay(CallbackInfo ci){
        if(InteriumClient.removeJumpDelay((LivingEntity)(Object)this)) jumpingCooldown=0;
    }
}
