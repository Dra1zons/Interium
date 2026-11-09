package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method="onKey",at=@At("HEAD"))
    private void interium$bindKey(long window,int action,KeyInput input,CallbackInfo ci){
        InteriumClient.handleModuleBind(input.key(),action);
    }
}
