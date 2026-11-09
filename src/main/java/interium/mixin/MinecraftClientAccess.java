package interium.mixin;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccess {
    @Accessor("itemUseCooldown") void interium$setItemUseCooldown(int value);
    @Invoker("doAttack") boolean interium$attack();
}
