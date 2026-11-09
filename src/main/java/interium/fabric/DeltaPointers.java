package interium.fabric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
/** Delta navigation-arrow path. 3D tracers are tracked separately, not silently substituted. */
final class DeltaPointers {
 private static final Identifier TEXTURE=Identifier.of("delta","pictures/pointer.png");
 private static float yaw;
 private static long last;
 static void render(MinecraftClient mc,DrawContext c){
  if(!DeltaVisualSettings.pointers||mc.player==null||mc.world==null||mc.options.hudHidden)return;
  long now=System.nanoTime();float dt=last==0?.016f:Math.min(.1f,(now-last)/1e9f);last=now;
  yaw+=MathHelper.wrapDegrees(mc.player.getYaw()-yaw)*(1-(float)Math.exp(-dt*12));
  float size=DeltaVisualSettings.number("pointers.size",7,5,15),radius=DeltaVisualSettings.number("pointers.radius",30,20,50);
  float cx=mc.getWindow().getScaledWidth()/2f,cy=mc.getWindow().getScaledHeight()/2f;
  for(var target:mc.world.getPlayers()){
   if(target==mc.player||!target.isAlive())continue;
   boolean friend=InteriumState.friends.contains(target.getUuid());if(DeltaVisualSettings.flag("pointers.friends",false)&&!friend)continue;
   float angle=MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(mc.player.getX()-target.getX(),target.getZ()-mc.player.getZ()))-yaw);
   float radians=(float)Math.toRadians(angle);var m=c.getMatrices();m.pushMatrix();m.translate(cx+(float)Math.sin(radians)*radius,cy-(float)Math.cos(radians)*radius);m.rotate(radians);m.translate(-size/2,-size/2);m.scale(size,size);
   c.drawTexture(RenderPipelines.GUI_TEXTURED,TEXTURE,0,0,0f,0f,1,1,1,1,1,1,friend?0xff55ff55:0xffffffff);m.popMatrix();
  }
 }
}
