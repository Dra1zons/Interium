package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MinecraftClient.class)
public class DeltaShaderOutlineMixin {
 @Inject(method="hasOutline",at=@At("HEAD"),cancellable=true)
 private void interium$outline(Entity entity,CallbackInfoReturnable<Boolean> ci){if(DeltaVisuals.shaderOutline(entity))ci.setReturnValue(true);}
}
