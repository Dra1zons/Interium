package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(MinecraftClient.class)
public abstract class FreeCameraActionsMixin {
    @Inject(method="doAttack",at=@At("HEAD"),cancellable=true)
    private void interium$cameraAttack(CallbackInfoReturnable<Boolean> cir){if(InteriumClient.freeCameraActive())cir.setReturnValue(false);}
    @Inject(method={"doItemUse","doItemPick","handleBlockBreaking"},at=@At("HEAD"),cancellable=true)
    private void interium$cameraAction(CallbackInfo ci){if(InteriumClient.freeCameraActive())ci.cancel();}
}
