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
    public static volatile boolean auraTargetPresent;
    public static float aspectRatio(){int mode=Math.round(DeltaVisualSettings.number("aspect.mode",4,0,4));return switch(mode){case 0->4f/3;case 1->16f/9;case 2->1;case 3->1.6f;default->DeltaVisualSettings.number("aspect.value",1.9f,.1f,5);};}
    public static float invisAlpha(){return DeltaVisualSettings.number("invis.alpha",.5f,.1f,1);}
    /** Same five transforms as Delta SwingAnimation; caller replaces vanilla's ordinary item swing. */
    public static boolean swing(MatrixStack m,Hand hand,float progress,int arm){
        if(!DeltaVisualSettings.swingAnimation||hand!=Hand.MAIN_HAND||(DeltaVisualSettings.flag("swing.aura",true)&&!auraTargetPresent))return false;
        int mode=Math.round(DeltaVisualSettings.number("swing.mode",1,1,5));
        float anim=(float)Math.sin(progress*3.1415936112270124d),power=DeltaVisualSettings.number("swing.power",5,1,10)*10;
        float angle=DeltaVisualSettings.number("swing.angle",75,0,360),tip=DeltaVisualSettings.number("swing.tip",-20,-90,90);
        m.translate(arm*(mode==5?.5f:.72f),-.5f,mode==5?-.72f:-1);
        if(mode!=5)m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(-tip));
        switch(mode){
          case 1->{ry(m,arm*90);rz(m,arm*-70);rx(m,-angle-power*anim);}
          case 2->{ry(m,arm*90);rz(m,arm*-65);rx(m,-65+power*anim);}
          case 3->{ry(m,arm*-90);rz(m,arm*60);rx(m,30);rz(m,arm*power*anim);}
          case 4->{ry(m,arm*90);rz(m,arm*-75);rx(m,-45-power*anim);ry(m,arm*power*anim*.5f);}
          case 5->{float strength=power/80,swing=anim*anim,twist=(float)Math.sin(progress*progress*3.1415936112270124d);ry(m,arm*(45+twist*-20*strength));rz(m,arm*swing*-22*strength);rx(m,swing*-85*strength);ry(m,arm*-45);}
        }return true;
    }
    private static void rx(MatrixStack m,float d){m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(d));}
    private static void ry(MatrixStack m,float d){m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(d));}
    private static void rz(MatrixStack m,float d){m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(d));}

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
