package interium.fabric;

import interium.core.Theme;
import interium.fabric.delta.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.*;
import java.io.IOException;

/** One live, persisted theme for Delta GUI and HUD. Cancel restores the previous settings. */
public final class ThemeEditorScreen extends Screen {
 private final Screen parent;
 private TextFieldWidget accentField,panelField;
 private DeltaFont font,icons;
 private int left,top,w,h,drag=-1;
 private float scroll;
 private String error="";
 private boolean committed;
 private final Map<String,String> original=new LinkedHashMap<>();
 private record Control(String label,String key,float fallback,float min,float max){}
 private static final Control[] CONTROLS={
  new Control("Непрозрачность","theme.opacity",.86f,.2f,1),
  new Control("Размытие","glass.blur",5,0,16),
  new Control("Преломление","glass.refraction",5,0,24),
  new Control("Скругление","glass.radius",8,4,14),
  new Control("Световая кромка","theme.rim",.65f,0,1),
  new Control("Свечение","theme.glow",.32f,0,1),
  new Control("Пульсация","theme.pulse",.35f,0,1),
  new Control("Скорость света","theme.speed",.65f,.2f,2),
  new Control("Длительность анимаций · 0 = выкл","theme.motion",1,0,2)
 };
 private static final String[][] PRESETS={{"Amethyst","9878F8","15151B"},{"Ocean","76CBE7","101B24"},{"Rose","ED9CBC","211720"}};
 public ThemeEditorScreen(Screen parent){super(Text.literal("Theme studio"));this.parent=parent;
  for(Control c:CONTROLS)original.put(c.key,DeltaVisualSettings.string(c.key,Float.toString(c.fallback)));
  original.put("theme.rgb",DeltaVisualSettings.string("theme.rgb","9992440"));
  original.put("theme.panel.rgb",DeltaVisualSettings.string("theme.panel.rgb","1381659"));
 }
 @Override protected void init(){
  try{font=DeltaFont.load(client,"onest_regular");icons=DeltaFont.load(client,"icons");}catch(IOException ex){throw new IllegalStateException(ex);}
  w=Math.max(180,Math.min(360,width-16));h=Math.max(180,Math.min(350,height-16));left=(width-w)/2;top=(height-h)/2;
  accentField=addDrawableChild(new TextFieldWidget(textRenderer,left+14,top+69,w/2-22,14,Text.literal("Accent RRGGBB")));
  panelField=addDrawableChild(new TextFieldWidget(textRenderer,left+w/2+6,top+69,w/2-22,14,Text.literal("Panel RRGGBB")));
  accentField.setDrawsBackground(false);panelField.setDrawsBackground(false);accentField.setMaxLength(6);panelField.setMaxLength(6);
  accentField.setText(String.format(Locale.ROOT,"%06X",DeltaVisualSettings.primary()&0xffffff));
  panelField.setText(String.format(Locale.ROOT,"%06X",DeltaVisualSettings.panelRgb()));
  accentField.setChangedListener(v->previewColors());panelField.setChangedListener(v->previewColors());
  clampScroll();
 }
 private void previewColors(){if(accentField.getText().matches("[0-9a-fA-F]{6}")&&panelField.getText().matches("[0-9a-fA-F]{6}")){
  DeltaVisualSettings.set("theme.rgb",Integer.parseInt(accentField.getText(),16));DeltaVisualSettings.set("theme.panel.rgb",Integer.parseInt(panelField.getText(),16));error="";
 }}
 private boolean hit(double x,double y,float bx,float by,float bw,float bh){return x>=bx&&x<bx+bw&&y>=by&&y<by+bh;}
 private void text(DrawContext c,String value,float x,float y,float size,int color){font.draw(c,value,x,font.centeredY(value,size,y),size,color);}
 private void button(DrawContext c,int x,int y,int bw,String title,int mx,int my){boolean hover=hit(mx,my,x,y,bw,18);DeltaRect.draw(c,x,y,bw,18,5,0,hover?0x299878f8:0x12ffffff);text(c,title,x+7,y+9,8,DeltaVisualSettings.text());}
 private int bodyBottom(){return top+h-88;}
 private void clampScroll(){scroll=Math.max(0,Math.min(scroll,Math.max(0,CONTROLS.length*22-(bodyBottom()-(top+94)))));}
 @Override public void renderBackground(DrawContext c,int mx,int my,float delta){}
 @Override public void render(DrawContext c,int mx,int my,float delta){
  DeltaGlass.draw(c,left,top,w,h,11,DeltaVisualSettings.background());
  text(c,"Theme studio",left+14,top+15,11,DeltaVisualSettings.text());text(c,"GUI + HUD · live preview",left+14,top+29,7,DeltaVisualSettings.primary());
  int bw=(w-28)/3;for(int i=0;i<PRESETS.length;i++)button(c,left+12+i*(bw+2),top+38,bw-2,PRESETS[i][0],mx,my);
  DeltaRect.draw(c,left+10,top+65,w/2-15,22,5,0,0x17ffffff);DeltaRect.draw(c,left+w/2+2,top+65,w/2-12,22,5,0,0x17ffffff);
  c.enableScissor(left+10,top+93,left+w-10,bodyBottom());
  float y=top+96-scroll;for(Control control:CONTROLS){
   float n=DeltaVisualSettings.number(control.key,control.fallback,control.min,control.max),fraction=(n-control.min)/(control.max-control.min);
   text(c,control.label,left+14,y+5,7,DeltaVisualSettings.text());String value=String.format(Locale.ROOT,"%.2f",n);text(c,value,left+w-14-font.width(value,7),y+5,7,DeltaVisualSettings.primary());
   DeltaRect.draw(c,left+14,y+14,w-28,2,1,0,0x24ffffff);if(fraction>0)DeltaRect.draw(c,left+14,y+14,(w-28)*fraction,2,1,0,DeltaVisualSettings.primary());
   DeltaRect.draw(c,left+12+(w-28)*fraction,y+12,6,6,3,0,DeltaVisualSettings.text());y+=22;
  }c.disableScissor();
  float py=top+h-78;DeltaGlass.draw(c,left+12,py,w-24,42,8,DeltaVisualSettings.background());
  text(c,"E",left+20,py+10,8,DeltaVisualSettings.primary());text(c,"Potions",left+36,py+10,8,DeltaVisualSettings.text());
  DeltaRect.draw(c,left+20,py+20,w-40,.5f,0,0,0x28ffffff);text(c,"Speed II",left+22,py+31,8,DeltaVisualSettings.text());text(c,"55s",left+w-40,py+31,8,DeltaVisualSettings.primary());
  if(!error.isEmpty())text(c,error,left+12,top+h-28,6,0xffff8888);
  button(c,left+w-144,top+h-23,62,"Cancel",mx,my);button(c,left+w-76,top+h-23,64,"Save",mx,my);
  super.render(c,mx,my,delta);
 }
 private void updateSlider(double mouseX){if(drag<0)return;Control c=CONTROLS[drag];float f=(float)Math.max(0,Math.min(1,(mouseX-left-14)/(w-28)));DeltaVisualSettings.set(c.key,Math.round((c.min+f*(c.max-c.min))*100)/100f);}
 @Override public boolean mouseClicked(Click click,boolean doubled){
  if(click.button()!=0)return super.mouseClicked(click,doubled);double x=click.x(),y=click.y();int bw=(w-28)/3;
  for(int i=0;i<PRESETS.length;i++)if(hit(x,y,left+12+i*(bw+2),top+38,bw-2,18)){accentField.setText(PRESETS[i][1]);panelField.setText(PRESETS[i][2]);previewColors();return true;}
  if(hit(x,y,left+12,top+94,w-24,bodyBottom()-(top+94))){int i=(int)((y-top-96+scroll)/22);if(i>=0&&i<CONTROLS.length){drag=i;accentField.setFocused(false);panelField.setFocused(false);updateSlider(x);return true;}}
  if(hit(x,y,left+w-144,top+h-23,62,18)){close();return true;}
  if(hit(x,y,left+w-76,top+h-23,64,18)){
   if(!accentField.getText().matches("[0-9a-fA-F]{6}")||!panelField.getText().matches("[0-9a-fA-F]{6}")){error="RRGGBB: 6 hex digits";return true;}
   previewColors();InteriumState.theme=new Theme("Custom",DeltaVisualSettings.primary()&0xffffff,DeltaVisualSettings.panelRgb(),Math.round(DeltaVisualSettings.number("theme.opacity",.86f,.2f,1)*100),InteriumState.theme.getBlur());
   committed=true;DeltaVisualSettings.save();InteriumState.save();close();return true;
  }
  return super.mouseClicked(click,doubled);
 }
 @Override public boolean mouseDragged(Click click,double dx,double dy){if(drag>=0&&click.button()==0){updateSlider(click.x());return true;}return super.mouseDragged(click,dx,dy);}
 @Override public boolean mouseReleased(Click click){if(drag>=0){drag=-1;return true;}return super.mouseReleased(click);}
 @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){scroll-=(float)vertical*22;clampScroll();return true;}
 @Override public void close(){if(!committed)for(var e:original.entrySet())DeltaVisualSettings.set(e.getKey(),e.getValue());client.setScreen(parent);}
 static void smoke(){
  var client=net.minecraft.client.MinecraftClient.getInstance();
  if(!(client.currentScreen instanceof ThemeEditorScreen editor))throw new AssertionError("Theme editor did not open");
  int accent=DeltaVisualSettings.primary(),panel=DeltaVisualSettings.panelRgb();
  editor.accentField.setText("AA66FF");editor.panelField.setText("17131F");
  if((DeltaVisualSettings.primary()&0xffffff)!=0xaa66ff||DeltaVisualSettings.panelRgb()!=0x17131f)throw new AssertionError("Live RGB preview failed");
  editor.close();if(DeltaVisualSettings.primary()!=accent||DeltaVisualSettings.panelRgb()!=panel)throw new AssertionError("Theme cancel failed");
  for(String[] category:InteriumScreen.MODULES)for(String module:category)if(module.equals("Warden ESP"))throw new AssertionError("Removed module is still registered");
  if(InteriumState.bindIndex(2,18,false)!=19||InteriumState.bindIndex(2,18,true)!=18||InteriumState.bindIndex(0,18,false)!=18)throw new AssertionError("Bind migration failed");
  client.setScreen(new ThemeEditorScreen(editor.parent));
  System.out.println("INTERIUM_THEME_PREVIEW_CANCEL_AND_BIND_MIGRATION_PASSED");
 }
 @Override public boolean shouldPause(){return false;}
}
