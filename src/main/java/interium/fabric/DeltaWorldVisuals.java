package interium.fabric;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import interium.fabric.delta.DeltaFont;
import interium.mixin.DeltaRenderLayerAccess;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.world.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.*;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/** Source-based Delta world visuals adapted to extraction/drawing separation.
 * Render states contain copied geometry/labels/items only, never worlds or entities.
 * EntityESP's remote effect history and server-specific providers are not emulated.
 */
public final class DeltaWorldVisuals {
 private static final RenderStateDataKey<Frame> KEY=RenderStateDataKey.create(()->"interium:delta_world");
 private static final Frame EMPTY=new Frame(List.of(),List.of(),List.of(),List.of());
 private static volatile Frame latest=EMPTY;
 // Some renderer integrations leave WorldRenderContext.worldState() unset.
 // Keep only the immutable geometry from the latest extraction, never query entities while drawing.
 private static volatile Frame extracted=EMPTY;
 private static DeltaFont font;
 private static Object worldIdentity;
 private static final Deque<SoundMark> sounds=new ConcurrentLinkedDeque<>();
 private static final Map<BlockPos,Integer> blocks=new LinkedHashMap<>();
   private static int scanCursor,ticks;
 private static BlockPos scanCenter=BlockPos.ORIGIN;
 private static String scanConfig="";
 private record Segment(Vec3d a,Vec3d b,int color){}
 private record Quad(Vec3d a,Vec3d b,Vec3d c,Vec3d d,int color,boolean depth){}
 private record ScreenBox(float x,float y,float w,float h,int color,boolean corners,boolean health,float fraction){}
 private record Label(float x,float y,String text,int color,List<ItemStack> items,List<String> effects,ItemStack shulker){}
 private record Frame(List<Segment> lines,List<Quad> quads,List<ScreenBox> boxes,List<Label> labels){}
 private record SoundMark(Vec3d pos,String name,long time){}
 private static final RenderLayer LINES=layer("delta_world_lines",VertexFormat.DrawMode.DEBUG_LINES);
 private static final RenderLayer QUADS=layer("delta_world_quads",VertexFormat.DrawMode.QUADS);
 private static final RenderLayer HATS=RenderLayers.debugQuads();
 private static RenderLayer layer(String name,VertexFormat.DrawMode mode){
  RenderPipeline p=RenderPipeline.builder().withLocation(Identifier.of("interium",name))
   .withVertexShader(Identifier.ofVanilla("core/position_color")).withFragmentShader(Identifier.ofVanilla("core/position_color"))
   .withUniform("DynamicTransforms",UniformType.UNIFORM_BUFFER).withUniform("Projection",UniformType.UNIFORM_BUFFER)
   .withVertexFormat(VertexFormats.POSITION_COLOR,mode).withBlend(BlendFunction.TRANSLUCENT)
   .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
  return DeltaRenderLayerAccess.interium$create(name,RenderSetup.builder(p).translucent().expectedBufferSize(262144).build());
 }
 public static void init(){
  ClientTickEvents.END_CLIENT_TICK.register(DeltaWorldVisuals::tick);
  WorldRenderEvents.END_EXTRACTION.register(DeltaWorldVisuals::extract);
  WorldRenderEvents.BEFORE_DEBUG_RENDER.register(DeltaWorldVisuals::draw);
  WorldRenderEvents.END_MAIN.register(ctx->latest=frame(ctx));
 }
 private static void tick(MinecraftClient mc){
  if(worldIdentity!=mc.world){worldIdentity=mc.world;latest=EMPTY;extracted=EMPTY;blocks.clear();sounds.clear();scanCursor=0;ticks=0;}
  if(mc.world==null||mc.player==null)return;ticks++;
  if(DeltaVisualSettings.blockEsp)scan(mc);else blocks.clear();
 }
 public static void sound(SoundInstance s){
  if(!DeltaVisualSettings.soundEsp)return;String path=s.getId().getPath(),label=null;
  if(path.contains("entity.firework_rocket.launch")&&DeltaVisualSettings.flag("sound.firework",true))label="Фейерверк";
  else if(path.contains("entity.generic.explode")&&DeltaVisualSettings.flag("sound.explosion",true))label="Взрывы";
  else if(path.contains("item.trident.return")&&DeltaVisualSettings.flag("sound.trident",true))label="Трезубец";
  if(label==null)return;long now=System.nanoTime();sounds.removeIf(e->now-e.time>5_500_000_000L);Vec3d pos=new Vec3d(s.getX(),s.getY(),s.getZ());
  for(SoundMark e:sounds)if(Math.abs(e.pos.x-pos.x)<=.5&&Math.abs(e.pos.y-pos.y)<=.5&&Math.abs(e.pos.z-pos.z)<=.5)return;
  sounds.addLast(new SoundMark(pos,label,now));while(sounds.size()>128)sounds.removeFirst();
 }
 private static void scan(MinecraftClient mc){
  String config=DeltaVisualSettings.string("block.ids","");BlockPos center=mc.player.getBlockPos();
  if(!config.equals(scanConfig)||scanCenter.getSquaredDistance(center)>64){scanConfig=config;scanCenter=center.toImmutable();scanCursor=0;blocks.clear();}
  Set<Block> targets=new HashSet<>();for(String id:config.split(",")){Identifier ident=Identifier.tryParse(id.trim());if(ident!=null&&Registries.BLOCK.containsId(ident)){Block b=Registries.BLOCK.get(ident);if(b!=Blocks.AIR)targets.add(b);}}
  if(targets.isEmpty()){blocks.clear();return;}
  int radius=70,side=radius*2+1,height=mc.world.getHeight(),total=side*side*height;BlockPos.Mutable pos=new BlockPos.Mutable();
  for(int n=0;n<12000;n++){int index=scanCursor++;if(scanCursor>=total)scanCursor=0;int y=index%height+mc.world.getBottomY(),column=index/height;int x=scanCenter.getX()-radius+column%side,z=scanCenter.getZ()-radius+column/side;
   if(!mc.world.getChunkManager().isChunkLoaded(x>>4,z>>4))continue;pos.set(x,y,z);if(targets.contains(mc.world.getBlockState(pos).getBlock())){if(blocks.size()<4096)blocks.put(pos.toImmutable(),DeltaVisualSettings.primary());}else blocks.remove(pos);
  }
 }
 private static void extract(WorldExtractionContext ctx){
  var mc=MinecraftClient.getInstance();extracted=EMPTY;latest=EMPTY;
  var state=ctx.worldState();if(state!=null)state.setData(KEY,EMPTY);
  if(mc.player==null||mc.world==null||ctx.world()==null||ctx.camera()==null||mc.options.hudHidden)return;
  float tick=ctx.tickCounter().getTickProgress(false);Vec3d cam=ctx.camera().getCameraPos();Matrix4f vp=new Matrix4f(ctx.cullProjectionMatrix()).mul(ctx.viewMatrix());
  int sw=mc.getWindow().getScaledWidth(),sh=mc.getWindow().getScaledHeight();List<Segment> lines=new ArrayList<>();List<Quad> quads=new ArrayList<>();List<ScreenBox> boxes=new ArrayList<>();List<Label> labels=new ArrayList<>();
  for(Entity e:ctx.world().getEntities()){
   if(mc.player.squaredDistanceTo(e)>512*512)continue;
   Vec3d pos=e.getLerpedPos(tick);Box box=e.getBoundingBox().offset(pos.subtract(e.getEntityPos()));boolean self=e==mc.player,friend=InteriumState.friends.contains(e.getUuid());
   if(DeltaVisualSettings.entityBox&&(e instanceof PlayerEntity||e instanceof ItemEntity)&&(!self||!mc.options.getPerspective().isFirstPerson())){
    int mode=Math.round(DeltaVisualSettings.number("box.mode",0,0,3)),color=DeltaVisualSettings.flag("box.static",false)?0xff000000|(int)DeltaVisualSettings.number("box.color",0xffffff,0,0xffffff):DeltaVisualSettings.primary();
    if(mode==2)filled(quads,box,cam,(color&0xffffff)|0x50000000);else if(mode<2){float[] b=bounds(vp,cam,box,sw,sh);if(b!=null){float fraction=e instanceof LivingEntity l?Math.max(0,Math.min(1,l.getHealth()/Math.max(1,l.getMaxHealth()))):0;boxes.add(new ScreenBox(b[0],b[1],b[2]-b[0],b[3]-b[1],color,mode==1,DeltaVisualSettings.flag("box.health",false)&&e instanceof LivingEntity,fraction));}}
   }
   if(DeltaVisualSettings.pointers&&DeltaVisualSettings.flag("pointers.tracers",true)&&e instanceof PlayerEntity&&!self&&e.isAlive()&&(!DeltaVisualSettings.flag("pointers.friends",false)||friend)){
    Vec3d start=new Vec3d(0,0,27).rotateX((float)-Math.toRadians(ctx.camera().getPitch())).rotateY((float)-Math.toRadians(ctx.camera().getYaw()));lines.add(new Segment(start,pos.add(0,e.getHeight()/2,0).subtract(cam),friend?0xff00ff00:0xffffffff));
   }
   if(DeltaVisualSettings.entityEsp&&!self&&tracked(e)){
    Vector2f p=project(vp,cam,pos.add(0,e.getHeight()+.25,0),sw,sh);if(p!=null){String name=e instanceof ItemEntity item?item.getStack().getName().getString()+" ×"+item.getStack().getCount():e.getName().getString();if(e instanceof LivingEntity l)name+=(" "+(int)l.getHealth()+" HP");
     List<ItemStack> equipment=new ArrayList<>();List<String> effects=new ArrayList<>();if(e instanceof LivingEntity l){for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.OFFHAND}){ItemStack stack=l.getEquippedStack(slot);if(!stack.isEmpty())equipment.add(stack.copy());}for(var effect:l.getStatusEffects()){int seconds=effect.getDuration()/20;effects.add(Text.translatable(effect.getTranslationKey()).getString()+" "+(effect.getAmplifier()+1)+" "+(effect.isInfinite()?"∞":seconds/60+":"+String.format(Locale.ROOT,"%02d",seconds%60)));}}
     labels.add(new Label(p.x,p.y,name,friend?0x80006400:0x50000000,List.copyOf(equipment),List.copyOf(effects),ItemStack.EMPTY));}
   }
   if(DeltaVisualSettings.shulkerPreview&&e instanceof ItemEntity item&&item.getStack().getItem() instanceof BlockItem bi&&bi.getBlock() instanceof ShulkerBoxBlock&&item.getStack().get(net.minecraft.component.DataComponentTypes.CONTAINER)!=null){Vector2f p=project(vp,cam,pos.add(0,.5,0),sw,sh);if(p!=null)labels.add(new Label(p.x,p.y,"",0,List.of(),List.of(),item.getStack().copy()));}
  }
  if(DeltaVisualSettings.chinaHat)for(var player:ctx.world().getPlayers())if((player==mc.player||InteriumState.friends.contains(player.getUuid()))&&(player!=mc.player||!mc.options.getPerspective().isFirstPerson())){
   Vec3d center=player.getLerpedPos(tick).add(0,player.getEyeHeight(player.getPose())+.15,0).subtract(cam);float radius=player.getWidth(),pitch=(float)Math.toRadians(player.getPitch(tick)),yaw=(float)-Math.toRadians(player.headYaw);int color=player==mc.player?0xff000000|(int)DeltaVisualSettings.number("hat.color",0x859cff,0,0xffffff):0xff009600;Vec3d apex=new Vec3d(0,.3,0).rotateX(pitch).rotateY(yaw).add(center);
   for(int segment=0;segment<90;segment++){double a=segment*Math.PI/45,b=(segment+1)*Math.PI/45;Vec3d p=new Vec3d(Math.sin(a)*radius,0,Math.cos(a)*radius).rotateX(pitch).rotateY(yaw).add(center),q=new Vec3d(Math.sin(b)*radius,0,Math.cos(b)*radius).rotateX(pitch).rotateY(yaw).add(center);float f=.75f+.25f*(float)Math.sin(segment*.14+System.currentTimeMillis()/450d);int shaded=0xff000000|Math.round(((color>>16)&255)*f)<<16|Math.round(((color>>8)&255)*f)<<8|Math.round((color&255)*f);quads.add(new Quad(p,q,apex,apex,shaded,true));}
  }
  for(var prediction:DeltaPredictions.extract(mc,tick)){var path=prediction.path();if(path.size()<2)continue;int base=DeltaVisualSettings.primary();if(!prediction.held())for(int n=0;n<path.size()-1;n++){Vec3d p=path.get(n),q=path.get(n+1);int color=DeltaVisualSettings.flag("predict.rainbow",false)?java.awt.Color.HSBtoRGB((float)((p.x*.05+p.z*.05)%1),1,1):base;int alpha=Math.round(255*(.3f+.7f*(1-n/(float)(path.size()-1))));lines.add(new Segment(p.subtract(cam),q.subtract(cam),(color&0xffffff)|(alpha<<24)));}
   Vec3d end=path.getLast();if(prediction.hit()!=null)outline(lines,prediction.hit(),cam,0xffff6464);if(prediction.side()!=null){Direction.Axis axis=prediction.side().getAxis();Vec3d u=axis==Direction.Axis.Y?new Vec3d(1,0,0):new Vec3d(0,1,0),v=axis==Direction.Axis.Z?new Vec3d(1,0,0):new Vec3d(0,0,1);for(int n=0;n<32;n++){double a=n*Math.PI/16,b=(n+1)*Math.PI/16;lines.add(new Segment(end.add(u.multiply(Math.cos(a)*.33)).add(v.multiply(Math.sin(a)*.33)).subtract(cam),end.add(u.multiply(Math.cos(b)*.33)).add(v.multiply(Math.sin(b)*.33)).subtract(cam),0xffffffff));}}
   if(!prediction.held()){Vector2f p=project(vp,cam,end,sw,sh);if(p!=null)labels.add(new Label(p.x,p.y,String.format(Locale.ROOT,"%.1fs",(path.size()-1)/20f),0x82000000,List.of(prediction.icon()),List.of(),ItemStack.EMPTY));}
  }
  if(DeltaVisualSettings.blockEsp)for(var entry:blocks.entrySet())outline(lines,new Box(entry.getKey()),cam,entry.getValue());
  if(DeltaVisualSettings.soundEsp){long now=System.nanoTime();sounds.removeIf(s->now-s.time>5_500_000_000L);for(SoundMark s:sounds){Vector2f p=project(vp,cam,s.pos,sw,sh);if(p!=null)labels.add(new Label(p.x,p.y,s.name+" ["+(int)mc.player.getEntityPos().distanceTo(s.pos)+"м/"+(int)((now-s.time)/1e9)+" сек]",0x64000000,List.of(),List.of(),ItemStack.EMPTY));}}
  Frame result=new Frame(List.copyOf(lines),List.copyOf(quads),List.copyOf(boxes),List.copyOf(labels));
  extracted=result;latest=result;if(state!=null)state.setData(KEY,result);
 }
 private static boolean tracked(Entity e){return e instanceof PlayerEntity?DeltaVisualSettings.flag("esp.players",true):e instanceof HostileEntity?DeltaVisualSettings.flag("esp.mobs",false):e instanceof AnimalEntity||e instanceof VillagerEntity?DeltaVisualSettings.flag("esp.animals",false):e instanceof ItemEntity||e instanceof net.minecraft.entity.projectile.ArrowEntity?DeltaVisualSettings.flag("esp.items",false):false;}
 private static Vector2f project(Matrix4f vp,Vec3d camera,Vec3d p,int w,int h){Vector4f v=new Vector4f((float)(p.x-camera.x),(float)(p.y-camera.y),(float)(p.z-camera.z),1);vp.transform(v);if(v.w<=.001f||v.z< -v.w||v.z>v.w)return null;float x=(v.x/v.w*.5f+.5f)*w,y=(.5f-v.y/v.w*.5f)*h;if(!Float.isFinite(x)||!Float.isFinite(y)||x< -w*4||x>w*5||y< -h*4||y>h*5)return null;return new Vector2f(x,y);}
 private static float[] bounds(Matrix4f vp,Vec3d cam,Box b,int w,int h){float minX=Float.POSITIVE_INFINITY,minY=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX;int n=0;for(int i=0;i<8;i++){Vector2f p=project(vp,cam,new Vec3d((i&1)==0?b.minX:b.maxX,(i&2)==0?b.minY:b.maxY,(i&4)==0?b.minZ:b.maxZ),w,h);if(p!=null){n++;minX=Math.min(minX,p.x);minY=Math.min(minY,p.y);maxX=Math.max(maxX,p.x);maxY=Math.max(maxY,p.y);}}return n>=2&&maxX>minX&&maxY>minY?new float[]{minX,minY,maxX,maxY}:null;}
 private static void outline(List<Segment> out,Box b,Vec3d cam,int color){Vec3d[] p=corners(b,cam);for(int i=0;i<8;i++)for(int bit:new int[]{1,2,4})if((i&bit)==0)out.add(new Segment(p[i],p[i|bit],color));}
 private static Vec3d[] corners(Box b,Vec3d cam){Vec3d[] p=new Vec3d[8];for(int i=0;i<8;i++)p[i]=new Vec3d((i&1)==0?b.minX:b.maxX,(i&2)==0?b.minY:b.maxY,(i&4)==0?b.minZ:b.maxZ).subtract(cam);return p;}
 private static void filled(List<Quad> out,Box b,Vec3d cam,int c){Vec3d[] p=corners(b,cam);for(int[] f:new int[][]{{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}})out.add(new Quad(p[f[0]],p[f[1]],p[f[2]],p[f[3]],c,false));}
 /** Regression for the exact missing-state callback reported on the user's client.
  * Runs inside the real Mixin-enabled client, not a replacement for an Iris world test. */
 public static void smoke(){
  if(!Boolean.getBoolean("interium.visualSmoke"))return;
  WorldRenderContext absent=(WorldRenderContext)java.lang.reflect.Proxy.newProxyInstance(WorldRenderContext.class.getClassLoader(),new Class<?>[]{WorldRenderContext.class},(proxy,method,args)->null);
  Frame previous=extracted;
  try{
   extracted=EMPTY;if(frame(absent)!=EMPTY)throw new AssertionError("Missing state must resolve to empty extraction");draw(absent);
   Frame sample=new Frame(List.of(),List.of(),List.of(),List.of());extracted=sample;
   if(frame(absent)!=sample)throw new AssertionError("Missing state lost extracted geometry");draw(absent);
  }finally{extracted=previous;}
  System.out.println("INTERIUM_NULL_WORLDSTATE_REGRESSION_PASSED (callback regression, not a modpack/world test)");
 }
 private static Frame frame(WorldRenderContext ctx){
  var state=ctx.worldState();return state==null?extracted:state.getDataOrDefault(KEY,extracted);
 }
 private static void draw(WorldRenderContext ctx){Frame f=frame(ctx);if(f==EMPTY||ctx.matrices()==null||ctx.consumers()==null)return;var entry=ctx.matrices().peek();if(!f.lines.isEmpty()){var v=ctx.consumers().getBuffer(LINES);for(Segment s:f.lines){vertex(v,entry,s.a,s.color);vertex(v,entry,s.b,s.color);}}if(!f.quads.isEmpty()){for(Quad q:f.quads){var v=ctx.consumers().getBuffer(q.depth?HATS:QUADS);vertex(v,entry,q.a,q.color);vertex(v,entry,q.b,q.color);vertex(v,entry,q.c,q.color);vertex(v,entry,q.d,q.color);}}}
 private static void vertex(VertexConsumer v,net.minecraft.client.util.math.MatrixStack.Entry m,Vec3d p,int color){v.vertex(m,(float)p.x,(float)p.y,(float)p.z).color(color);}
 public static void hud(MinecraftClient mc,DrawContext c){
  if(mc.player==null||mc.world==null||mc.options.hudHidden)return;Frame frame=latest;
  if(font==null)try{font=DeltaFont.load(mc,"gt_regular");}catch(java.io.IOException ex){return;}
  for(ScreenBox b:frame.boxes){float l=b.x,t=b.y,r=l+b.w,d=t+b.h;if(b.corners){float n=Math.min(b.w,b.h)*.25f;for(float[] line:new float[][]{{l,t,n,0},{l,t,0,n},{r,t,-n,0},{r,t,0,n},{l,d,n,0},{l,d,0,-n},{r,d,-n,0},{r,d,0,-n}})edge(c,line[0],line[1],line[2],line[3],b.color);}else{edge(c,l,t,b.w,0,b.color);edge(c,l,d,b.w,0,b.color);edge(c,l,t,0,b.h,b.color);edge(c,r,t,0,b.h,b.color);}if(b.health){quad(c,l-3.5f,t-.5f,1.75f,b.h+1,0xff000000);int color=0xff000000|Math.round(255*(1-b.fraction))<<16|Math.round(255*b.fraction)<<8;quad(c,l-3,t+b.h*(1-b.fraction),.75f,b.h*b.fraction,color);}}
  for(Label label:frame.labels){if(!label.shulker.isEmpty()){var m=c.getMatrices();m.pushMatrix();m.translate(label.x-43.75f,label.y-35);m.scale(.5f,.5f);DeltaShulkerPreview.drawAt(c,label.shulker,0,0);m.popMatrix();continue;}float size=7.5f,w=font.width(label.text,size),x=label.x-w/2;quad(c,x-2,label.y,w+4,10,label.color);font.draw(c,label.text,x,label.y,size,0xffffffff);float ix=label.x-label.items.size()*6f;for(ItemStack item:label.items){var m=c.getMatrices();m.pushMatrix();m.translate(ix,label.y-12);m.scale(.625f,.625f);c.drawItem(item,0,0);c.drawStackOverlay(mc.textRenderer,item,0,0);m.popMatrix();ix+=12;}float y=label.y+11;for(String effect:label.effects){float ew=font.width(effect,6.5f);quad(c,label.x-ew/2-2,y,ew+4,9,label.color);font.draw(c,effect,label.x-ew/2,y,6.5f,0xffffffff);y+=10;}}
 }
 private static void edge(DrawContext c,float x,float y,float dx,float dy,int color){float l=Math.min(x,x+dx),t=Math.min(y,y+dy),w=dx==0?.75f:Math.abs(dx),h=dy==0?.75f:Math.abs(dy);if(dx==0)l-=.375f;if(dy==0)t-=.375f;quad(c,l-.5f,t-.5f,w+1,h+1,0xff000000);quad(c,l,t,w,h,color);}
 private static void quad(DrawContext c,float x,float y,float w,float h,int color){if(w<=0||h<=0)return;var m=c.getMatrices();m.pushMatrix();m.translate(x,y);m.scale(w,h);c.fill(0,0,1,1,color);m.popMatrix();}
}
