package interium.mixin;
import interium.fabric.TntCountdownState;
import net.minecraft.client.render.entity.state.TntEntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(TntEntityRenderState.class)
public abstract class TntCountdownStateMixin implements TntCountdownState {
    @Unique private Text interium$countdown;
    @Unique private double interium$countdownHeight;
    public Text interium$getCountdown(){return interium$countdown;}
    public double interium$getCountdownHeight(){return interium$countdownHeight;}
    public void interium$setCountdown(Text text,double height){interium$countdown=text;interium$countdownHeight=height;}
}
