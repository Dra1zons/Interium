package interium.mixin;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(DrawContext.class)
public interface DeltaDrawContextAccess {
 @Accessor("state") GuiRenderState interium$state();
 @Accessor("scissorStack") DrawContext.ScissorStack interium$scissors();
}
