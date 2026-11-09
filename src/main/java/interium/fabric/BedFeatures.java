package interium.fabric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Client-side loaded-block search and ordinary break interactions, with no packet spoofing. */
final class BedFeatures {
    private final List<BlockPos> nearby = new ArrayList<>();
    private BlockPos target;
    private int scanTimer, targetTimer;
    private boolean breaking;
    void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            nearby.clear(); target = null; breaking = false; scanTimer = targetTimer = 0; return;
        }
        if (InteriumClient.bedRadarEnabled) {
            if (client.currentScreen == null && --scanTimer <= 0) { scanTimer = 40; scanRadar(client); }
        } else { nearby.clear(); scanTimer = 0; }
        if (!InteriumClient.bedBreakerEnabled || client.currentScreen != null || client.options.attackKey.isPressed()) {
            stop(client); target = null; targetTimer = 0; return;
        }
        if (--targetTimer <= 0 || target == null || !(client.world.getBlockState(target).getBlock() instanceof BedBlock)
                || client.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(target)) > 20.25) {
            targetTimer = 10; target = nearestBed(client, 4, 20.25);
        }
        if (target == null) { stop(client); return; }
        Vec3d eye = client.player.getEyePos();
        BlockHitResult hit = client.world.raycast(new RaycastContext(eye, Vec3d.ofCenter(target), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));
        if (hit.getType() != HitResult.Type.BLOCK || eye.squaredDistanceTo(hit.getPos()) > 20.25) { stop(client); return; }
        BlockPos pos = hit.getBlockPos(); BlockState state = client.world.getBlockState(pos);
        if (state.isAir() || state.getHardness(client.world, pos) < 0) { stop(client); return; }
        client.interactionManager.updateBlockBreakingProgress(pos, hit.getSide());
        client.player.swingHand(Hand.MAIN_HAND);
        breaking = true;
    }
    private void stop(MinecraftClient c) { if (breaking && c.interactionManager != null) c.interactionManager.cancelBlockBreaking(); breaking = false; }
    private BlockPos nearestBed(MinecraftClient c, int radius, double distanceSquared) {
        Vec3d eye = c.player.getEyePos(); BlockPos base = c.player.getBlockPos(); BlockPos best = null;
        double nearest = distanceSquared;
        for (BlockPos p : BlockPos.iterate(base.add(-radius,-radius,-radius),base.add(radius,radius,radius))) {
            if (!(c.world.getBlockState(p).getBlock() instanceof BedBlock)) continue;
            double d = eye.squaredDistanceTo(Vec3d.ofCenter(p));
            if (d <= nearest) { nearest=d; best=p.toImmutable(); }
        }
        return best;
    }
    private void scanRadar(MinecraftClient c) {
        nearby.clear(); BlockPos base=c.player.getBlockPos(); Vec3d eye=c.player.getEyePos();
        for (BlockPos p:BlockPos.iterate(base.add(-12,-6,-12),base.add(12,6,12))) {
            BlockState s=c.world.getBlockState(p);
            if (s.getBlock() instanceof BedBlock && s.get(BedBlock.PART)==BedPart.HEAD && eye.squaredDistanceTo(Vec3d.ofCenter(p))<=144) nearby.add(p.toImmutable());
        }
        nearby.sort(Comparator.comparingDouble(p -> eye.squaredDistanceTo(Vec3d.ofCenter(p))));
        if(nearby.size()>4) nearby.subList(4,nearby.size()).clear();
    }
    void render(MinecraftClient c, DrawContext context) {
        if (!InteriumClient.bedRadarEnabled || c.player==null || c.world==null) return;
        int y=36;
        for (BlockPos p:nearby) {
            int distance=(int)Math.round(c.player.getEyePos().distanceTo(Vec3d.ofCenter(p)));
            String label="Bed " + distance + "m ["+p.getX()+", "+p.getY()+", "+p.getZ()+"]";
            int x=Math.max(2,context.getScaledWindowWidth()-200);
            context.fill(x,y-2,x+194,y+12,0xC0181826);
            context.drawTextWithShadow(c.textRenderer,label,x+5,y,0xFFF4F2FF); y+=15;
        }
    }
}
