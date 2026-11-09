package interium.fabric;
import net.minecraft.client.MinecraftClient;import net.minecraft.entity.*;import net.minecraft.entity.player.PlayerEntity;import net.minecraft.entity.vehicle.AbstractBoatEntity;import interium.core.ModuleMath;
final class Speed {
 void tick(MinecraftClient c){var p=c.player;if(!InteriumClient.speedEnabled||p==null||c.world==null||c.currentScreen!=null||!p.isAlive()||p.getAbilities().flying||p.isGliding()||p.hasVehicle()||p.isSneaking()||p.isTouchingWater()||p.isInLava()||p.isClimbing())return;
 var input=p.input.getMovementInput();double side=input.x,forward=input.y,len=Math.hypot(side,forward);
 if(InteriumState.speedMode==2){if(DeltaVisualSettings.flag("speed.entity.moving",true)&&len<1e-4)return;double range=DeltaVisualSettings.number("speed.entity.range",3,.5f,10),expand=DeltaVisualSettings.number("speed.entity.expand",.5f,.1f,2),extent=Math.max(range,expand+1);boolean players=DeltaVisualSettings.flag("speed.entity.players",true);var box=p.getBoundingBox().expand(expand);int collisions=0;Entity nearest=null;double best=range*range;
 for(Entity e:c.world.getOtherEntities(p,p.getBoundingBox().expand(extent,c.world.getHeight()+64,extent),e->!e.isRemoved()&&(players?e instanceof PlayerEntity:e instanceof LivingEntity||e instanceof AbstractBoatEntity))){if(box.intersects(e.getBoundingBox()))collisions++;double dx=e.getX()-p.getX(),dz=e.getZ()-p.getZ(),sq=dx*dx+dz*dz;if(sq<=range*range&&(nearest==null||sq<best)){best=sq;nearest=e;}}
 if(collisions>0&&nearest!=null){double[] d=ModuleMath.direction(nearest.getX()-p.getX(),nearest.getZ()-p.getZ(),ModuleMath.entityImpulse(DeltaVisualSettings.number("speed.entity.power",8,1,20),collisions));p.addVelocity(d[0],0,d[1]);}return;}
 if(len<1e-4)return;side/=len;forward/=len;if(InteriumState.speedMode==1&&p.isOnGround())p.jump();double yaw=Math.toRadians(p.getYaw()),sin=Math.sin(yaw),cos=Math.cos(yaw);var old=p.getVelocity();p.setVelocity((side*cos-forward*sin)*InteriumState.speedValue,old.y,(forward*cos+side*sin)*InteriumState.speedValue);
 }
}
