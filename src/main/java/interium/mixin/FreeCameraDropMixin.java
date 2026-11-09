package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ClientPlayerEntity.class)
public abstract class FreeCameraDropMixin {
    @Inject(method="dropSelectedItem",at=@At("HEAD"),cancellable=true)
    private void interium$cameraDrop(CallbackInfoReturnable<Boolean> cir){if(InteriumClient.freeCameraActive())cir.setReturnValue(false);}
}
