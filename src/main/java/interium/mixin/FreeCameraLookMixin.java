package interium.mixin;
import interium.fabric.InteriumClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class FreeCameraLookMixin {
    @Inject(method="changeLookDirection",at=@At("HEAD"),cancellable=true)
    private void interium$cameraLook(double x,double y,CallbackInfo ci){
        if(InteriumClient.freeCameraLook((Entity)(Object)this,x,y))ci.cancel();
    }
}
