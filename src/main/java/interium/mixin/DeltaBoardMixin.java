package interium.mixin;
import interium.fabric.DeltaBoardSpoofer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(InGameHud.class)
public abstract class DeltaBoardMixin {
 @ModifyArg(method="renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V"),index=1)
 private Text interium$line(Text text){return DeltaBoardSpoofer.replace(text);}
 @ModifyArg(method="renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I"),index=0)
 private StringVisitable interium$width(StringVisitable text){return text instanceof Text value?DeltaBoardSpoofer.replace(value):text;}
}
