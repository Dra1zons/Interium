package interium.mixin;
import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Input.class)
public interface InputAccess {
    @Accessor("movementVector") void interium$setMovementVector(Vec2f value);
}
