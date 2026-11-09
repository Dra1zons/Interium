package interium.fabric;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

/** Experimental client-side movement, not a server-specific bypass. */
final class Speed {
    void tick(MinecraftClient c){
        var p=c.player;
        if(!InteriumClient.speedEnabled || p==null || c.world==null || c.currentScreen!=null || !p.isAlive()
                || p.getAbilities().flying || p.isGliding() || p.hasVehicle() || p.isSneaking()
                || p.isTouchingWater() || p.isInLava() || p.isClimbing())return;
        var input=p.input.getMovementInput();
        double side=input.x,forward=input.y,len=Math.hypot(side,forward);
        if(len<1e-4)return;
        side/=len;forward/=len;
        if(InteriumState.speedMode==1 && p.isOnGround())p.jump();
        double yaw=Math.toRadians(p.getYaw()),sin=Math.sin(yaw),cos=Math.cos(yaw);
        double speed=InteriumState.speedValue;
        var old=p.getVelocity();
        p.setVelocity((side*cos-forward*sin)*speed,old.y,(forward*cos+side*sin)*speed);
    }
}
