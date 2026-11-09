package interium.fabric;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/** Client-side circle movement around the current Aura target; no server bypass. */
final class TargetStrafe {
    /** Returns true when this module took control of horizontal velocity. */
    boolean tick(MinecraftClient c, LivingEntity target){
        var p=c.player;
        if(!InteriumClient.targetStrafeEnabled || !InteriumClient.auraEnabled || p==null || target==null || c.world==null || c.currentScreen!=null
                || !p.isAlive() || !target.isAlive() || p.getAbilities().flying || p.isGliding() || p.hasVehicle()
                || p.isSneaking() || p.isTouchingWater() || p.isInLava() || p.isClimbing())return false;
        var input=p.input.getMovementInput();
        if(InteriumState.targetStrafeInput && input.x*input.x+input.y*input.y<1e-4)return false;
        double dx=p.getX()-target.getX(), dz=p.getZ()-target.getZ();
        double dist=Math.hypot(dx,dz);
        if(dist<.05 || Math.abs(p.getY()-target.getY())>4)return false;
        double nx=dx/dist,nz=dz/dist, direction=InteriumState.targetStrafeClockwise?1:-1;
        double correction=MathHelper.clamp((InteriumState.targetStrafeRadius-dist)*.7,-.8,.8);
        double vx=-nz*direction+nx*correction, vz=nx*direction+nz*correction;
        double len=Math.hypot(vx,vz);
        var velocity=p.getVelocity();
        p.setVelocity(vx/len*InteriumState.targetStrafeSpeed,velocity.y,vz/len*InteriumState.targetStrafeSpeed);
        return true;
    }
}
