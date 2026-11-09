package interium.mixin;
import interium.fabric.DeltaInvisibleState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(LivingEntityRenderState.class)
public abstract class DeltaInvisibleStateMixin implements DeltaInvisibleState {
 @Unique private boolean interium$reveal;
 @Unique private float interium$opacity=1;
 public boolean interium$reveal(){return interium$reveal;}
 public float interium$opacity(){return interium$opacity;}
 public void interium$reveal(boolean value,float opacity){interium$reveal=value;interium$opacity=opacity;}
}
