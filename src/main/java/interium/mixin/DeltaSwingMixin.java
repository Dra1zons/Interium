package interium.mixin;
import interium.fabric.DeltaVisuals;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(HeldItemRenderer.class)
public abstract class DeltaSwingMixin {
 @WrapOperation(method="renderFirstPersonItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V",ordinal=2))
 private void interium$swing(HeldItemRenderer renderer,float progress,MatrixStack m,int sign,Arm arm,Operation<Void> original,@Local(argsOnly=true) Hand hand){if(!DeltaVisuals.swing(m,hand,progress,sign))original.call(renderer,progress,m,sign,arm);}
}
