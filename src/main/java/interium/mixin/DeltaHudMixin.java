package interium.mixin;
import interium.fabric.DeltaVisuals;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameHud.class)
public abstract class DeltaHudMixin {
 @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
 private void interium$crosshair(DrawContext c,RenderTickCounter t,CallbackInfo ci){if(DeltaVisuals.crosshair(c,t))ci.cancel();}
 @Inject(method="renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",at=@At("HEAD"),cancellable=true)
 private void interium$scoreboard(DrawContext c,RenderTickCounter t,CallbackInfo ci){if(DeltaVisuals.remove("scoreboard",false))ci.cancel();}
 @Inject(method="renderBossBarHud",at=@At("HEAD"),cancellable=true)
 private void interium$bossbar(DrawContext c,RenderTickCounter t,CallbackInfo ci){if(DeltaVisuals.remove("bossbar",false))ci.cancel();}
 @Inject(method="renderPortalOverlay",at=@At("HEAD"),cancellable=true)
 private void interium$portal(DrawContext c,float strength,CallbackInfo ci){if(DeltaVisuals.remove("portal",true))ci.cancel();}
 @Inject(method="renderNauseaOverlay",at=@At("HEAD"),cancellable=true)
 private void interium$nausea(DrawContext c,float strength,CallbackInfo ci){if(DeltaVisuals.remove("nausea",true))ci.cancel();}
 @Inject(method="renderOverlay",at=@At("HEAD"),cancellable=true)
 private void interium$pumpkin(DrawContext c,Identifier texture,float opacity,CallbackInfo ci){if(DeltaVisuals.remove("pumpkin",true)&&texture.getPath().contains("pumpkinblur"))ci.cancel();}
}
