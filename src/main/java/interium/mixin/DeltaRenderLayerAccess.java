package interium.mixin;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(RenderLayer.class)
public interface DeltaRenderLayerAccess {
 @Invoker("of") static RenderLayer interium$create(String name,RenderSetup setup){throw new AssertionError();}
}
