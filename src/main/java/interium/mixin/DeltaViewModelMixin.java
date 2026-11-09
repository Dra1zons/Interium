package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HeldItemRenderer.class)
public abstract class DeltaViewModelMixin {
 @Inject(method="renderFirstPersonItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/util/math/MatrixStack;push()V",ordinal=0,shift=At.Shift.AFTER))
 private void interium$view(AbstractClientPlayerEntity player,float tick,float pitch,Hand hand,float swing,ItemStack item,float equip,MatrixStack m,OrderedRenderCommandQueue queue,int light,CallbackInfo ci){DeltaVisuals.viewModel(m,hand);}
}
