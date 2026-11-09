package interium.mixin;
import interium.fabric.TntCountdownRenderer;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.render.entity.state.TntEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(TntEntityRenderer.class)
public abstract class TntCountdownMixin {
    @Inject(method="updateRenderState(Lnet/minecraft/entity/TntEntity;Lnet/minecraft/client/render/entity/state/TntEntityRenderState;F)V",at=@At("TAIL"))
    private void interium$extractCountdown(TntEntity entity,TntEntityRenderState state,float tickProgress,CallbackInfo ci){
        TntCountdownRenderer.update(entity,state);
    }
    @Inject(method="render(Lnet/minecraft/client/render/entity/state/TntEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",at=@At("TAIL"))
    private void interium$renderCountdown(TntEntityRenderState state,MatrixStack matrices,OrderedRenderCommandQueue queue,CameraRenderState camera,CallbackInfo ci){
        TntCountdownRenderer.render(state,matrices,queue,camera);
    }
}
