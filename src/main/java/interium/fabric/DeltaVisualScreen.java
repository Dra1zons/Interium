package interium.fabric;

import interium.fabric.delta.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.io.IOException;
import java.util.*;

/** Settings adapter using Delta's original atlases/row geometry; not its full element framework. */
final class DeltaVisualScreen extends Screen {
    private final Screen parent;
    private final int mode;
    private DeltaFont font,icons;
    private float scale,x,y,panelH,scroll;
    private final List<Option> options=new ArrayList<>();
    private record Option(String label,String key,boolean fallback,float value,float min,float max,float step,Runnable action){}
    DeltaVisualScreen(Screen parent,int mode){super(Text.literal(new String[]{"Interface","Crosshair","View Model","Removals","Aspect Ratio","Swing Animation","Pointers","Shulker Preview","See Invisibles","Item Physic","Entity Box","Entity ESP","Block ESP","Sound ESP","Warden ESP"}[mode]));this.parent=parent;this.mode=mode;}
    private void flag(String label,String key,boolean fallback){options.add(new Option(label,key,fallback,0,0,0,0,null));}
    private void slider(String label,String key,float fallback,float min,float max,float step){options.add(new Option(label,key,false,fallback,min,max,step,null));}
    private void button(String label,Runnable action){options.add(new Option(label,"",false,0,0,0,0,action));}
    @Override protected void init(){try{font=DeltaFont.load(client,"gt_regular");icons=DeltaFont.load(client,"icons");}catch(IOException e){throw new IllegalStateException(e);}options.clear();
        if(mode==0){button("Редактор HUD",()->client.setScreen(new DeltaHudEditor(this)));for(int i=0;i<DeltaHud.IDS.length;i++)flag(DeltaHud.NAMES[i],"hud."+DeltaHud.IDS[i],true);
            flag("Светлая тема","theme.light",false);slider("Прозрачность","theme.opacity",.78f,.2f,1,.05f);slider("Масштаб HUD","hud.scale",1,.5f,2,.1f);
            flag("Наводка: Target HUD","target.crosshair",false);flag("Предметы цели","target.items",true);
            for(String key:new String[]{"name","fps","ping","time","coords","speed"})flag("Инфо: "+switch(key){case "name"->"Ник";case "fps"->"FPS";case "ping"->"Пинг";case "time"->"Время";case "coords"->"Координаты";default->"Скорость";},"watermark."+key,true);
        }else if(mode==1){slider("Расстояние от центра","crosshair.gap",0,0,6,.5f);slider("Длина сегментов","crosshair.length",2.5f,2,5,.5f);flag("Адаптивность","crosshair.adaptive",false);flag("Контур","crosshair.outline",true);flag("Центральная метка","crosshair.dot",false);
        }else if(mode==2){for(String hand:new String[]{"main","off"})for(String axis:new String[]{"x","y","z"})slider((hand.equals("main")?"Основная":"Вторая")+" рука "+axis.toUpperCase(Locale.ROOT),"view."+hand+"."+axis,0,-2,2,.1f);button("Сбросить позиции",()->{for(String hand:new String[]{"main","off"})for(String axis:new String[]{"x","y","z"})DeltaVisualSettings.set("view."+hand+"."+axis,0);DeltaVisualSettings.save();});
        }else if(mode==3){flag("Тряска при уроне","remove.hurt",true);flag("Скорбоард","remove.scoreboard",false);flag("Боссбар","remove.bossbar",false);flag("Эффект портала","remove.portal",true);flag("Тошнота","remove.nausea",true);flag("Тыква","remove.pumpkin",true);flag("Огонь","remove.fire",true);flag("Обрезка камеры","remove.clip",true);flag("Частицы погоды","remove.weather",false);flag("Водная плёнка","remove.water",false);flag("Слепота","remove.blindness",true);flag("Тьма","remove.darkness",true);flag("Свечение","remove.glow",true);flag("Чёрные сердца","remove.hearts",true);
        }else if(mode==4){slider("Режим: 0=4:3 1=16:9 2=1:1 3=16:10 4=свой","aspect.mode",4,0,4,1);slider("Соотношение","aspect.value",1.9f,.1f,5,.1f);
        }else if(mode==5){flag("Только при цели Aura","swing.aura",true);slider("Мод анимации","swing.mode",1,1,5,1);slider("Угол поворота (Мод 1)","swing.angle",75,0,360,1);slider("Наклон кончика (Моды 1–4)","swing.tip",-20,-90,90,1);slider("Интенсивность","swing.power",5,1,10,1);
        }else if(mode==6){flag("Только друзья","pointers.friends",false);flag("Трассировка до игрока","pointers.tracers",true);slider("Размер стрелки","pointers.size",7,5,15,1);slider("Отступ от центра","pointers.radius",30,20,50,1);
        }else if(mode==7){button("Показывает реальные данные CONTAINER",()->{});
        }else if(mode==8){slider("Прозрачность","invis.alpha",.5f,.1f,1,.1f);
        }else if(mode==9){flag("Уменьшить размер предметов","physic.small",false);
        }else if(mode==10){slider("Вид: 0=квадрат 1=углы 2=заливка 3=выкл","box.mode",0,0,3,1);flag("Статичный цвет","box.static",false);flag("Бар здоровья","box.health",false);
        }else if(mode==11){flag("Игроки","esp.players",true);flag("Животные","esp.animals",false);flag("Мобы","esp.mobs",false);flag("Предметы","esp.items",false);
        }else if(mode==12){button("Блоки задаются в block.ids (через запятую)",()->{});
        }else if(mode==13){flag("Трезубец","sound.trident",true);flag("Фейерверк","sound.firework",true);flag("Взрывы","sound.explosion",true);
        }else if(mode==14){button("Исходная область города Delta; только загруженное",()->{});}
    }
    private void layout(){scale=Math.max(.1f,Math.min(2f/client.getWindow().getScaleFactor(),Math.min((width-8f)/260,(height-8f)/110)));x=(width/scale-260)/2;panelH=Math.min(options.size()*16+43,height/scale-20);y=(height/scale-panelH)/2;scroll=Math.max(0,Math.min(scroll,Math.max(0,options.size()*16-(panelH-43))));}
    @Override public void renderBackground(DrawContext c,int mx,int my,float delta){}
    private void label(DrawContext c,String text,float x,float cy,float size,int color){font.draw(c,text,x,font.centeredY(text,size,cy),size,color);}
    @Override public void render(DrawContext c,int mx,int my,float delta){layout();var m=c.getMatrices();m.pushMatrix();m.scale(scale,scale);DeltaRect.draw(c,x,y,260,panelH,8,0,DeltaVisualSettings.background());label(c,getTitle().getString(),x+10,y+12,9,DeltaVisualSettings.text());label(c,"ЛКМ + / ПКМ − · Esc назад",x+10,y+panelH-9,6,DeltaVisualSettings.text());c.enableScissor((int)(x+4),(int)(y+26),(int)(x+256),(int)(y+panelH-17));float row=y+27-scroll;for(Option o:options){boolean hover=mx/scale>=x+6&&mx/scale<x+254&&my/scale>=row&&my/scale<row+15;if(hover)DeltaRect.draw(c,x+6,row,248,15,3,0,0x207c8ee6);label(c,o.label,x+11,row+7.5f,7,DeltaVisualSettings.text());if(o.action==null){String value=o.step>0?String.format(Locale.ROOT,"%.2f",DeltaVisualSettings.number(o.key,o.value,o.min,o.max)):DeltaVisualSettings.flag(o.key,o.fallback)?"Да":"Нет";label(c,value,x+249-font.width(value,7),row+7.5f,7,DeltaVisualSettings.primary());}row+=16;}c.disableScissor();m.popMatrix();}
    @Override public boolean mouseClicked(Click click,boolean doubled){layout();double mx=click.x()/scale,my=click.y()/scale;if(mx<x+6||mx>=x+254||my<y+27||my>=y+panelH-17)return false;int i=(int)((my-y-27+scroll)/16);if(i<0||i>=options.size())return false;Option o=options.get(i);if(o.action!=null){if(click.button()==0)o.action.run();return true;}if(click.button()!=0&&click.button()!=1)return false;if(o.step>0){float n=DeltaVisualSettings.number(o.key,o.value,o.min,o.max)+(click.button()==1?-o.step:o.step);DeltaVisualSettings.set(o.key,Math.round(Math.max(o.min,Math.min(o.max,n))*1000)/1000f);}else DeltaVisualSettings.toggle(o.key,o.fallback);DeltaVisualSettings.save();return true;}
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){scroll-=(float)vertical*20;layout();return true;}
    @Override public void close(){DeltaVisualSettings.save();client.setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
}
