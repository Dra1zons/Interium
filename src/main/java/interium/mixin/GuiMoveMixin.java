package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class GuiMoveMixin {
    @Inject(method="tick",at=@At("HEAD"))
    private void interium$syncScreenKeys(CallbackInfo ci){ InteriumClient.syncGuiMove(); }
}
