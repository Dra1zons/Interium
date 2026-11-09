package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityPushMixin {
    @Inject(method="pushAwayFrom",at=@At("HEAD"),cancellable=true)
    private void interium$avoidEntityPush(Entity other, CallbackInfo ci){
        if(InteriumClient.avoidEntityPush((Entity)(Object)this,other))ci.cancel();
    }
}
