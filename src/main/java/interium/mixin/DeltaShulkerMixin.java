package interium.mixin;
import interium.fabric.DeltaShulkerPreview;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HandledScreen.class)
public abstract class DeltaShulkerMixin {
 @Shadow protected Slot focusedSlot;
 @Inject(method="drawMouseoverTooltip",at=@At("HEAD"),cancellable=true)
 private void interium$preview(DrawContext c,int x,int y,CallbackInfo ci){if(focusedSlot!=null&&focusedSlot.hasStack()&&DeltaShulkerPreview.draw(c,focusedSlot.getStack(),x,y))ci.cancel();}
}
