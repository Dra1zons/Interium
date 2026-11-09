package interium.mixin;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ButtonWidget.class)
public interface DeltaButtonActionAccess {
 @Accessor("onPress") ButtonWidget.PressAction interium$action();
}
