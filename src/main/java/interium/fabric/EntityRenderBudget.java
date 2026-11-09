package interium.fabric;
import net.minecraft.entity.*;import net.minecraft.entity.player.PlayerEntity;import net.minecraft.client.MinecraftClient;
public final class EntityRenderBudget {
 private static volatile boolean active;private static volatile double actors=4096,items=1024;
 static void update(){active=DeltaVisualSettings.flag("perf.entities",false);double a=DeltaVisualSettings.number("perf.entityRange",64,16,256),i=DeltaVisualSettings.number("perf.itemRange",32,8,128);actors=a*a;items=i*i;}
 public static boolean skip(Entity e,double distance){if(!active||e==null||e instanceof PlayerEntity||e==MinecraftClient.getInstance().getCameraEntity()||e==DeltaWorldVisuals.currentTarget())return false;return e instanceof ItemEntity?distance>items:e instanceof LivingEntity&&distance>actors;}
}
