package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Delta's two visual modes without altering server-supplied status effects. */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapFullBrightMixin {
    @Redirect(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;",ordinal=2))
    private Object interium$gamma(SimpleOption<?> option){return InteriumClient.fullBrightGamma()?Double.valueOf(InteriumClient.fullBrightGammaValue()):option.getValue();}
    @Redirect(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z",ordinal=0))
    private boolean interium$nightVisionFlag(ClientPlayerEntity player,RegistryEntry<StatusEffect> effect){return InteriumClient.fullBrightNightVision()||player.hasStatusEffect(effect);}
    @Redirect(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/render/GameRenderer;getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F"))
    private float interium$nightVisionStrength(LivingEntity entity,float tickDelta){return InteriumClient.fullBrightNightVision()?1f:GameRenderer.getNightVisionStrength(entity,tickDelta);}
}
