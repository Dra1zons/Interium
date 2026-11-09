package interium.fabric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.item.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import java.util.*;
/** Delta's drag/gravity, 140-tick flight/130-tick aim and nearest-entity raycasts.
 * World access stays in extraction. Return values contain copied paths only.
 */
final class DeltaPredictions {
 record Prediction(List<Vec3d> path,ItemStack icon,Box hit,Direction side,boolean held){}
 static List<Prediction> extract(MinecraftClient mc,float tick){List<Prediction> result=new ArrayList<>();if(mc.player==null||mc.world==null||!DeltaVisualSettings.predictions)return result;
  int count=0;for(Entity e:mc.world.getEntities()){if(count>=64)break;if(e.getVelocity().lengthSquared()<1e-6||e.isOnGround()||mc.player.squaredDistanceTo(e)>512*512)continue;ItemStack icon=null;boolean thrown=false,potion=false;
   if(e instanceof TridentEntity&&DeltaVisualSettings.flag("predict.tridents",true))icon=Items.TRIDENT.getDefaultStack();
   else if(e instanceof ArrowEntity&&DeltaVisualSettings.flag("predict.arrows",true))icon=Items.ARROW.getDefaultStack();
   else if(e instanceof EnderPearlEntity&&DeltaVisualSettings.flag("predict.pearls",true)){icon=Items.ENDER_PEARL.getDefaultStack();thrown=true;}
   else if(e instanceof PotionEntity p&&DeltaVisualSettings.flag("predict.potions",true)){icon=p.getStack().copy();thrown=true;potion=true;}
   if(icon!=null){result.add(simulate(mc,e,e.getEntityPos(),e.getVelocity(),thrown,potion,140,icon,false));count++;}
  }
  ItemStack main=mc.player.getMainHandStack(),off=mc.player.getOffHandStack(),item=main;float speed=0;boolean thrown=false,potion=false;
  if(main.getItem() instanceof BowItem&&DeltaVisualSettings.flag("predict.arrows",true)){if(!mc.player.isUsingItem())return List.copyOf(result);float f=mc.player.getItemUseTime()/20f;speed=Math.min((f*f+2*f)/3,1)*3;if(speed<.3f)return List.copyOf(result);}
  else if(main.getItem() instanceof CrossbowItem&&DeltaVisualSettings.flag("predict.arrows",true))speed=3;
  else if(main.getItem() instanceof TridentItem&&DeltaVisualSettings.flag("predict.tridents",true))speed=2.5f;
  else if((main.isOf(Items.ENDER_PEARL)||off.isOf(Items.ENDER_PEARL))&&DeltaVisualSettings.flag("predict.pearls",true)){speed=1.5f;thrown=true;item=main.isOf(Items.ENDER_PEARL)?main:off;}
  else if((main.isOf(Items.SPLASH_POTION)||main.isOf(Items.LINGERING_POTION)||off.isOf(Items.SPLASH_POTION)||off.isOf(Items.LINGERING_POTION))&&DeltaVisualSettings.flag("predict.potions",true)){speed=.5f;thrown=true;potion=true;item=main.isOf(Items.SPLASH_POTION)||main.isOf(Items.LINGERING_POTION)?main:off;}
  if(speed>0){var charged=main.get(DataComponentTypes.CHARGED_PROJECTILES);float[] spread=main.getItem() instanceof CrossbowItem&&charged!=null&&charged.getProjectiles().size()>1?new float[]{-10,0,10}:new float[]{0};for(float angle:spread){double pitch=Math.toRadians(mc.player.getPitch(tick)),yaw=Math.toRadians(mc.player.getYaw(tick));Vec3d look=new Vec3d(-Math.sin(yaw)*Math.cos(pitch),-Math.sin(Math.toRadians(mc.player.getPitch(tick)+(potion?-20:0))),Math.cos(yaw)*Math.cos(pitch)).normalize();if(angle!=0){Vec3d right=new Vec3d(0,1,0).crossProduct(look);Vec3d axis=look.crossProduct(right.lengthSquared()<1e-10?new Vec3d(Math.cos(yaw),0,Math.sin(yaw)):right.normalize()).normalize();double a=Math.toRadians(angle),c=Math.cos(a),s=Math.sin(a);look=look.multiply(c).add(axis.crossProduct(look).multiply(s)).add(axis.multiply(axis.dotProduct(look)*(1-c)));}Vec3d velocity=look.multiply(speed).add(mc.player.getVelocity().x,mc.player.isOnGround()?0:mc.player.getVelocity().y,mc.player.getVelocity().z);result.add(simulate(mc,mc.player,mc.player.getCameraPosVec(tick),velocity,thrown,potion,130,item.copy(),true));}}
  return List.copyOf(result);
 }
 private static Prediction simulate(MinecraftClient mc,Entity source,Vec3d pos,Vec3d velocity,boolean thrown,boolean potion,int max,ItemStack icon,boolean held){List<Vec3d> path=new ArrayList<>();path.add(pos);double gravity=potion?.05000000070627959:.030000000582077163;
  for(int n=0;n<max&&velocity.lengthSquared()>=1e-6&&pos.y>=mc.world.getBottomY()&&pos.y<=mc.world.getBottomY()+mc.world.getHeight();n++){
   if(!mc.world.getChunkManager().isChunkLoaded(BlockPos.ofFloored(pos).getX()>>4,BlockPos.ofFloored(pos).getZ()>>4))break;
   double drag=mc.world.getFluidState(BlockPos.ofFloored(pos)).isIn(FluidTags.WATER)?thrown?.7999999144424994:.6000000001891753:.990000120151185;if(thrown)velocity=new Vec3d(velocity.x*drag,(velocity.y-gravity)*drag,velocity.z*drag);
   Vec3d next=pos.add(velocity);BlockHitResult block=mc.world.raycast(new RaycastContext(pos,next,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,source));Vec3d end=block.getType()!=HitResult.Type.MISS?block.getPos():next;
   if(held){Entity closest=null;Vec3d at=null;double distance=Double.MAX_VALUE;for(Entity candidate:mc.world.getOtherEntities(source,new Box(pos,end).expand(1)))if(candidate instanceof LivingEntity&&candidate.isAlive()&&!candidate.isSpectator()){var hit=candidate.getBoundingBox().expand(.30000001176381136).raycast(pos,end);if(hit.isPresent()&&pos.squaredDistanceTo(hit.get())<distance){closest=candidate;at=hit.get();distance=pos.squaredDistanceTo(at);}}if(closest!=null){path.add(at);return new Prediction(List.copyOf(path),icon,closest.getBoundingBox(),null,true);}}
   if(block.getType()!=HitResult.Type.MISS){path.add(block.getPos());return new Prediction(List.copyOf(path),icon,null,block.getSide(),held);}pos=next;if(!thrown)velocity=new Vec3d(velocity.x*drag,velocity.y*drag-.05000000070627959,velocity.z*drag);path.add(pos);
  }return new Prediction(List.copyOf(path),icon,null,null,held);
 }
}
