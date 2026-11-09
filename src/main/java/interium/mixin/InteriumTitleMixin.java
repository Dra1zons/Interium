package interium.mixin;
import interium.fabric.InteriumTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(MinecraftClient.class)
public class InteriumTitleMixin {
 @ModifyVariable(method="setScreen",at=@At("HEAD"),argsOnly=true)
 private Screen interium$startupScreen(Screen screen){return screen instanceof TitleScreen&&!(screen instanceof InteriumTitleScreen)?new InteriumTitleScreen():screen;}
}
