package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Delta's Vanilla No Slow Down, adapted from tickMovement to 1.21.11's speed-factor method. */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerNoSlowMixin {
    @Redirect(method="applyMovementSpeedFactors",at=@At(value="INVOKE",target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean interium$itemSlow(ClientPlayerEntity player){return player.isUsingItem()&&!InteriumClient.bypassItemSlow();}
}
