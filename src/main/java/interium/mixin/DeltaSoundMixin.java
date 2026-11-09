package interium.mixin;
import interium.fabric.DeltaWorldVisuals;
import net.minecraft.client.sound.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SoundManager.class)
public abstract class DeltaSoundMixin {
 @Inject(method="play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",at=@At("HEAD"))
 private void interium$sound(SoundInstance sound,CallbackInfoReturnable<SoundSystem.PlayResult> cir){DeltaWorldVisuals.sound(sound);}
}
