package interium.fabric;

import interium.fabric.delta.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.io.IOException;
import java.util.*;

/** A real replacement for the startup screen, retaining vanilla navigation and its restrictions. */
public final class InteriumTitleScreen extends TitleScreen {
 private DeltaFont font;
 private final List<Entry> entries=new ArrayList<>();
 private final Map<ButtonWidget,Float> hover=new IdentityHashMap<>();
 private record Entry(ButtonWidget button,String label){}
 private float panelX,panelY,panelW,panelH,opening;
 private long previous;
 private int vanillaActions;
 public InteriumTitleScreen(){super(false);}
 @Override protected void init(){
  super.init();InteriumLogo.window(client);
  List<ButtonWidget> original=new ArrayList<>();for(var child:children())if(child instanceof ButtonWidget button)original.add(button);
  vanillaActions=original.size();clearChildren();entries.clear();hover.clear();
  try{font=DeltaFont.load(client,"onest_regular");}catch(IOException ex){throw new IllegalStateException("Interium title font",ex);}
  int row=height<300?21:28,gap=4,padding=12;panelW=Math.min(350,width-28);panelX=(width-panelW)/2;
  int full=Math.min(3,original.size()),remaining=original.size()-full,rows=full+(remaining+1)/2+1;
  panelH=padding*2+rows*(row+gap)-gap;
  panelY=Math.max(52,(height-panelH)/2+20);if(panelY+panelH>height-25)panelY=height-panelH-25;
  int y=(int)panelY+padding,x=(int)panelX+padding,wide=(int)panelW-2*padding;
  for(int i=0;i<full;i++){ButtonWidget old=original.get(i);addAction(old.getMessage(),old.active,b->activate(old),x,y,wide,row);y+=row+gap;}
  for(int i=full;i<original.size();i++){ButtonWidget old=original.get(i);int col=(i-full)%2;addAction(old.getMessage(),old.active,b->activate(old),x+col*((wide+gap)/2),y,(wide-gap)/2,row);if(col==1||i==original.size()-1)y+=row+gap;}
  addAction(Text.literal("Themes"),true,b->client.setScreen(new ThemeEditorScreen(this)),x,y,(wide-gap)/2,row);
  addAction(Text.literal("Performance"),true,b->client.setScreen(new PerformanceScreen(this)),x+(wide+gap)/2,y,(wide-gap)/2,row);
 }
 private static void activate(ButtonWidget button){((interium.mixin.DeltaButtonActionAccess)(Object)button).interium$action().onPress(button);}
 private void addAction(Text message,boolean active,ButtonWidget.PressAction action,int x,int y,int w,int h){ButtonWidget b=addDrawableChild(ButtonWidget.builder(message,action).dimensions(x,y,w,h).build());b.active=active;entries.add(new Entry(b,message.getString()));}
 @Override public void renderBackground(DrawContext c,int mx,int my,float delta){}
 @Override public void render(DrawContext c,int mx,int my,float delta){
  long now=System.nanoTime();float dt=previous==0?.016f:Math.min(.05f,(now-previous)/1e9f);previous=now;
  float ease=DeltaVisualSettings.motion()==0?1:1-(float)Math.exp(-dt*12/Math.max(.2f,DeltaVisualSettings.motion()));opening+=(1-opening)*ease;
  int ac=DeltaVisualSettings.primary()&0xffffff;float pulse=DeltaVisualSettings.pulse();
  // No panorama cubemap, moving scene or fullscreen blur on the startup screen.
  c.fillGradient(0,0,width,height,0xff0b0c14,0xff18131f);
  if(DeltaPerformance.quality()>0)for(int i=4;i>=1;i--){float size=40+i*35;DeltaRect.draw(c,width*.2f-size/2,height*.22f-size/2,size,size,size/2,0,(Math.round((3+i)*pulse)<<24)|ac);}
  float titleSize=height<300?26:42,titleY=Math.max(22,panelY-(height<300?24:46));
  String brand="INTERIUM";float tw=font.width(brand,titleSize);
  InteriumLogo.draw(c,(int)((width-tw-36)/2),(int)titleY-15,30,opening);
  font.draw(c,brand,(width-tw+36)/2,font.centeredY(brand,titleSize,titleY),titleSize,DeltaHud.fade(0xfff5f1ff,opening));
  DeltaGlass.draw(c,panelX,panelY,panelW,panelH,12,DeltaHud.fade(DeltaVisualSettings.background(),opening));
  for(Entry e:entries){ButtonWidget b=e.button;boolean hot=b.active&&(b.isFocused()||(mx>=b.getX()&&mx<b.getX()+b.getWidth()&&my>=b.getY()&&my<b.getY()+b.getHeight()));float v=hover.getOrDefault(b,0f);v+=((hot?1:0)-v)*ease;hover.put(b,v);
   DeltaRect.draw(c,b.getX(),b.getY(),b.getWidth(),b.getHeight(),6,0,DeltaHud.fade((Math.round(14+v*22)<<24)|ac,opening));
   if(v>.01f)DeltaRect.draw(c,b.getX()+1,b.getY()+1,b.getWidth()-2,b.getHeight()-2,5,.6f,DeltaHud.fade((Math.round(v*85*pulse)<<24)|ac,opening));
   String text=e.label;float size=height<300?8:9;while(text.length()>1&&font.width(text,size)>b.getWidth()-16)text=text.substring(0,text.offsetByCodePoints(text.length(),-1));
   font.draw(c,text,b.getX()+(b.getWidth()-font.width(text,size))/2,font.centeredY(text,size,b.getY()+b.getHeight()/2f),size,DeltaHud.fade(b.active?0xfff1edf9:0xff72717d,opening));
  }
  String footer="Fabric 1.21.11  /  "+DeltaPerformance.name()+"  /  Prism";
  font.draw(c,footer,12,height-15,7,0xff8c879f);
 }
 @Override public boolean shouldPause(){return false;}
 @Override public void close(){} // Escape must not turn the startup screen into an empty screen.
 static void smoke(){
  var c=net.minecraft.client.MinecraftClient.getInstance();if(!(c.currentScreen instanceof InteriumTitleScreen s))throw new AssertionError("Custom startup screen not installed");
  if(s.vanillaActions<5||s.entries.size()!=s.vanillaActions+2)throw new AssertionError("Vanilla navigation was lost");
  Entry first=s.entries.getFirst();if(first.button.active){activate(first.button);if(c.currentScreen==s)throw new AssertionError("Singleplayer navigation did not run");c.setScreen(s);}
  System.out.println("INTERIUM_CUSTOM_TITLE_AND_NAVIGATION_PASSED (screen navigation, not a world test)");
 }
}
