package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameOverlayRenderer.class)
public abstract class DeltaOverlayRemovalMixin {
 @Inject(method="renderFireOverlay",at=@At("HEAD"),cancellable=true)
 private static void interium$fire(MatrixStack m,VertexConsumerProvider v,Sprite s,CallbackInfo ci){if(DeltaVisuals.remove("fire",true))ci.cancel();}
 @Inject(method="renderUnderwaterOverlay",at=@At("HEAD"),cancellable=true)
 private static void interium$water(MinecraftClient mc,MatrixStack m,VertexConsumerProvider v,CallbackInfo ci){if(DeltaVisuals.remove("water",false))ci.cancel();}
}
