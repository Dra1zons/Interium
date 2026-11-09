package interium.fabric;

import net.minecraft.client.MinecraftClient;

/** Basic local-velocity flight; does not grant server permission to fly. */
final class Flight {
    boolean tick(MinecraftClient c){
        var p=c.player;
        if(!InteriumClient.flightEnabled || p==null || c.world==null || c.currentScreen!=null || !p.isAlive()
                || p.hasVehicle() || p.isGliding() || p.getAbilities().flying) return false;
        var input=p.input.getMovementInput();
        double side=input.x, forward=input.y, len=Math.hypot(side,forward);
        if(len>1){side/=len;forward/=len;}
        double yaw=Math.toRadians(p.getYaw());
        double vertical=(c.options.jumpKey.isPressed()?0.35:0.0)-(c.options.sneakKey.isPressed()?0.35:0.0);
        p.setVelocity((side*Math.cos(yaw)-forward*Math.sin(yaw))*.35,vertical,(forward*Math.cos(yaw)+side*Math.sin(yaw))*.35);
        return true;
    }
}
