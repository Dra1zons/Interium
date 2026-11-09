package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WeatherRendering.class)
public abstract class DeltaWeatherRemovalMixin {
 @Inject(method="renderPrecipitation",at=@At("HEAD"),cancellable=true)
 private void interium$weather(VertexConsumerProvider v,Vec3d pos,WeatherRenderState state,CallbackInfo ci){if(DeltaVisuals.remove("weather",false))ci.cancel();}
 @Inject(method="addParticlesAndSound",at=@At("HEAD"),cancellable=true)
 private void interium$weatherParticles(CallbackInfo ci){if(DeltaVisuals.remove("weather",false))ci.cancel();}
}
