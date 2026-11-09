package interium.mixin;
import interium.fabric.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemEntityRenderer.class)
public abstract class DeltaItemPhysicMixin {
 @Inject(method="updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V",at=@At("TAIL"))
 private void interium$capture(ItemEntity e,ItemEntityRenderState state,float delta,CallbackInfo ci){((DeltaItemState)state).interium$physics(DeltaVisualSettings.itemPhysic,e.isOnGround(),DeltaVisuals.smallItems());}
 @WrapOperation(method="render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
 private void interium$hover(MatrixStack m,float x,float y,float z,Operation<Void> original,@Local(argsOnly=true) ItemEntityRenderState state){original.call(m,x,((DeltaItemState)state).interium$physics()?0f:y,z);}
 @WrapOperation(method="render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V"))
 private void interium$rotate(MatrixStack m,Quaternionfc q,Operation<Void> original,@Local(argsOnly=true) ItemEntityRenderState state){if(!((DeltaItemState)state).interium$physics())original.call(m,q);}
 @Inject(method="render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/render/entity/ItemEntityRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/ItemStackEntityRenderState;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/Box;)V"))
 private void interium$physics(ItemEntityRenderState state,MatrixStack m,OrderedRenderCommandQueue queue,CameraRenderState camera,CallbackInfo ci){var data=(DeltaItemState)state;if(!data.interium$physics())return;if(data.interium$small())m.scale(.5f,.5f,.5f);m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(data.interium$ground()?90:ItemEntity.getRotation(state.age,state.uniqueOffset)*300));}
}
