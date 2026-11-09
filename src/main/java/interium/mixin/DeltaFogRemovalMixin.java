package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.block.enums.CameraSubmersionType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(StatusEffectFogModifier.class)
public abstract class DeltaFogRemovalMixin {
 @Shadow public abstract RegistryEntry<StatusEffect> getStatusEffect();
 @Inject(method="shouldApply",at=@At("HEAD"),cancellable=true)
 private void interium$fog(CameraSubmersionType submersion,Entity entity,CallbackInfoReturnable<Boolean> cir){var effect=getStatusEffect();if(effect.equals(StatusEffects.BLINDNESS)&&DeltaVisuals.remove("blindness",true)||effect.equals(StatusEffects.DARKNESS)&&DeltaVisuals.remove("darkness",true))cir.setReturnValue(false);}
}
