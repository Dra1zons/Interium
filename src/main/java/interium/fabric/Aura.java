package interium.fabric;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Bounded Delta/Newcode-inspired adaptation, not their original event engines or a verified server bypass. */
final class Aura {
 static final String[] ROTATIONS={"ФанТайм","ФанТайм ФОВ","Легит","Grim","SpookyTime"};
 private long nextHit;private Object world;private int scanAge=-100;private java.util.List<net.minecraft.entity.Entity> nearby=java.util.List.of();
 private float lastYawStep,lastPitchStep;
 static float n(String key,float value,float min,float max){return DeltaVisualSettings.number("aura."+key,value,min,max);}
 static boolean f(String key,boolean value){return DeltaVisualSettings.flag("aura."+key,value);}

    private LivingEntity locked;
    LivingEntity target(){ return locked; }
    void clear(){locked=null;nextHit=0;nearby=java.util.List.of();scanAge=-100;lastYawStep=lastPitchStep=0;}
    void tick(MinecraftClient c) {
        if(world!=c.world){world=c.world;clear();}
        if (!InteriumClient.auraEnabled || c.player == null || c.world == null || c.interactionManager == null || c.currentScreen != null || !c.player.isAlive()) {clear();return;}
        if (InteriumState.auraStopUsing && c.player.isUsingItem()) {locked=null;return;}
        final var player=c.player;
        Vec3d eye=player.getEyePos();
        Comparator<LivingEntity> ordering=switch(InteriumState.auraPriority) {
            case 1 -> Comparator.comparingDouble(e -> distanceSquared(eye,e.getBoundingBox()));
            case 2 -> Comparator.comparingDouble(LivingEntity::getHealth);
            default -> Comparator.comparingDouble(e -> angle(player.getRotationVec(1f),e.getBoundingBox().getCenter().subtract(eye)));
        };
        double search=InteriumState.auraRange+InteriumState.auraExtra;
        if(player.age-scanAge>=(int)n("scanTicks",2,1,10)||nearby.isEmpty()){nearby=c.world.getOtherEntities(player,player.getBoundingBox().expand(search+1));scanAge=player.age;}
        List<LivingEntity> candidates=nearby.stream()
            .filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
            .filter(e -> e!=player && e.isAlive() && !e.isSpectator() && !player.isTeammate(e))
            .filter(this::validType)
            .filter(e -> distanceSquared(eye,e.getBoundingBox())<=search*search)
            .filter(e->!f("walls",true)||player.canSee(e))
            .filter(e->(int)n("rotation",3,0,4)!=1||angle(player.getRotationVec(1f),e.getBoundingBox().getCenter().subtract(eye))<=Math.toRadians(n("fov",100,10,360)*.5)).toList();
        LivingEntity target=InteriumState.auraLock && locked!=null && candidates.contains(locked)?locked:candidates.stream().min(ordering).orElse(null);
        locked=target;
        if(target==null)return;
        Box aim=target.getBoundingBox();
        Vec3d delta=new Vec3d(MathHelper.clamp(eye.x,aim.minX+.04,aim.maxX-.04),aim.minY+(aim.maxY-aim.minY)*n("aimHeight",.65f,.1f,.95f),MathHelper.clamp(eye.z,aim.minZ+.04,aim.maxZ-.04)).add(target.getVelocity().multiply(n("lead",0,0,2))).subtract(eye);
        if(delta.lengthSquared()<1e-8)return;
        float desiredYaw=(float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z,delta.x))-90);
        float desiredPitch=(float)MathHelper.clamp(-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))),-90,90);
        float oldYaw=player.getYaw(),oldPitch=player.getPitch();double dy=interium.core.ModuleMath.wrap(desiredYaw-oldYaw),dp=desiredPitch-oldPitch,limit=InteriumState.auraTurnSpeed,pitchLimit=n("pitchSpeed",45,1,180),fraction=1;
        int mode=(int)n("rotation",3,0,4);switch(mode){case 0->{fraction=.8;pitchLimit=Math.min(pitchLimit,30);}case 1->{fraction=.8;pitchLimit=Math.min(pitchLimit,30);}case 2->{fraction=n("smooth",.25f,.05f,1);limit=Math.min(limit,35);pitchLimit=Math.min(pitchLimit,20);}case 3->{limit=Math.min(limit,150);pitchLimit=Math.min(pitchLimit,90);}case 4->{boolean active=player.getAttackCooldownProgress(0)<.7f;fraction=active?.5:.8;limit=Math.min(limit,active?70:45);pitchLimit=Math.min(pitchLimit,active?65:40);}}
        double ys=interium.core.ModuleMath.turn(dy,limit,fraction),ps=interium.core.ModuleMath.turn(dp,pitchLimit,fraction);
        if(mode==2||mode==4){double inertia=mode==2?.35:.2;ys=ys*(1-inertia)+lastYawStep*inertia;ps=ps*(1-inertia)+lastPitchStep*inertia;if(Math.signum(ys)!=Math.signum(dy))ys=0;if(Math.signum(ps)!=Math.signum(dp))ps=0;}
        ys=Math.copySign(Math.min(Math.abs(ys),Math.abs(dy)),dy);ps=Math.copySign(Math.min(Math.abs(ps),Math.abs(dp)),dp);
        if(f("quantize",true)){double sens=c.options.getMouseSensitivity().getValue()*.6+.2,gcd=sens*sens*sens*8*.15;ys=interium.core.ModuleMath.quantize(ys,gcd);ps=interium.core.ModuleMath.quantize(ps,gcd);}
        lastYawStep=(float)ys;lastPitchStep=(float)ps;player.setYaw(oldYaw+(float)ys);player.setPitch(MathHelper.clamp(oldPitch+(float)ps,-89,89));
        if(f("moveFix",true)){var input=player.input.getMovementInput();double a=Math.toRadians(oldYaw-player.getYaw()),co=Math.cos(a),si=Math.sin(a);((interium.mixin.DeltaInputAccess)(Object)player.input).interium$movement(new net.minecraft.util.math.Vec2f((float)(input.x*co-input.y*si),(float)(input.x*si+input.y*co)));}
        Vec3d end=eye.add(player.getRotationVec(1f).multiply(InteriumState.auraRange));
        var hit=target.getBoundingBox().raycast(eye,end);
        if(hit.isEmpty())return; // Wait until the actual look ray reaches the hitbox, within attack range.
        Vec3d hitPos=hit.get();
        var block=c.world.raycast(new RaycastContext(eye,hitPos,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,player));
        if(f("walls",true)&&block.getType()==HitResult.Type.BLOCK && eye.squaredDistanceTo(block.getPos())+1e-6<eye.squaredDistanceTo(hitPos))return;
        // Avoid attacking through another living entity, including allies, on the same line.
        double hitDistance=eye.squaredDistanceTo(hitPos);
        boolean blocked=c.world.getOtherEntities(player,new Box(eye,hitPos).expand(.2)).stream()
            .filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
            .filter(e->e!=player && e!=target && e.isAlive() && !e.isSpectator())
            .anyMatch(e->e.getBoundingBox().raycast(eye,hitPos).map(v->eye.squaredDistanceTo(v)<hitDistance).orElse(false));
        if(blocked)return;
        boolean falling=player.fallDistance>0 && !player.isOnGround() && !player.isTouchingWater() && !player.isInLava() && !player.isClimbing() && !player.hasVehicle() && !player.hasStatusEffect(StatusEffects.BLINDNESS);
        if(InteriumState.auraOnlyCrits && !falling && !(InteriumState.auraAdaptive && player.isOnGround() && !player.input.playerInput.jump()))return;
        int oldSlot=player.getInventory().getSelectedSlot(), attackSlot=oldSlot;
        if(InteriumState.auraShieldBreak && target.isBlocking() && !(player.getMainHandStack().getItem() instanceof AxeItem)){
            for(int i=0;i<9;i++)if(player.getInventory().getStack(i).getItem() instanceof AxeItem){attackSlot=i;break;}
        }
        if(attackSlot!=oldSlot)player.getInventory().setSelectedSlot(attackSlot);
        try{
            boolean legacy=f("legacy",false);long now=System.nanoTime();if(now<nextHit||!legacy&&player.getAttackCooldownProgress(.5f)<n("cooldown",.93f,.8f,1))return;
            if(falling && InteriumState.auraOnlyCrits)player.setSprinting(false);
            c.interactionManager.attackEntity(player,target);player.swingHand(Hand.MAIN_HAND);nextHit=now+(legacy?interium.core.ModuleMath.interval(n("cpsMin",8,1,20),n("cpsMax",12,1,20),java.util.concurrent.ThreadLocalRandom.current().nextDouble()):50_000_000L);
        }finally{if(attackSlot!=oldSlot)player.getInventory().setSelectedSlot(oldSlot);}
    }
    private boolean validType(LivingEntity e) {
        if(!f("invisible",false)&&e.isInvisible())return false;
        if(e instanceof PlayerEntity p && !f("naked",true)&&p.getArmor()==0)return false;
        if(e instanceof PlayerEntity p) return InteriumState.auraPlayers && !InteriumState.friends.contains(p.getUuid());
        if(e instanceof HostileEntity || e instanceof SlimeEntity)return InteriumState.auraHostiles;
        if(e instanceof PassiveEntity || e instanceof GolemEntity)return InteriumState.auraAnimals;
        return false;
    }
    private static double distanceSquared(Vec3d p,Box b){double x=MathHelper.clamp(p.x,b.minX,b.maxX),y=MathHelper.clamp(p.y,b.minY,b.maxY),z=MathHelper.clamp(p.z,b.minZ,b.maxZ);return p.squaredDistanceTo(x,y,z);}
    private static double angle(Vec3d look,Vec3d delta){return delta.lengthSquared()<1e-8?0:Math.acos(MathHelper.clamp(look.normalize().dotProduct(delta.normalize()),-1,1));}
}
