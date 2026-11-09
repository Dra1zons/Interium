package interium.fabric;

import interium.fabric.delta.DeltaFont;
import interium.fabric.delta.DeltaRect;
import interium.mixin.TextFieldWidgetAccess;
import com.viaversion.viafabricplus.ViaFabricPlus;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.util.*;

/** Experimental adaptation of Delta's panel geometry and input to 1.21.11.
 * Blur, animation, and most Delta settings are not yet ported. */
public final class DeltaScreen extends Screen {
    private static final String[] CATEGORY={"Combat","Movement","Render","Player","Misc"};
    private static final String[] ICON={"V","I","t","L","D"};
    private static final float PW=125, PH=270, GAP=8, ROW=16, ROW_GAP=4;
    private static final float TOTAL=PW*5+GAP*4;
    private static final int BG=0xc811131a, OUTLINE=0x0affffff, TEXT=0xffffffff, DISABLED=0xff434651, PRIMARY=0xff859cff;
    private DeltaFont onest,icons;
    private TextFieldWidget input;
    private final float[] scroll=new float[5];
    private int expanded=-1, searchStart=0, binding=-1;
    private float scale=1,left,top,searchX,searchY;
    private record Item(int i,int j,String name) {}
    public DeltaScreen(){super(Text.literal("Interium"));}
    @Override protected void init() {
        try {onest=DeltaFont.load(client,"onest_regular");icons=DeltaFont.load(client,"icons");}
        catch(IOException e){throw new IllegalStateException("Could not load Delta fonts",e);}
        String previous=input==null?"":input.getText();
        input=new TextFieldWidget(textRenderer,0,0,100,20,Text.literal("Поиск по модулям"));
        input.setMaxLength(80);input.setText(previous);
        input.setChangedListener(s -> Arrays.fill(scroll,0));
    }
    private void layout(){
        float gameScale=client.getWindow().getScaleFactor();
        scale=Math.min(2f/gameScale,Math.min((width-8f)/TOTAL,(height-8f)/(PH+64)));
        scale=Math.max(0.1f,scale);
        float w=width/scale,h=height/scale;
        left=(w-TOTAL)/2;top=(h-PH)/2;searchX=w/2-50;searchY=top+PH+12;
    }
    private float px(int i){return left+i*(PW+GAP);}
    private List<Item> items(int i){
        String query=input.getText().trim().toLowerCase(Locale.ROOT);
        List<Item> out=new ArrayList<>();
        for(int j=0;j<InteriumScreen.MODULES[i].length;j++) {
            String n=InteriumScreen.MODULES[i][j];
            if(n.toLowerCase(Locale.ROOT).contains(query))out.add(new Item(i,j,n));
        }
        out.sort(Comparator.comparing(Item::name,String.CASE_INSENSITIVE_ORDER));return out;
    }
    private static boolean hasSetting(Item a){return (a.i==0&&(a.j==0||a.j==3||a.j==5||a.j==6||a.j==7))||(a.i==3&&a.j==0)||(a.i==2&&(a.j==0||a.j==3))||(a.i==1&&(a.j==1||a.j==2||a.j==3||a.j==8));}
    private float rowHeight(Item a){return ROW+(hasSetting(a)&&expanded==a.i*10+a.j?((a.i==0&&a.j==3)?216:(a.i==1&&a.j==2)?36:(a.i==1&&a.j==3)?72:(a.i==0&&a.j==5)?72:(a.i==0&&a.j==6)?54:(a.i==2&&a.j==3&&!InteriumState.nightVision)?36:18):0);}
    private float clampedScroll(int i,List<Item> items){
        float total=0;for(Item a:items)total+=rowHeight(a)+ROW_GAP;
        scroll[i]=Math.max(Math.min(0, (PH-30)-total),Math.min(0,scroll[i]));return scroll[i];
    }
    private static boolean inside(double x,double y,float bx,float by,float w,float h){return x>=bx&&x<bx+w&&y>=by&&y<by+h;}
    private static void rect(DrawContext c,float x,float y,float w,float h,float r,int color){DeltaRect.draw(c,x,y,w,h,r,0,color);}
    private static void border(DrawContext c,float x,float y,float w,float h,float r,int color){DeltaRect.draw(c,x,y,w,h,r,.5f,color);}
    private void label(DrawContext c,DeltaFont f,String s,float x,float center,float size,int color){f.draw(c,s,x,f.centeredY(s,size,center),size,color);}
    private String trim(String s,float size,float width){
        if(onest.width(s,size)<=width)return s;
        int end=s.length();while(end>0&&onest.width(s.substring(0,end)+"…",size)>width)end--;
        return s.substring(0,end)+"…";
    }
    private static String onoff(boolean b){return b?"Да":"Нет";}
    private String bindName(int key){
        if(key==-1)return "N/A";
        if(key>=-100&&key<=-93)return "Mouse "+(key+101);
        String name=GLFW.glfwGetKeyName(key,0);
        if(name!=null)return name.toUpperCase(Locale.ROOT);
        if(key>=GLFW.GLFW_KEY_F1&&key<=GLFW.GLFW_KEY_F25)return "F"+(key-GLFW.GLFW_KEY_F1+1);
        return switch(key){case GLFW.GLFW_KEY_SPACE -> "Space";case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_ENTER -> "Enter";case GLFW.GLFW_KEY_BACKSPACE -> "Back";default -> "Key "+key;};
    }
    private void setBinding(int id,int key){InteriumState.binds[id/10][id%10]=key;binding=-1;InteriumState.save();}
    @Override public void renderBackground(DrawContext c,int x,int y,float delta) {} // Delta deliberately leaves the game visible.
    @Override public void render(DrawContext c,int rawX,int rawY,float delta){
        layout();float mx=rawX/scale,my=rawY/scale;
        var matrix=c.getMatrices();matrix.pushMatrix();matrix.scale(scale,scale);
        for(int i=0;i<5;i++) {
            float x=px(i); List<Item> list=items(i);
            rect(c,x,top,PW,PH,8,BG);border(c,x,top,PW,PH,8,OUTLINE);
            int title=0xffe1e6ff;
            label(c,onest,CATEGORY[i],x+10,top+12,9,title);
            label(c,icons,ICON[i],x+PW-10-icons.width(ICON[i],9),top+12,9,title);
            float y=top+28+clampedScroll(i,list),bottom=top+PH-2;
            c.enableScissor((int)x,(int)(top+28),(int)(x+PW),(int)bottom);
            for(Item item:list){
                float h=rowHeight(item);boolean on=InteriumScreen.enabled(item.i,item.j);
                boolean hover=inside(mx,my,x+6,y,PW-12,ROW)&&my>=top+28&&my<bottom;
                if(on)rect(c,x+6,y,PW-12,h,4,0x0a859cff);
                if(hover)rect(c,x+6,y,PW-12,h,4,0x06ffffff);
                if(on)border(c,x+6,y,PW-12,h,4,0x05ffffff);
                int id=item.i*10+item.j,key=InteriumState.binds[item.i][item.j];
                String name=trim(item.name,7.25f,(key!=-1||binding==id)?53:(on?76:92));
                onest.draw(c,name,x+10,y+8-7.25f/2-.5f,7.25f,TEXT);
                if(key!=-1||binding==id){
                    String b=binding==id?"?":bindName(key);float iw=icons.width("C",6);
                    float bw=4+iw+2.5f+onest.width(b,6)+4;
                    float bx=x+14+onest.width(name,7.25f),by=y+3.5f;
                    rect(c,bx,by,bw,9,2,0x28859cff);border(c,bx,by,bw,9,2,0x0affffff);
                    label(c,icons,"C",bx+4,y+8,6,TEXT);label(c,onest,b,bx+4+iw+2.5f,y+8,6,TEXT);
                }
                if(hasSetting(item))label(c,onest,"...",x+PW-10-(on?18:0)-onest.width("...",10),y+8,10,DISABLED);
                if(on){float tx=x+PW-24,ty=y+3.75f;rect(c,tx,ty,14,8.5f,3.25f,0x7d859cff);border(c,tx,ty,14,8.5f,3.25f,0x05ffffff);rect(c,tx+7,ty+1.5f,5.5f,5.5f,1.75f,TEXT);}
                if(expanded==item.i*10+item.j&&hasSetting(item)){
                    if(item.i==0&&item.j==3){
                        String[] values={"Дистанция: "+String.format(Locale.ROOT,"%.1f",InteriumState.auraRange),"Доп. дистанция: "+String.format(Locale.ROOT,"%.1f",InteriumState.auraExtra),"Приоритет: "+new String[]{"Прицел","Дистанция","ХП"}[InteriumState.auraPriority],"Игроки: "+onoff(InteriumState.auraPlayers),"Враждебные мобы: "+onoff(InteriumState.auraHostiles),"Животные: "+onoff(InteriumState.auraAnimals),"Только криты: "+onoff(InteriumState.auraOnlyCrits),"Адаптивные: "+onoff(InteriumState.auraAdaptive),"Не бить при использовании: "+onoff(InteriumState.auraStopUsing),"Топор против щита: "+onoff(InteriumState.auraShieldBreak),"Скорость поворота: "+(int)InteriumState.auraTurnSpeed,"Удерживать цель: "+onoff(InteriumState.auraLock)};
                        for(int n=0;n<values.length;n++)label(c,onest,trim(values[n],6.5f,PW-20),x+10,y+24+n*18,6.5f,0xffdddded);
                    }else if(item.i==1&&item.j==3){
                        String[] values={"Радиус: "+String.format(Locale.ROOT,"%.1f",InteriumState.targetStrafeRadius),"Скорость: "+String.format(Locale.ROOT,"%.2f",InteriumState.targetStrafeSpeed),"Направление: "+(InteriumState.targetStrafeClockwise?"По часовой":"Против часовой"),"Только при движении: "+onoff(InteriumState.targetStrafeInput)};
                        for(int n=0;n<values.length;n++)label(c,onest,trim(values[n],6.5f,PW-20),x+10,y+24+n*18,6.5f,0xffdddded);
                    }else if(item.i==2&&item.j==0){
                        label(c,onest,"Отсчёт: "+(InteriumState.tntTimerTicks?"Тики":"Секунды"),x+10,y+24,6.5f,0xffdddded);
                    }else if(item.i==1&&item.j==8){
                        label(c,onest,"Скорость: "+String.format(Locale.ROOT,"%.1f",InteriumState.freeCameraSpeed),x+10,y+24,6.5f,0xffdddded);
                    }else if(item.i==1&&item.j==2){
                        String[] values={"Режим: "+(InteriumState.speedMode==0?"Strafe":"BHop"),"Скорость: "+String.format(Locale.ROOT,"%.2f",InteriumState.speedValue)};
                        for(int n=0;n<values.length;n++)label(c,onest,values[n],x+10,y+24+n*18,6.5f,0xffdddded);
                    }else if(item.i==0&&item.j==5){
                        String[] values={"Здоровье: "+String.format(Locale.ROOT,"%.1f",InteriumState.totemHealth),"Вернуть предмет: "+onoff(InteriumState.totemRestore),"Сначала обычные: "+onoff(InteriumState.totemPreferPlain),"Пауза во время еды: "+onoff(InteriumState.totemPauseEating)};
                        for(int n=0;n<values.length;n++)label(c,onest,trim(values[n],6.5f,PW-20),x+10,y+24+n*18,6.5f,0xffdddded);
                    }else if(item.i==0&&item.j==7){
                        label(c,onest,"Животные (тест): "+onoff(InteriumState.crystalPlaceAnimals),x+10,y+24,6.5f,0xffdddded);
                    }else if(item.i==0&&item.j==6){
                        String[] values={"Дальность: "+String.format(Locale.ROOT,"%.1f",InteriumState.crystalRange),"Мин. дистанция: "+String.format(Locale.ROOT,"%.1f",InteriumState.crystalMinDistance),"Мин. здоровье: "+String.format(Locale.ROOT,"%.1f",InteriumState.crystalMinHealth)};
                        for(int n=0;n<values.length;n++)label(c,onest,trim(values[n],6.5f,PW-20),x+10,y+24+n*18,6.5f,0xffdddded);
                    }else{
                        String val=item.i==3?"Mode: "+(InteriumState.middleFriend?"Friend":"Pearl"):
                            item.i==2?"Режим: "+(InteriumState.nightVision?"Ночное зрение":"Гамма"):
                            item.i==1?"Режим: Vanilla":"CPS: "+InteriumState.cps;
                        label(c,onest,val,x+10,y+24,6.5f,0xffdddded);
                        if(item.i==2&&!InteriumState.nightVision)label(c,onest,"Уровень гаммы: "+InteriumState.gammaLevel,x+10,y+42,6.5f,0xffdddded);
                    }
                }
                y+=h+ROW_GAP;
            }
            c.disableScissor();
        }
        rect(c,searchX,searchY,100,20,6,0xc80e0f14);border(c,searchX,searchY,100,20,6,OUTLINE);
        String query=input.getText();boolean hint=query.isEmpty()&&!input.isFocused();
        int cursor=Math.max(0,Math.min(input.getCursor(),query.length()));
        searchStart=Math.min(searchStart,cursor);
        while(searchStart<cursor&&onest.width(query.substring(searchStart,cursor),7)>87)
            searchStart=query.offsetByCodePoints(searchStart,1);
        while(searchStart>0){
            int previous=query.offsetByCodePoints(searchStart,-1);
            if(onest.width(query.substring(previous,cursor),7)>87)break;
            searchStart=previous;
        }
        String visible=hint?"Поиск по модулям":query.substring(searchStart);
        c.enableScissor((int)(searchX+5),(int)searchY,(int)(searchX+95),(int)(searchY+20));
        if(input.isFocused()) {
            int anchor=((TextFieldWidgetAccess)(Object)input).interium$getSelectionEnd();
            int a=Math.max(searchStart,Math.min(cursor,anchor)),b=Math.max(searchStart,Math.max(cursor,anchor));
            if(a!=b){float sx=searchX+6+onest.width(query.substring(searchStart,a),7);
                float ex=searchX+6+onest.width(query.substring(searchStart,b),7);
                c.fill((int)sx,(int)(searchY+6),(int)Math.ceil(ex),(int)(searchY+14),0x78859cff);}
        }
        label(c,onest,visible,searchX+6,searchY+10,7,hint?DISABLED:TEXT);
        if(input.isFocused()&&System.currentTimeMillis()%1000<500){
            float cx=searchX+6+onest.width(query.substring(searchStart,cursor),7);
            rect(c,cx,searchY+6.5f,.5f,7,0,TEXT);
        }
        c.disableScissor();matrix.popMatrix();
    }
    @Override public boolean mouseClicked(Click click,boolean doubled){
        layout();double mx=click.x()/scale,my=click.y()/scale;int button=click.button();
        if(binding!=-1){if(button>=0&&button<=7){setBinding(binding,-100+button);return true;}return false;}
        boolean hitSearch=inside(mx,my,searchX,searchY,100,20);
        input.setFocused(hitSearch);if(hitSearch){input.setCursor(searchIndex(mx),false);return true;}
        for(int i=0;i<5;i++){
            float x=px(i);if(!inside(mx,my,x,top+28,PW,PH-30))continue;
            List<Item> list=items(i);float y=top+28+clampedScroll(i,list);
            for(Item item:list){float h=rowHeight(item);
                if(inside(mx,my,x+6,y,PW-12,ROW)){
                    if(button==0){InteriumScreen.toggle(item.i,item.j);return true;}
                    if(button==2){binding=item.i*10+item.j;return true;}
                    if(button==1&&hasSetting(item)){expanded=expanded==item.i*10+item.j?-1:item.i*10+item.j;return true;}
                }
                if(hasSetting(item)&&expanded==item.i*10+item.j&&inside(mx,my,x+6,y+ROW,PW-12,h-ROW)){
                    if(button==0||button==1){
                        if(item.i==0&&item.j==3){
                            int n=(int)((my-y-ROW)/18);int step=button==1?-1:1;
                            switch(n){
                                case 0 -> InteriumState.auraRange=Math.round(Math.max(.1f,Math.min(6f,InteriumState.auraRange+step*.1f))*10)/10f;
                                case 1 -> InteriumState.auraExtra=Math.round(Math.max(.1f,Math.min(3f,InteriumState.auraExtra+step*.1f))*10)/10f;
                                case 2 -> InteriumState.auraPriority=Math.floorMod(InteriumState.auraPriority+step,3);
                                case 3 -> InteriumState.auraPlayers=!InteriumState.auraPlayers;
                                case 4 -> InteriumState.auraHostiles=!InteriumState.auraHostiles;
                                case 5 -> InteriumState.auraAnimals=!InteriumState.auraAnimals;
                                case 6 -> InteriumState.auraOnlyCrits=!InteriumState.auraOnlyCrits;
                                case 7 -> InteriumState.auraAdaptive=!InteriumState.auraAdaptive;
                                case 8 -> InteriumState.auraStopUsing=!InteriumState.auraStopUsing;
                                case 9 -> InteriumState.auraShieldBreak=!InteriumState.auraShieldBreak;
                                case 10 -> InteriumState.auraTurnSpeed=Math.max(10,Math.min(180,InteriumState.auraTurnSpeed+step*10));
                                case 11 -> InteriumState.auraLock=!InteriumState.auraLock;
                            }
                        }else if(item.i==1&&item.j==3){
                            int n=(int)((my-y-ROW)/18);int step=button==1?-1:1;
                            switch(n){
                                case 0 -> InteriumState.targetStrafeRadius=Math.round(Math.max(1f,Math.min(5f,InteriumState.targetStrafeRadius+step*.1f))*10)/10f;
                                case 1 -> InteriumState.targetStrafeSpeed=Math.round(Math.max(.1f,Math.min(.6f,InteriumState.targetStrafeSpeed+step*.05f))*100)/100f;
                                case 2 -> InteriumState.targetStrafeClockwise=!InteriumState.targetStrafeClockwise;
                                case 3 -> InteriumState.targetStrafeInput=!InteriumState.targetStrafeInput;
                            }
                        }else if(item.i==2&&item.j==0){
                            InteriumState.tntTimerTicks=!InteriumState.tntTimerTicks;
                        }else if(item.i==1&&item.j==8){
                            int step=button==1?-1:1;
                            InteriumState.freeCameraSpeed=Math.round(Math.max(.1f,Math.min(2f,InteriumState.freeCameraSpeed+step*.1f))*10)/10f;
                        }else if(item.i==1&&item.j==2){
                            int n=(int)((my-y-ROW)/18); int step=button==1?-1:1;
                            if(n==0)InteriumState.speedMode=1-InteriumState.speedMode;
                            else if(n==1)InteriumState.speedValue=Math.round(Math.max(.2f,Math.min(.8f,InteriumState.speedValue+step*.02f))*100)/100f;
                        }else if(item.i==0&&item.j==5){
                            int n=(int)((my-y-ROW)/18);int step=button==1?-1:1;
                            switch(n){
                                case 0 -> InteriumState.totemHealth=Math.round(Math.max(1f,Math.min(20f,InteriumState.totemHealth+step*.5f))*2)/2f;
                                case 1 -> InteriumState.totemRestore=!InteriumState.totemRestore;
                                case 2 -> InteriumState.totemPreferPlain=!InteriumState.totemPreferPlain;
                                case 3 -> InteriumState.totemPauseEating=!InteriumState.totemPauseEating;
                            }
                        }else if(item.i==0&&item.j==6){
                            int n=(int)((my-y-ROW)/18);int step=button==1?-1:1;
                            switch(n){
                                case 0 -> InteriumState.crystalRange=Math.round(Math.max(3f,Math.min(6f,InteriumState.crystalRange+step*.5f))*2)/2f;
                                case 1 -> InteriumState.crystalMinDistance=Math.round(Math.max(0f,Math.min(6f,InteriumState.crystalMinDistance+step*.5f))*2)/2f;
                                case 2 -> InteriumState.crystalMinHealth=Math.round(Math.max(0f,Math.min(20f,InteriumState.crystalMinHealth+step))*1f);
                            }
                        }else if(item.i==0&&item.j==7)InteriumState.crystalPlaceAnimals=!InteriumState.crystalPlaceAnimals;
                        else if(item.i==3)InteriumState.middleFriend=!InteriumState.middleFriend;
                        else if(item.i==2){
                            if(my<y+ROW+18)InteriumState.nightVision=!InteriumState.nightVision;
                            else InteriumState.gammaLevel=Math.max(1f,Math.min(8f,InteriumState.gammaLevel+(button==1?-.5f:.5f)));
                        }else if(item.i==0)InteriumState.cps=Math.max(1,Math.min(20,InteriumState.cps+(button==1?-1:1)));
                        InteriumState.save();return true;}
                }
                y+=h+ROW_GAP;
            }
        }
        return false;
    }
    private int searchIndex(double mx){
        String value=input.getText();float target=(float)mx-searchX-6;int pos=searchStart;
        while(pos<value.length()){
            int next=value.offsetByCodePoints(pos,1);
            float midpoint=(onest.width(value.substring(searchStart,pos),7)+onest.width(value.substring(searchStart,next),7))*.5f;
            if(target<midpoint)break;pos=next;
        }
        return pos;
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){
        if(input.isFocused()&&click.button()==0){input.setCursor(searchIndex(click.x()/scale),true);return true;}
        return super.mouseDragged(click,dx,dy);
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double h,double v){
        layout();double x=mouseX/scale,y=mouseY/scale;
        for(int i=0;i<5;i++)if(inside(x,y,px(i),top,PW,PH)){scroll[i]+=(float)v*15;clampedScroll(i,items(i));return true;}
        return false;
    }
    @Override public boolean keyPressed(KeyInput key){
        if(binding!=-1){int code=key.key();setBinding(binding,code==GLFW.GLFW_KEY_DELETE?-1:code);return true;}
        if(key.key()==GLFW.GLFW_KEY_F&& (key.modifiers()&GLFW.GLFW_MOD_CONTROL)!=0){input.setFocused(!input.isFocused());return true;}
        if(input.isFocused()&&key.key()!=GLFW.GLFW_KEY_ESCAPE&&input.keyPressed(key))return true;
        if(!input.isFocused()&&key.key()==GLFW.GLFW_KEY_F7){client.setScreen(new ThemeEditorScreen(this));return true;}
        if(!input.isFocused()&&key.key()==GLFW.GLFW_KEY_F6&&ViaFabricPlus.getImpl()!=null){ViaFabricPlus.getImpl().openProtocolSelectionScreen(this);return true;}
        return super.keyPressed(key);
    }
    @Override public boolean charTyped(CharInput inputChar){return input.isFocused()&&input.charTyped(inputChar)||super.charTyped(inputChar);}
    @Override public boolean shouldPause(){return false;}
}
