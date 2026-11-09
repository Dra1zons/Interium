package interium.mixin;
import interium.fabric.DeltaHandsShader;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public class DeltaHandsMixin {
 @Inject(method="renderHand",at=@At("HEAD"))private void interium$before(float tick,boolean sleeping,Matrix4f projection,CallbackInfo ci){DeltaHandsShader.before();}
 @Inject(method="renderHand",at=@At("RETURN"))private void interium$after(float tick,boolean sleeping,Matrix4f projection,CallbackInfo ci){DeltaHandsShader.after();}
}
