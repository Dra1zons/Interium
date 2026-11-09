package interium.fabric;
import interium.fabric.delta.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
/** Native editing/clipboard behavior, rendered with original Delta GT instead of the pixel font. */
final class DeltaTextSettingScreen extends Screen {
 private final Screen parent;private final String key;private TextFieldWidget input;private DeltaFont font;private int start;
 DeltaTextSettingScreen(Screen parent,String key,String title){super(Text.literal(title));this.parent=parent;this.key=key;}
 @Override protected void init(){try{font=DeltaFont.load(client,"gt_regular");}catch(java.io.IOException e){throw new IllegalStateException(e);}input=addSelectableChild(new TextFieldWidget(textRenderer,12,height/2,Math.max(10,width-24),24,getTitle()));input.setMaxLength(2048);input.setDrawsBackground(false);input.setText(DeltaVisualSettings.string(key,""));input.setChangedListener(v->DeltaVisualSettings.set(key,v));setInitialFocus(input);}
 @Override public void renderBackground(DrawContext c,int mx,int my,float delta){}
 @Override public void render(DrawContext c,int mx,int my,float delta){DeltaRect.draw(c,6,height/2f-36,width-12,102,6,0,DeltaVisualSettings.background());font.draw(c,getTitle().getString(),12,height/2f-24,12,DeltaVisualSettings.text());String s=input.getText();int cursor=input.getCursor();start=Math.min(start,cursor);while(start<cursor&&font.width(s.substring(start,cursor),11)>width-32)start++;String shown=s.substring(start);while(!shown.isEmpty()&&font.width(shown,11)>width-30)shown=shown.substring(0,shown.offsetByCodePoints(shown.length(),-1));c.enableScissor(12,height/2,width-12,height/2+25);font.draw(c,shown,12,height/2f+3,11,DeltaVisualSettings.text());if(System.currentTimeMillis()%1000<500){float x=12+font.width(s.substring(start,cursor),11);c.fill((int)x,height/2+2,(int)x+1,height/2+15,DeltaVisualSettings.primary());}c.disableScissor();font.draw(c,"Ctrl+A / Ctrl+V · Enter или Esc: сохранить и назад",12,height/2f+40,8,DeltaVisualSettings.text());}
 @Override public boolean mouseClicked(Click click,boolean doubled){if(click.y()>=height/2&&click.y()<height/2+24){String s=input.getText();int position=start;for(int i=start;i<s.length();){int next=s.offsetByCodePoints(i,1);if(12+font.width(s.substring(start,next),11)>click.x())break;position=next;i=next;}input.setCursor(position,false);setFocused(input);input.setFocused(true);return true;}return super.mouseClicked(click,doubled);}
 @Override public boolean keyPressed(KeyInput input){if(input.key()==257||input.key()==335){close();return true;}return super.keyPressed(input);}
 @Override public void close(){DeltaVisualSettings.save();client.setScreen(parent);}
 @Override public boolean shouldPause(){return false;}
}
