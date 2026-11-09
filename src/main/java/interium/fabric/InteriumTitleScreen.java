package interium.fabric;
import interium.fabric.delta.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.io.IOException;
import java.util.*;
/** One original minimal I identity, with native navigation/focus and no decorative blur. */
public final class InteriumTitleScreen extends TitleScreen {
 private DeltaFont font;private final List<Entry> entries=new ArrayList<>();private final Map<ButtonWidget,Float> hover=new IdentityHashMap<>();
 private record Entry(ButtonWidget button,String label,boolean primary){}
 private float opening;private long previous;private int vanillaActions,logoSize,logoY,brandY,subtitleY;
 public InteriumTitleScreen(){super(false);}
 @Override protected void init(){super.init();InteriumLogo.window(client);List<ButtonWidget> original=new ArrayList<>();for(var child:children())if(child instanceof ButtonWidget button)original.add(button);vanillaActions=original.size();clearChildren();entries.clear();hover.clear();
  try{font=DeltaFont.load(client,"onest_regular");}catch(IOException e){throw new IllegalStateException("Interium title font",e);}
  boolean compact=height<300;int full=Math.min(3,original.size()),extraRows=(original.size()-full+1)/2,row=compact?20:25,minor=compact?16:20,gap=5,header=compact?72:112;
  int total=header+full*(row+gap)+extraRows*(minor+gap)+minor,top=Math.max(8,(height-total)/2),w=Math.min(compact?206:232,width-32),x=(width-w)/2,y=top+header;
  logoSize=compact?34:48;logoY=top;brandY=top+logoSize+(compact?12:19);subtitleY=brandY+(compact?14:17);
  for(int i=0;i<full;i++){ButtonWidget old=original.get(i);add(old.getMessage(),old.active,b->activate(old),x,y,w,row,i==0);y+=row+gap;}
  for(int i=full;i<original.size();i++){ButtonWidget old=original.get(i);int col=(i-full)%2;add(old.getMessage(),old.active,b->activate(old),x+col*((w+gap)/2),y,(w-gap)/2,minor,false);if(col==1||i==original.size()-1)y+=minor+gap;}
  add(Text.literal("Themes"),true,b->client.setScreen(new ThemeEditorScreen(this)),x,y,(w-gap)/2,minor,false);add(Text.literal("Performance"),true,b->client.setScreen(new PerformanceScreen(this)),x+(w+gap)/2,y,(w-gap)/2,minor,false);
 }
 private static void activate(ButtonWidget b){((interium.mixin.DeltaButtonActionAccess)(Object)b).interium$action().onPress(b);}
 private void add(Text text,boolean active,ButtonWidget.PressAction action,int x,int y,int w,int h,boolean primary){var b=addDrawableChild(ButtonWidget.builder(text,action).dimensions(x,y,w,h).build());b.active=active;entries.add(new Entry(b,text.getString(),primary));}
 @Override public void renderBackground(DrawContext c,int mx,int my,float delta){}
 @Override public void render(DrawContext c,int mx,int my,float delta){long now=System.nanoTime();float dt=previous==0?.016f:Math.min(.05f,(now-previous)/1e9f);previous=now;float ease=DeltaVisualSettings.motion()==0?1:1-(float)Math.exp(-dt*12/Math.max(.2f,DeltaVisualSettings.motion()));opening+=(1-opening)*ease;
  c.fill(0,0,width,height,0xff0c0f14);InteriumLogo.draw(c,(width-logoSize)/2,logoY,logoSize,opening);
  String brand="INTERIUM";float size=height<300?17:21;draw(c,brand,(width-font.width(brand,size))/2,brandY,size,0xfff2f5fa);
  String sub="MINECRAFT CLIENT";draw(c,sub,(width-font.width(sub,5.5f))/2,subtitleY,5.5f,0xff697585);
  for(Entry e:entries){var b=e.button;boolean hot=b.active&&(b.isFocused()||mx>=b.getX()&&mx<b.getX()+b.getWidth()&&my>=b.getY()&&my<b.getY()+b.getHeight());float v=hover.getOrDefault(b,0f);v+=((hot?1:0)-v)*ease;hover.put(b,v);
   int fill=e.primary?0xff172333:0xff12171f;DeltaRect.draw(c,b.getX(),b.getY(),b.getWidth(),b.getHeight(),4,0,DeltaHud.fade(fill,opening));
   DeltaRect.draw(c,b.getX(),b.getY(),b.getWidth(),b.getHeight(),4,.5f,DeltaHud.fade(0xff7daeff,opening*(.05f+.25f*v)));
   if(v>.01f)DeltaRect.draw(c,b.getX()+1,b.getY()+1,b.getWidth()-2,b.getHeight()-2,3,0,DeltaHud.fade(0xff378cf5,opening*v*.08f));
   if(e.primary)DeltaRect.draw(c,b.getX()+7,b.getY()+b.getHeight()/2f-2,2,4,1,0,DeltaHud.fade(0xff4695ff,opening));
   String text=e.label;float ts=b.getHeight()<20?6.5f:8;while(text.length()>1&&font.width(text,ts)>b.getWidth()-22)text=text.substring(0,text.offsetByCodePoints(text.length(),-1));draw(c,text,b.getX()+(b.getWidth()-font.width(text,ts))/2,b.getY()+b.getHeight()/2f,ts,b.active?0xffe4eaf1:0xff59616d);
  }
  draw(c,"Interium 0.5.8",10,height-10,5.5f,0xff4b5664);String version="Fabric 1.21.11";draw(c,version,width-font.width(version,5.5f)-10,height-10,5.5f,0xff4b5664);
 }
 private void draw(DrawContext c,String s,float x,float cy,float size,int color){font.draw(c,s,x,font.centeredY(s,size,cy),size,DeltaHud.fade(color,opening));}
 @Override public boolean shouldPause(){return false;}@Override public void close(){}
 static void smoke(){var c=net.minecraft.client.MinecraftClient.getInstance();if(!(c.currentScreen instanceof InteriumTitleScreen s))throw new AssertionError("Custom title missing");if(s.vanillaActions<5||s.entries.size()!=s.vanillaActions+2)throw new AssertionError("Navigation missing");for(var e:s.entries){var b=e.button;if(b.getX()<0||b.getY()<0||b.getX()+b.getWidth()>s.width||b.getY()+b.getHeight()>s.height)throw new AssertionError("Clipped title action");}Entry first=s.entries.getFirst();if(first.button.active){activate(first.button);if(c.currentScreen==s)throw new AssertionError("Singleplayer action lost");c.setScreen(s);}System.out.println("INTERIUM_CUSTOM_TITLE_AND_NAVIGATION_PASSED (minimal identity, bounded actions; not a world test)");}
}
