package interium.fabric;
import interium.fabric.delta.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.*;
import java.io.IOException;

/** Explicit, reversible Interium-only budgets, not a video settings rewrite. */
public final class PerformanceScreen extends Screen {
 private final Screen parent;private DeltaFont font;
 private int x,y,w,h;private final List<ButtonWidget> buttons=new ArrayList<>();
 private static final String[] NAMES={"Lite","Balanced","Quality"};
 private static final String[] DESC={"Без GPU blur и glow · минимальная нагрузка","Blur 1/4 · один слой свечения","Blur 1/2 · полное свечение"};
 public PerformanceScreen(Screen parent){super(Text.literal("Performance"));this.parent=parent;}
 @Override protected void init(){try{font=DeltaFont.load(client,"onest_regular");}catch(IOException e){throw new IllegalStateException(e);}
  w=Math.min(360,width-20);h=206;x=(width-w)/2;y=Math.max(8,(height-h)/2);buttons.clear();
  for(int i=0;i<3;i++){final int q=i;buttons.add(addDrawableChild(ButtonWidget.builder(Text.literal(NAMES[i]),b->DeltaPerformance.choose(q)).dimensions(x+12,y+43+i*41,w-24,35).build()));}
  buttons.add(addDrawableChild(ButtonWidget.builder(Text.literal("Назад"),b->close()).dimensions(x+w-86,y+h-30,74,20).build()));
 }
 @Override public void renderBackground(DrawContext c,int mx,int my,float delta){}
 @Override public void render(DrawContext c,int mx,int my,float delta){
  if(client.world==null)c.fillGradient(0,0,width,height,0xff0b0c14,0xff18131f);
  DeltaGlass.draw(c,x,y,w,h,10,DeltaVisualSettings.background());font.draw(c,"Performance",x+14,y+12,12,DeltaVisualSettings.text());font.draw(c,"Только Interium · настройки игры не меняются",x+14,y+29,7,DeltaVisualSettings.primary());
  for(int i=0;i<buttons.size();i++){var b=buttons.get(i);boolean selected=i<3&&DeltaPerformance.quality()==i,hot=b.isFocused()||(mx>=b.getX()&&mx<b.getX()+b.getWidth()&&my>=b.getY()&&my<b.getY()+b.getHeight());
   DeltaRect.draw(c,b.getX(),b.getY(),b.getWidth(),b.getHeight(),6,0,((selected?60:hot?32:17)<<24)|(DeltaVisualSettings.primary()&0xffffff));
   font.draw(c,b.getMessage().getString(),b.getX()+9,b.getY()+6,8,DeltaVisualSettings.text());if(i<3)font.draw(c,DESC[i],b.getX()+9,b.getY()+20,6.5f,DeltaVisualSettings.text());
  }
 }
 @Override public void close(){client.setScreen(parent);}
 @Override public boolean shouldPause(){return false;}
}
