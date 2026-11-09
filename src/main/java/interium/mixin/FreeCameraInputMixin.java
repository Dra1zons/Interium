package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardInput.class)
public abstract class FreeCameraInputMixin {
    @Inject(method="tick",at=@At("TAIL"))
    private void interium$cameraInput(CallbackInfo ci){InteriumClient.clearFreeCameraInput();}
}
