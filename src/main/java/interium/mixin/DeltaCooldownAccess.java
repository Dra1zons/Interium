package interium.mixin;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;
@Mixin(ItemCooldownManager.class)
public interface DeltaCooldownAccess {
 @Accessor("entries") Map<Identifier,?> interium$entries();
 @Accessor("tick") int interium$tick();
}
