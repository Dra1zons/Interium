package interium.mixin;
import net.minecraft.client.input.Input;import net.minecraft.util.math.Vec2f;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Input.class) public interface DeltaInputAccess {@Accessor("movementVector") void interium$movement(Vec2f value);}
