package interium.fabric;

import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/** Initial crystal-breaking component, not full crystal PvP or damage prediction. */
final class CrystalBreaker {
    private int cooldown;
    void tick(MinecraftClient c){
        if(!InteriumClient.crystalBreakerEnabled || c.player==null || c.world==null || c.interactionManager==null || c.currentScreen!=null || !c.player.isAlive()) {cooldown=0;return;}
        if(cooldown>0){cooldown--;return;}
        if(c.player.getHealth()+c.player.getAbsorptionAmount()<=InteriumState.crystalMinHealth)return;
        double minSq=InteriumState.crystalMinDistance*InteriumState.crystalMinDistance;
        double maxSq=InteriumState.crystalRange*InteriumState.crystalRange;
        var target=StreamSupport.stream(c.world.getEntities().spliterator(),false)
            .filter(EndCrystalEntity.class::isInstance).map(EndCrystalEntity.class::cast)
            .filter(e->e.isAlive() && c.player.squaredDistanceTo(e)>=minSq && c.player.squaredDistanceTo(e)<=maxSq && c.player.canSee(e))
            .filter(e->c.world.getPlayers().stream().noneMatch(p->p!=c.player && p.isAlive() && (InteriumState.friends.contains(p.getUuid())||c.player.isTeammate(p)) && p.squaredDistanceTo(e)<144))
            .min(Comparator.comparingDouble(e->c.player.squaredDistanceTo(e))).orElse(null);
        if(target==null)return;
        var eye=c.player.getEyePos();var delta=target.getBoundingBox().getCenter().subtract(eye);
        if(delta.lengthSquared()<1e-8)return;
        c.player.setYaw((float)net.minecraft.util.math.MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z,delta.x))-90));
        c.player.setPitch((float)net.minecraft.util.math.MathHelper.clamp(-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))),-90,90));
        c.interactionManager.attackEntity(c.player,target);c.player.swingHand(Hand.MAIN_HAND);cooldown=6;
    }
}
