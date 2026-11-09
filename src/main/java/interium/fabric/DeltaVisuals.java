package interium.fabric;

import interium.fabric.delta.DeltaRect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/** Source-based Delta Crosshair and View Model; render-only, no gameplay mutation. */
public final class DeltaVisuals {
    private DeltaVisuals(){}
    public static boolean remove(String effect,boolean fallback){return DeltaVisualSettings.removals&&DeltaVisualSettings.flag("remove."+effect,fallback);}
    public static void viewModel(MatrixStack matrices,Hand hand){if(!DeltaVisualSettings.viewModel)return;String prefix="view."+(hand==Hand.MAIN_HAND?"main":"off")+".";matrices.translate(DeltaVisualSettings.number(prefix+"x",0,-2,2),DeltaVisualSettings.number(prefix+"y",0,-2,2),DeltaVisualSettings.number(prefix+"z",0,-2,2));}
    /** Replaces vanilla only when Delta's crosshair can actually be drawn. */
    public static boolean crosshair(DrawContext c,RenderTickCounter tick){var mc=MinecraftClient.getInstance();if(!DeltaVisualSettings.crosshair||mc.player==null||mc.options.hudHidden||!mc.options.getPerspective().isFirstPerson()||mc.player.isSpectator())return false;
        float cx=mc.getWindow().getScaledWidth()/2f,cy=mc.getWindow().getScaledHeight()/2f;
        float cooldown=1-mc.player.getAttackCooldownProgress(tick.getTickProgress(false));
        float gap=DeltaVisualSettings.number("crosshair.gap",0,0,6)+(DeltaVisualSettings.flag("crosshair.adaptive",false)?8*cooldown:0);
        float len=DeltaVisualSettings.number("crosshair.length",2.5f,2,5);
        int color=mc.crosshairTarget instanceof EntityHitResult?0xffff4040:0xffffffff;
        boolean outline=DeltaVisualSettings.flag("crosshair.outline",true);
        segment(c,cx+gap,cy-.5f,len,1,color,outline);segment(c,cx-gap-len,cy-.5f,len,1,color,outline);
        segment(c,cx-.5f,cy-gap-len,1,len,color,outline);segment(c,cx-.5f,cy+gap,1,len,color,outline);
        if(gap>0&&DeltaVisualSettings.flag("crosshair.dot",false))segment(c,cx-.5f,cy-.5f,1,1,color,outline);
        return true;
    }
    private static void segment(DrawContext c,float x,float y,float w,float h,int color,boolean outline){if(outline)quad(c,x-.5f,y-.5f,w+1,h+1,0xff000000);quad(c,x,y,w,h,color);}
    private static void quad(DrawContext c,float x,float y,float w,float h,int color){var m=c.getMatrices();m.pushMatrix();m.translate(x,y);m.scale(w,h);c.fill(0,0,1,1,color);m.popMatrix();}
}
