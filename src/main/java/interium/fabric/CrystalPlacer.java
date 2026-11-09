package interium.fabric;

import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Experimental placement only; proximity checks are not explosion damage prediction. */
final class CrystalPlacer {
    private int cooldown;
    void tick(MinecraftClient c){
        if(!InteriumClient.crystalPlacerEnabled || c.player==null || c.world==null || c.interactionManager==null || c.currentScreen!=null || !c.player.isAlive()){cooldown=0;return;}
        if(cooldown>0){cooldown--;return;}
        var p=c.player;
        Hand hand=p.getMainHandStack().isOf(Items.END_CRYSTAL)?Hand.MAIN_HAND:p.getOffHandStack().isOf(Items.END_CRYSTAL)?Hand.OFF_HAND:null;
        if(hand==null || p.isUsingItem() || p.getHealth()+p.getAbsorptionAmount()<=InteriumState.crystalMinHealth)return;
        LivingEntity target=StreamSupport.stream(c.world.getEntities().spliterator(),false)
            .filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
            .filter(e->e!=p && e.isAlive() && !e.isSpectator() && !p.isTeammate(e) && p.canSee(e))
            .filter(e->e instanceof PlayerEntity ? !InteriumState.friends.contains(e.getUuid()) : InteriumState.crystalPlaceAnimals && e instanceof PassiveEntity)
            .filter(e->p.squaredDistanceTo(e)<=100)
            .min(Comparator.comparingDouble(p::squaredDistanceTo)).orElse(null);
        if(target==null)return;
        Vec3d eye=p.getEyePos(); BlockPos center=p.getBlockPos(); BlockPos best=null; double bestScore=Double.MAX_VALUE;
        for(int x=-5;x<=5;x++)for(int y=-5;y<=4;y++)for(int z=-5;z<=5;z++){
            BlockPos pos=center.add(x,y,z); if(!c.world.isChunkLoaded(pos))continue;
            var state=c.world.getBlockState(pos);
            if(!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))continue;
            BlockPos above=pos.up(); if(!c.world.isAir(above))continue;
            Vec3d crystalPos=new Vec3d(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
            Vec3d hit=crystalPos.add(0,-.001,0);
            if(eye.squaredDistanceTo(hit)>InteriumState.crystalRange*InteriumState.crystalRange || p.squaredDistanceTo(crystalPos)<InteriumState.crystalMinDistance*InteriumState.crystalMinDistance)continue;
            double score=target.squaredDistanceTo(crystalPos); if(score>12.25 || score>=bestScore)continue;
            if(c.world.getPlayers().stream().anyMatch(f->f!=p && f.isAlive() && (InteriumState.friends.contains(f.getUuid())||p.isTeammate(f)) && f.squaredDistanceTo(crystalPos)<144))continue;
            Box space=new Box(above.getX(),above.getY(),above.getZ(),above.getX()+1,above.getY()+2,above.getZ()+1);
            if(!c.world.getOtherEntities(null,space).isEmpty())continue;
            var ray=c.world.raycast(new RaycastContext(eye,hit,RaycastContext.ShapeType.OUTLINE,RaycastContext.FluidHandling.NONE,p));
            if(ray.getType()!=HitResult.Type.BLOCK || !ray.getBlockPos().equals(pos) || ray.getSide()!=Direction.UP)continue;
            best=pos;bestScore=score;
        }
        if(best==null)return;
        Vec3d hit=new Vec3d(best.getX()+.5,best.getY()+1,best.getZ()+.5);Vec3d d=hit.subtract(eye);
        p.setYaw((float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d.z,d.x))-90));
        p.setPitch((float)MathHelper.clamp(-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))),-90,90));
        var result=c.interactionManager.interactBlock(p,hand,new BlockHitResult(hit,Direction.UP,best,false));
        if(result.isAccepted())p.swingHand(hand);
        cooldown=10;
    }
}
