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

/** Partial Delta-inspired Aura for 1.21.11, not original rotation/movement correction. */
final class Aura {
    private LivingEntity locked;
    LivingEntity target(){ return locked; }
    void clear(){locked=null;}
    void tick(MinecraftClient c) {
        if (!InteriumClient.auraEnabled || c.player == null || c.world == null || c.interactionManager == null || c.currentScreen != null || !c.player.isAlive()) {locked=null;return;}
        if (InteriumState.auraStopUsing && c.player.isUsingItem()) {locked=null;return;}
        final var player=c.player;
        Vec3d eye=player.getEyePos();
        Comparator<LivingEntity> ordering=switch(InteriumState.auraPriority) {
            case 1 -> Comparator.comparingDouble(e -> distanceSquared(eye,e.getBoundingBox()));
            case 2 -> Comparator.comparingDouble(LivingEntity::getHealth);
            default -> Comparator.comparingDouble(e -> angle(player.getRotationVec(1f),e.getBoundingBox().getCenter().subtract(eye)));
        };
        double search=InteriumState.auraRange+InteriumState.auraExtra;
        List<LivingEntity> candidates=StreamSupport.stream(c.world.getEntities().spliterator(),false)
            .filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
            .filter(e -> e!=player && e.isAlive() && !e.isSpectator() && !player.isTeammate(e))
            .filter(this::validType)
            .filter(e -> distanceSquared(eye,e.getBoundingBox())<=search*search)
            .filter(player::canSee).toList();
        LivingEntity target=InteriumState.auraLock && locked!=null && candidates.contains(locked)?locked:candidates.stream().min(ordering).orElse(null);
        locked=target;
        if(target==null)return;
        Vec3d delta=target.getBoundingBox().getCenter().subtract(eye);
        if(delta.lengthSquared()<1e-8)return;
        float desiredYaw=(float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z,delta.x))-90);
        float desiredPitch=(float)MathHelper.clamp(-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))),-90,90);
        float turn=InteriumState.auraTurnSpeed;
        player.setYaw(player.getYaw()+MathHelper.clamp(MathHelper.wrapDegrees(desiredYaw-player.getYaw()),-turn,turn));
        player.setPitch(MathHelper.clamp(player.getPitch()+MathHelper.clamp(desiredPitch-player.getPitch(),-turn,turn),-90,90));
        Vec3d end=eye.add(player.getRotationVec(1f).multiply(InteriumState.auraRange));
        var hit=target.getBoundingBox().raycast(eye,end);
        if(hit.isEmpty())return; // Wait until the actual look ray reaches the hitbox, within attack range.
        Vec3d hitPos=hit.get();
        var block=c.world.raycast(new RaycastContext(eye,hitPos,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,player));
        if(block.getType()==HitResult.Type.BLOCK && eye.squaredDistanceTo(block.getPos())+1e-6<eye.squaredDistanceTo(hitPos))return;
        // Avoid attacking through another living entity, including allies, on the same line.
        double hitDistance=eye.squaredDistanceTo(hitPos);
        boolean blocked=StreamSupport.stream(c.world.getEntities().spliterator(),false)
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
            if(player.getAttackCooldownProgress(.5f)<.9f)return;
            if(falling && InteriumState.auraOnlyCrits)player.setSprinting(false);
            c.interactionManager.attackEntity(player,target);player.swingHand(Hand.MAIN_HAND);
        }finally{if(attackSlot!=oldSlot)player.getInventory().setSelectedSlot(oldSlot);}
    }
    private boolean validType(LivingEntity e) {
        if(e instanceof PlayerEntity p) return InteriumState.auraPlayers && !InteriumState.friends.contains(p.getUuid());
        if(e instanceof HostileEntity || e instanceof SlimeEntity)return InteriumState.auraHostiles;
        if(e instanceof PassiveEntity || e instanceof GolemEntity)return InteriumState.auraAnimals;
        return false;
    }
    private static double distanceSquared(Vec3d p,Box b){double x=MathHelper.clamp(p.x,b.minX,b.maxX),y=MathHelper.clamp(p.y,b.minY,b.maxY),z=MathHelper.clamp(p.z,b.minZ,b.maxZ);return p.squaredDistanceTo(x,y,z);}
    private static double angle(Vec3d look,Vec3d delta){return delta.lengthSquared()<1e-8?0:Math.acos(MathHelper.clamp(look.normalize().dotProduct(delta.normalize()),-1,1));}
}
