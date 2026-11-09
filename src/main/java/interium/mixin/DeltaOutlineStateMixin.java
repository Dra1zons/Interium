package interium.mixin;
import interium.fabric.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Native shader outline adapter; donor ShaderESP itself contains no implementation. */
@Mixin(EntityRenderer.class)
public class DeltaOutlineStateMixin {
 @Inject(method="updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V",at=@At("TAIL"))
 private void interium$color(Entity entity,EntityRenderState state,float tick,CallbackInfo ci){if(DeltaVisuals.shaderOutline(entity))state.outlineColor=DeltaVisualSettings.primary();}
}
