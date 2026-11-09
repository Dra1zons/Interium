package interium.mixin;
import interium.fabric.DeltaItemState;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(ItemEntityRenderState.class)
public abstract class DeltaItemStateMixin implements DeltaItemState {
 @Unique private boolean interium$active,interium$ground,interium$small;
 public boolean interium$physics(){return interium$active;}
 public boolean interium$ground(){return interium$ground;}
 public boolean interium$small(){return interium$small;}
 public void interium$physics(boolean active,boolean ground,boolean small){interium$active=active;interium$ground=ground;interium$small=small;}
}
