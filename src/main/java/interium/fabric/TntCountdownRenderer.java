package interium.fabric;

import interium.core.TntTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.TntEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;

/** Independent 1.21.11 implementation of the reference's observable TNT label behavior.
 * No old JAR classes, configuration library or 1.16 rendering calls are embedded.
 */
public final class TntCountdownRenderer {
    private TntCountdownRenderer(){}
    public static void update(TntEntity entity,TntEntityRenderState state){
        var data=(TntCountdownState)state;
        Text label=InteriumClient.tntTimerEnabled && entity.isAlive() && !entity.isRemoved() && !entity.hasPassengers()
                ?Text.literal(TntTimer.format(entity.getFuse(),InteriumState.tntTimerTicks)):null;
        data.interium$setCountdown(label,entity.getHeight()+.5);
    }
    public static void render(TntEntityRenderState state,MatrixStack matrices,
                              OrderedRenderCommandQueue queue,CameraRenderState camera){
        if(!InteriumClient.tntTimerEnabled)return;
        var client=MinecraftClient.getInstance();
        if(client.world==null || client.options.hudHidden || camera.orientation==null)return;
        var data=(TntCountdownState)state;
        Text label=data.interium$getCountdown();
        if(label==null)return;
        // TntEntityRenderer already supplies the interpolated entity-local transform
        // and vanilla entity/frustum culling. Do not subtract camera position again.
        matrices.push();
        try{
            matrices.translate(0,data.interium$getCountdownHeight(),0);
            matrices.multiply(camera.orientation);
            matrices.scale(.025f,-.025f,.025f);
            float x=-client.textRenderer.getWidth(label)/2f;
            // Reference: faint see-through pass + opaque depth-tested white pass.
            // Full-bright text only: the TNT's own lighting is left untouched.
            queue.submitText(matrices,x,0,label.asOrderedText(),false,TextRenderer.TextLayerType.SEE_THROUGH,
                    15728640,0x20ffffff,0x3f000000,0);
            queue.submitText(matrices,x,0,label.asOrderedText(),false,TextRenderer.TextLayerType.NORMAL,
                    15728640,0xffffffff,0,0);
        }finally{matrices.pop();}
    }
}
