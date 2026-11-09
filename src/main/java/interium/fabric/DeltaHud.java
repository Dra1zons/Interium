package interium.fabric;

import interium.fabric.delta.*;
import interium.mixin.DeltaCooldownAccess;
import interium.mixin.DeltaCooldownEntryAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 1.21.11 adaptation of Delta Widget/Watermark/Hotkeys/Potion/Cooldowns/Target/Armor.
 * Original GT and icons atlases, header 12.5, body 11.5, gap 2, separator .75.
 * GPU backdrop blur/glow and backend notifications are NOT implemented here.
 */
public final class DeltaHud {
    static final DeltaHud INSTANCE=new DeltaHud();
    static final String[] IDS={"watermark","keys","target","cooldowns","potions","armor","staff","environment","notifications"};
    static final String[] NAMES={"Инфо-панель","Клавиши","Таргет-худ","Задержки","Зелья","Броня","Стафф","Окружение","Уведомления"};
    private static final EquipmentSlot[] ARMOR={EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD};
    private static final float[][] DEFAULT={{.5f,.015f},{.02f,.27f},{.57f,.6f},{.02f,.5f},{.82f,.5f},{.5f,.9f},{.82f,.27f},{.82f,.68f},{.5f,.18f}};
    private final LinkedHashMap<String,Bounds> bounds=new LinkedHashMap<>();
    private final Map<String,Float> animations=new HashMap<>();
    private final Deque<Notice> notices=new ArrayDeque<>();
    private DeltaFont gt,icons;
    private long lastFrame;
    private float dt=.016f,sw,sh,alpha=1,health=1;
    private LivingEntity retainedTarget;
    private UUID healthTarget;
    void resetWorld(){retainedTarget=null;healthTarget=null;animations.clear();bounds.clear();notices.clear();lastFrame=0;}
    private record Notice(String text,long time){}
    record Bounds(float x,float y,float w,float h){boolean contains(double mx,double my){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}}
    private record Row(String key,String icon,String title,String right,ItemStack stack,Identifier skin,StatusEffectInstance effect,int color){}
    static boolean enabled(String id){return DeltaVisualSettings.flag("hud."+id,true);}
    float scale(MinecraftClient c){return Math.max(.1f,2f/c.getWindow().getScaleFactor()*DeltaVisualSettings.number("hud.scale",1,.5f,2));}
    Map<String,Bounds> bounds(){return Collections.unmodifiableMap(bounds);}
    void move(String id,float x,float y){Bounds b=bounds.get(id);if(b==null)return;DeltaVisualSettings.set("pos."+id+".x",Math.max(0,Math.min(1,x/Math.max(1,sw-b.w))));DeltaVisualSettings.set("pos."+id+".y",Math.max(0,Math.min(1,y/Math.max(1,sh-b.h))));}
    static void notifyModule(String name,boolean on){INSTANCE.notices.addLast(new Notice(name+(on?" включён":" выключен"),System.nanoTime()));while(INSTANCE.notices.size()>5)INSTANCE.notices.removeFirst();}
    private void fonts(MinecraftClient c){if(gt!=null)return;try{gt=DeltaFont.load(c,"gt_regular");icons=DeltaFont.load(c,"icons");}catch(IOException e){throw new IllegalStateException("Original Delta HUD fonts could not be loaded",e);}}
    static int fade(int color,float a){return Math.round(((color>>>24)&255)*Math.max(0,Math.min(1,a)))<<24|(color&0xffffff);}
    private float anim(String id,boolean visible){float v=animations.getOrDefault(id,0f);float t=visible?1:0;v+=(t-v)*(1-(float)Math.exp(-dt*14));if(Math.abs(v-t)<.005f)v=t;animations.put(id,v);return v;}
    void render(MinecraftClient c,DrawContext ctx,LivingEntity auraTarget,boolean edit){
        if(c.player==null||c.world==null||(!edit&&(c.options.hudHidden||!DeltaVisualSettings.hud)))return;
        fonts(c);long now=System.nanoTime();dt=lastFrame==0?.016f:Math.min(.1f,(now-lastFrame)/1_000_000_000f);lastFrame=now;
        float scale=scale(c);sw=c.getWindow().getScaledWidth()/scale;sh=c.getWindow().getScaledHeight()/scale;
        bounds.clear();var matrix=ctx.getMatrices();matrix.pushMatrix();matrix.scale(scale,scale);
        boolean preview=edit||c.currentScreen instanceof ChatScreen;
        if(enabled("watermark"))watermark(c,ctx);
        if(enabled("keys"))keys(c,ctx,preview);
        if(enabled("potions"))potions(c,ctx,preview);
        if(enabled("cooldowns"))cooldowns(c,ctx,preview);
        if(enabled("target"))target(c,ctx,auraTarget,preview);
        if(enabled("armor"))armor(c,ctx,preview);
        if(enabled("staff"))staff(c,ctx,preview);
        if(enabled("environment"))environment(c,ctx,preview);
        if(enabled("notifications"))notifications(ctx,preview,now);
        alpha=1;matrix.popMatrix();
        if(animations.size()>512)animations.keySet().removeIf(k->animations.get(k)==0f);
    }
    private Bounds place(String id,float w,float h){int i=Arrays.asList(IDS).indexOf(id);float[] d=DEFAULT[Math.max(0,i)];w=Math.min(sw,Math.max(1,(float)Math.ceil(w)));h=Math.min(sh,Math.max(1,h));float x=DeltaVisualSettings.number("pos."+id+".x",d[0],0,1)*Math.max(0,sw-w);float y=DeltaVisualSettings.number("pos."+id+".y",d[1],0,1)*Math.max(0,sh-h);Bounds b=new Bounds(x,y,w,h);bounds.put(id,b);return b;}
    private void text(DrawContext c,String s,float x,float cy,float size,int color){gt.draw(c,s,x,gt.centeredY(s,size,cy),size,fade(color,alpha));}
    private void icon(DrawContext c,String s,float x,float cy,float size,int color){icons.draw(c,s,x,icons.centeredY(s,size,cy),size,fade(color,alpha));}
    private void box(DrawContext c,float x,float y,float w,float h){DeltaRect.draw(c,x,y,(float)Math.ceil(w),h,Math.min(5,h/2),0,fade(DeltaVisualSettings.background(),alpha));}
    private void separator(DrawContext c,float x,float y,float h){DeltaRect.draw(c,x,y+h/4,.75f,h/2,0,0,fade(DeltaVisualSettings.light()?0x80111216:0x80c8c8c8,alpha));}
    private void header(DrawContext c,Bounds b,String glyph,String title){box(c,b.x,b.y,b.w,12.5f);icon(c,glyph,b.x+3,b.y+6.25f,8,DeltaVisualSettings.primary());separator(c,b.x+13.5f,b.y,12.5f);text(c,title,b.x+17.5f,b.y+5.75f,7,DeltaVisualSettings.text());}
    private void item(DrawContext c,ItemStack stack,float x,float y,float size,boolean overlay){if(stack.isEmpty())return;var m=c.getMatrices();m.pushMatrix();m.translate(x,y);m.scale(size/16,size/16);c.drawItem(stack,0,0);if(overlay)c.drawStackOverlay(MinecraftClient.getInstance().textRenderer,stack,0,0);m.popMatrix();}
    private void face(DrawContext c,Identifier skin,float x,float y,float size){var m=c.getMatrices();m.pushMatrix();m.translate(x,y);m.scale(size/8,size/8);c.drawTexture(RenderPipelines.GUI_TEXTURED,skin,0,0,8,8,8,8,64,64,fade(-1,alpha));c.drawTexture(RenderPipelines.GUI_TEXTURED,skin,0,0,40,8,8,8,64,64,fade(-1,alpha));m.popMatrix();}
    private void list(DrawContext c,String id,String glyph,String title,List<Row> rows,boolean preview){
        float a=anim("panel."+id,preview||!rows.isEmpty());if(a<=0)return;float w=21.5f+gt.width(title,7);
        for(Row r:rows)w=Math.max(w,34+gt.width(r.title,6.5f)+gt.width(r.right,6.5f));
        Bounds b=place(id,w,12.5f+(rows.isEmpty()?0:3+rows.size()*13.5f-2));alpha=a;header(c,b,glyph,title);float y=b.y+15.5f;
        for(Row r:rows){float ra=anim("row."+id+"."+r.key,true);alpha=a*ra;float x=b.x-8*(1-ra);box(c,x,y,b.w,11.5f);separator(c,x+15,y,11.5f);
            if(r.skin!=null)face(c,r.skin,x+5,y+2,7.5f);else if(r.effect!=null)c.drawGuiTexture(RenderPipelines.GUI_TEXTURED,net.minecraft.client.gui.hud.InGameHud.getEffectTexture(r.effect.getEffectType()),(int)(x+5),(int)(y+2),7,7,fade(-1,alpha));else if(!r.stack.isEmpty())item(c,r.stack,x+5,y+2,7.2f,false);else icon(c,r.icon,x+5,y+5.75f,6.5f,DeltaVisualSettings.primary());
            text(c,r.title,x+19,y+5.25f,6.5f,r.color);text(c,r.right,x+b.w-6-gt.width(r.right,6.5f),y+5.25f,6.5f,fade(DeltaVisualSettings.text(),.55f));y+=13.5f;
        }alpha=1;
    }
    private Row row(String key,String glyph,String title,String value){return new Row(key,glyph,title,value,ItemStack.EMPTY,null,null,DeltaVisualSettings.text());}
    private void watermark(MinecraftClient c,DrawContext ctx){
        var p=c.player;int latency=c.getNetworkHandler()==null||c.getNetworkHandler().getPlayerListEntry(p.getUuid())==null?0:c.getNetworkHandler().getPlayerListEntry(p.getUuid()).getLatency();
        List<String[]> top=new ArrayList<>(),bottom=new ArrayList<>();
        if(DeltaVisualSettings.flag("watermark.name",true))top.add(new String[]{"L",p.getName().getString()});
        if(DeltaVisualSettings.flag("watermark.fps",true))top.add(new String[]{"q",c.getCurrentFps()+" FPS"});
        if(DeltaVisualSettings.flag("watermark.ping",true))top.add(new String[]{"P",latency+" ms"});
        if(DeltaVisualSettings.flag("watermark.time",true))top.add(new String[]{"T",LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))});
        if(DeltaVisualSettings.flag("watermark.coords",true))bottom.add(new String[]{"b","x "+(int)p.getX()+" y "+(int)p.getY()+" z "+(int)p.getZ()});
        if(DeltaVisualSettings.flag("watermark.speed",true))bottom.add(new String[]{"e",String.format(Locale.ROOT,"%.2f BPS",Math.hypot(p.getX()-p.lastX,p.getZ()-p.lastZ)*20)});
        float tw=sectionWidth(top,true),bw=bottom.isEmpty()?0:sectionWidth(bottom,false);Bounds b=place("watermark",Math.max(tw,bw),bw==0?12.5f:28);alpha=1;
        sections(ctx,b.x+(b.w-tw)/2,b.y,tw,top,true);if(bw>0)sections(ctx,b.x+(b.w-bw)/2,b.y+15.5f,bw,bottom,false);
    }
    private float sectionWidth(List<String[]> parts,boolean logo){float w=5+(logo?icons.width("a",8)+10:0);for(String[] v:parts)w+=icons.width(v[0],6.5f)+3+gt.width(v[1],7)+11;return Math.max(12,w-6);}
    private void sections(DrawContext c,float x,float y,float w,List<String[]> parts,boolean logo){box(c,x,y,w,12.5f);float cursor=x+5;if(logo){icon(c,"a",cursor,y+6.25f,8,DeltaVisualSettings.primary());cursor+=icons.width("a",8)+4;separator(c,cursor,y,12.5f);cursor+=6;}
        for(int i=0;i<parts.size();i++){if(i>0){separator(c,cursor,y,12.5f);cursor+=6;}String[] v=parts.get(i);icon(c,v[0],cursor,y+6.25f,6.5f,DeltaVisualSettings.primary());cursor+=icons.width(v[0],6.5f)+3;text(c,v[1],cursor,y+5.75f,7,DeltaVisualSettings.text());cursor+=gt.width(v[1],7)+5;}}
    static String keyName(int key){if(key<=-93&&key>=-100)return "Mouse "+(key+101);String s=GLFW.glfwGetKeyName(key,0);if(s!=null)return s.toUpperCase(Locale.ROOT);if(key>=290&&key<=314)return "F"+(key-289);return switch(key){case 32->"Space";case 340->"LShift";case 344->"RShift";case 341->"LCtrl";case 345->"RCtrl";default->"Key "+key;};}
    private void keys(MinecraftClient c,DrawContext ctx,boolean preview){List<Row> rows=new ArrayList<>();for(int i=0;i<InteriumScreen.MODULES.length;i++)for(int j=0;j<InteriumScreen.MODULES[i].length;j++)if(InteriumState.binds[i][j]!=-1&&InteriumScreen.enabled(i,j))rows.add(row(i+"."+j,new String[]{"V","I","t","L","D"}[i],InteriumScreen.MODULES[i][j],keyName(InteriumState.binds[i][j])));list(ctx,"keys","Q","Hot-keys",rows,preview);}
    private void potions(MinecraftClient c,DrawContext ctx,boolean preview){List<Row> rows=new ArrayList<>();List<StatusEffectInstance> effects=new ArrayList<>(c.player.getStatusEffects());effects.sort(Comparator.comparing(e->e.getTranslationKey()));for(var e:effects){int sec=Math.max(0,e.getDuration()/20);String t=e.isInfinite()?"∞":String.format(Locale.ROOT,"%d:%02d",sec/60,sec%60);rows.add(new Row(e.getTranslationKey(),"E",Text.translatable(e.getTranslationKey()).getString()+" "+(e.getAmplifier()+1),t,ItemStack.EMPTY,null,e,e.getEffectType().value().isBeneficial()?DeltaVisualSettings.text():0xffd74c4c));}list(ctx,"potions","E","Potion-list",rows,preview);}
    private void cooldowns(MinecraftClient c,DrawContext ctx,boolean preview){List<Row> rows=new ArrayList<>();var manager=c.player.getItemCooldownManager();var access=(DeltaCooldownAccess)manager;Map<Identifier,ItemStack> inventory=new HashMap<>();for(int i=0;i<c.player.getInventory().size();i++){ItemStack s=c.player.getInventory().getStack(i);if(!s.isEmpty())inventory.putIfAbsent(manager.getGroup(s),s);}for(var entry:access.interium$entries().entrySet()){int ticks=((DeltaCooldownEntryAccess)entry.getValue()).interium$endTick()-access.interium$tick();ItemStack s=inventory.get(entry.getKey());if(ticks>5&&s!=null)rows.add(new Row(entry.getKey().toString(),"d",s.getName().getString(),String.format(Locale.ROOT,"%.1fс",ticks/20f),s,null,null,DeltaVisualSettings.text()));}rows.sort(Comparator.comparing(Row::title));list(ctx,"cooldowns","d","Cooldowns",rows,preview);}
    private void target(MinecraftClient c,DrawContext ctx,LivingEntity auraTarget,boolean preview){LivingEntity t=auraTarget;if(t==null&&DeltaVisualSettings.flag("target.crosshair",false)&&c.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit&&hit.getEntity() instanceof PlayerEntity living)t=living;if(t==null&&preview)t=c.player;float a=anim("panel.target",t!=null&&t.isAlive());if(t!=null)retainedTarget=t;if(a<=0||retainedTarget==null)return;t=retainedTarget;alpha=a;Bounds b=place("target",100,DeltaVisualSettings.flag("target.items",true)?34:24);box(ctx,b.x,b.y,100,24);float hs=24/1.35f,hy=b.y+(24-hs)/2;if(t instanceof AbstractClientPlayerEntity p)face(ctx,p.getSkin().body().texturePath(),b.x+5,hy,hs);else icon(ctx,"B",b.x+5,hy+hs/2,18,DeltaVisualSettings.text());float tx=b.x+10+hs;String name=t.getName().getString();if(name.codePointCount(0,name.length())>12)name=name.substring(0,name.offsetByCodePoints(0,12))+"…";text(ctx,name,tx,hy+3.75f,7.5f,DeltaVisualSettings.text());String hp=Integer.toString((int)t.getHealth());text(ctx,hp,b.x+95-gt.width(hp,7),hy+3.75f,7,DeltaVisualSettings.primary());float target=Math.max(0,Math.min(1,t.getHealth()/Math.max(1,t.getMaxHealth())));if(!t.getUuid().equals(healthTarget)){health=target;healthTarget=t.getUuid();}health+=(target-health)*(1-(float)Math.exp(-dt*10));rectFill(ctx,tx,hy+12.5f,54,3,fade(DeltaVisualSettings.primary(),.3f));rectFill(ctx,tx,hy+12.5f,54*health,3,DeltaVisualSettings.primary());if(DeltaVisualSettings.flag("target.items",true)){int i=0;for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){ItemStack s=t.getEquippedStack(slot);if(!s.isEmpty())item(ctx,s,b.x+90-9*i++,b.y+24,8.8f,true);}}alpha=1;}
    private void rectFill(DrawContext c,float x,float y,float w,float h,int color){if(w>0)c.fill((int)x,(int)y,(int)Math.ceil(x+w),(int)Math.ceil(y+h),fade(color,alpha));}
    private void armor(MinecraftClient c,DrawContext ctx,boolean preview){List<ItemStack> stacks=new ArrayList<>();for(var slot:ARMOR){var s=c.player.getEquippedStack(slot);if(!s.isEmpty())stacks.add(s);}if(c.player.isSpectator()||stacks.isEmpty()&&!preview)return;Bounds b=place("armor",Math.max(1,stacks.size())*20+1,22);ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED,Identifier.ofVanilla("hud/hotbar"),182,22,0,0,(int)b.x,(int)b.y,(int)b.w,22);int i=0;for(var s:stacks)item(ctx,s,b.x+3+20*i++,b.y+3,16,true);}
    private void staff(MinecraftClient c,DrawContext ctx,boolean preview){List<Row> rows=new ArrayList<>();Set<String> names=new HashSet<>();for(String name:DeltaVisualSettings.string("staff.names","").split(","))if(!name.isBlank())names.add(name.trim().toLowerCase(Locale.ROOT));if(c.getNetworkHandler()!=null)for(var e:c.getNetworkHandler().getPlayerList()){String name=e.getProfile().name();if(names.contains(name.toLowerCase(Locale.ROOT))){boolean near=c.world.getPlayers().stream().anyMatch(p->p.getName().getString().equalsIgnoreCase(name));rows.add(new Row(name,"y",name,near?"Near":"Online",ItemStack.EMPTY,e.getSkinTextures().body().texturePath(),null,DeltaVisualSettings.text()));}}list(ctx,"staff","i","Staff-list",rows,preview);}
    private void environment(MinecraftClient c,DrawContext ctx,boolean preview){List<Row> rows=new ArrayList<>();c.world.getPlayers().stream().filter(p->p!=c.player&&p.isAlive()&&!InteriumState.friends.contains(p.getUuid())).sorted(Comparator.comparingDouble(c.player::squaredDistanceTo)).limit(2).forEach(p->rows.add(new Row(p.getUuid().toString(),"x",p.getName().getString(),(int)p.getHealth()+"HP",ItemStack.EMPTY,p instanceof AbstractClientPlayerEntity a?a.getSkin().body().texturePath():null,null,DeltaVisualSettings.text())));list(ctx,"environment","x","Окружение",rows,preview);}
    private void notifications(DrawContext ctx,boolean preview,long now){notices.removeIf(n->now-n.time>3_000_000_000L);if(notices.isEmpty()){if(preview){Bounds b=place("notifications",100,12.5f);header(ctx,b,"i","Уведомления");}return;}float w=0;for(Notice n:notices)w=Math.max(w,21.5f+gt.width(n.text,7));Bounds b=place("notifications",w,notices.size()*16-3.5f);float y=b.y;for(Notice n:notices){float age=(now-n.time)/1_000_000_000f;alpha=Math.min(1,Math.min(age/.2f,(3-age)/.3f));header(ctx,new Bounds(b.x,y,b.w,12.5f),"i",n.text);y+=16;}alpha=1;}
}
