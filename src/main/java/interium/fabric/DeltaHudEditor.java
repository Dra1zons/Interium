package interium.fabric;

import interium.fabric.delta.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.io.IOException;
import java.util.*;

/** Drag saved HUD anchors; no fake combat targets, cooldowns or staff records. */
final class DeltaHudEditor extends Screen {
    private final Screen parent;
    private String dragging;
    private float dx,dy;
    private DeltaFont font;
    DeltaHudEditor(Screen parent){super(Text.literal("Редактор HUD"));this.parent=parent;}
    @Override protected void init(){try{font=DeltaFont.load(client,"gt_regular");}catch(IOException e){throw new IllegalStateException(e);}}
    @Override public void renderBackground(DrawContext c,int x,int y,float d){}
    @Override public void render(DrawContext c,int mx,int my,float delta){DeltaHud.INSTANCE.render(client,c,null,true);float scale=DeltaHud.INSTANCE.scale(client);var m=c.getMatrices();m.pushMatrix();m.scale(scale,scale);for(var e:DeltaHud.INSTANCE.bounds().entrySet()){var b=e.getValue();DeltaRect.draw(c,b.x(),b.y(),b.w(),b.h(),4,.5f,0x90859cff);}String s="ЛКМ — перемещение · Esc — сохранить и назад";font.draw(c,s,8,8,7,DeltaVisualSettings.text());m.popMatrix();}
    @Override public boolean mouseClicked(Click click,boolean doubled){if(click.button()!=0)return false;float scale=DeltaHud.INSTANCE.scale(client);double x=click.x()/scale,y=click.y()/scale;List<String> ids=new ArrayList<>(DeltaHud.INSTANCE.bounds().keySet());Collections.reverse(ids);for(String id:ids){var b=DeltaHud.INSTANCE.bounds().get(id);if(b.contains(x,y)){dragging=id;dx=(float)x-b.x();dy=(float)y-b.y();return true;}}return false;}
    @Override public boolean mouseDragged(Click click,double rx,double ry){if(dragging==null||click.button()!=0)return false;float scale=DeltaHud.INSTANCE.scale(client);DeltaHud.INSTANCE.move(dragging,(float)click.x()/scale-dx,(float)click.y()/scale-dy);return true;}
    @Override public boolean mouseReleased(Click click){if(dragging==null)return false;dragging=null;DeltaVisualSettings.save();return true;}
    @Override public void close(){dragging=null;DeltaVisualSettings.save();client.setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
}
