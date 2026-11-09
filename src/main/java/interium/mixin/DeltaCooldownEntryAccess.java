package interium.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets="net.minecraft.entity.player.ItemCooldownManager$Entry")
public interface DeltaCooldownEntryAccess {
 @Accessor("endTick") int interium$endTick();
}
