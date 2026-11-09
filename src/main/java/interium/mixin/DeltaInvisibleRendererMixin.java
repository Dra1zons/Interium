package interium.mixin;
import interium.fabric.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntityRenderer.class)
public abstract class DeltaInvisibleRendererMixin {
 @Inject(method="updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
 private void interium$capture(LivingEntity e,LivingEntityRenderState state,float delta,CallbackInfo ci){((DeltaInvisibleState)state).interium$reveal(DeltaVisualSettings.seeInvisibles&&e instanceof PlayerEntity&&state.invisible,DeltaVisuals.invisAlpha());}
 @Inject(method="isVisible",at=@At("HEAD"),cancellable=true)
 private void interium$visible(LivingEntityRenderState state,CallbackInfoReturnable<Boolean> cir){if(((DeltaInvisibleState)state).interium$reveal())cir.setReturnValue(true);}
 @Inject(method="getMixColor",at=@At("RETURN"),cancellable=true)
 private void interium$alpha(LivingEntityRenderState state,CallbackInfoReturnable<Integer> cir){var data=(DeltaInvisibleState)state;if(data.interium$reveal()){int c=cir.getReturnValue();cir.setReturnValue((Math.round((c>>>24)*data.interium$opacity())<<24)|(c&0xffffff));}}
}
