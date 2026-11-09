package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ClientPlayerInteractionManager.class)
public abstract class FreeCameraInteractionMixin {
    @Inject(method={"attackEntity","clickSlot"},at=@At("HEAD"),cancellable=true)
    private void interium$cameraInteraction(CallbackInfo ci){if(InteriumClient.freeCameraActive())ci.cancel();}
    @Inject(method={"attackBlock","updateBlockBreakingProgress"},at=@At("HEAD"),cancellable=true)
    private void interium$cameraBlock(CallbackInfoReturnable<Boolean> cir){if(InteriumClient.freeCameraActive())cir.setReturnValue(false);}
    @Inject(method={"interactItem","interactBlock","interactEntity","interactEntityAtLocation"},at=@At("HEAD"),cancellable=true)
    private void interium$cameraUse(CallbackInfoReturnable<ActionResult> cir){if(InteriumClient.freeCameraActive())cir.setReturnValue(ActionResult.PASS);}
}
