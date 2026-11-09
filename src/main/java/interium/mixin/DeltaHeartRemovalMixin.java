package interium.mixin;
import interium.fabric.DeltaVisuals;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.*;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(targets="net.minecraft.client.gui.hud.InGameHud$HeartType")
public abstract class DeltaHeartRemovalMixin {
 @WrapOperation(method="fromPlayerState",at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/PlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
 private static boolean interium$hearts(PlayerEntity p,RegistryEntry<StatusEffect> effect,Operation<Boolean> original){return !(effect.equals(StatusEffects.WITHER)&&DeltaVisuals.remove("hearts",true))&&original.call(p,effect);}
}
