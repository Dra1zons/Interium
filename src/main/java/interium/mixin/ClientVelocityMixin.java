package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Only Delta's ordinary mode; it does not cancel explosion packet knockback. */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientVelocityMixin {
    @Inject(method="onEntityVelocityUpdate",at=@At(value="INVOKE",target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/network/PacketApplyBatcher;)V",shift=At.Shift.AFTER),cancellable=true)
    private void interium$velocity(EntityVelocityUpdateS2CPacket packet,CallbackInfo ci){
        var player=MinecraftClient.getInstance().player;
        if(InteriumClient.cancelVelocity() && player!=null && packet.getEntityId()==player.getId())ci.cancel();
    }
}
