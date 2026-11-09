package interium.fabric;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Real look/ray-checked placement. Mineblaze preset is unverified, not a bypass claim. */
final class Scaffold {
    private int cooldown;
    private static final Direction[] SUPPORTS={Direction.DOWN,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST};
    void tick(MinecraftClient c) {
        if (!InteriumClient.scaffoldEnabled || c.player == null || c.world == null || c.interactionManager == null || c.currentScreen != null) {cooldown=0; return;}
        if (cooldown > 0) {cooldown--;return;}
        int old=c.player.getInventory().getSelectedSlot(),slot=old;
        if(!(c.player.getMainHandStack().getItem() instanceof BlockItem)&&DeltaVisualSettings.flag("scaffold.hotbar",true))for(int i=0;i<9;i++)if(c.player.getInventory().getStack(i).getItem() instanceof BlockItem bi&&bi.getBlock().getDefaultState().isFullCube(c.world,c.player.getBlockPos().down())){slot=i;break;}
        if(!(c.player.getInventory().getStack(slot).getItem() instanceof BlockItem))return;
        BlockItem item=(BlockItem)c.player.getInventory().getStack(slot).getItem();
        BlockPos dest=c.player.getBlockPos().down();
        if (!c.world.getBlockState(dest).isReplaceable() || !item.getBlock().getDefaultState().canPlaceAt(c.world,dest)) return;
        // Restrict supports to predictable, non-interactive full cubes. This avoids opening chests etc.
        for(Direction direction:SUPPORTS) {
            BlockPos support=dest.offset(direction); BlockState state=c.world.getBlockState(support);
            if (!allowedSupport(state) || !state.isFullCube(c.world,support) || state.hasBlockEntity()) continue;
            Direction face=direction.getOpposite(); Vec3d hit=Vec3d.ofCenter(support).offset(face,0.5);
            if(c.player.getEyePos().squaredDistanceTo(hit)>Math.pow(DeltaVisualSettings.number("scaffold.range",4.25f,1,4.5f),2)) continue;
            var p=c.player;Vec3d eye=p.getEyePos();double range=DeltaVisualSettings.number("scaffold.range",4.25f,1,4.5f);
            if(DeltaVisualSettings.flag("scaffold.rotate",true)){Vec3d d=hit.subtract(eye);float oldYaw=p.getYaw(),yaw=(float)(Math.toDegrees(Math.atan2(d.z,d.x))-90),pitch=(float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))),limit=DeltaVisualSettings.number("scaffold.turn",80,10,180);p.setYaw(oldYaw+net.minecraft.util.math.MathHelper.clamp(net.minecraft.util.math.MathHelper.wrapDegrees(yaw-oldYaw),-limit,limit));p.setPitch(net.minecraft.util.math.MathHelper.clamp(p.getPitch()+net.minecraft.util.math.MathHelper.clamp(pitch-p.getPitch(),-limit,limit),-89.9f,89.9f));var input=p.input.getMovementInput();double a=Math.toRadians(oldYaw-p.getYaw()),co=Math.cos(a),si=Math.sin(a);((interium.mixin.DeltaInputAccess)(Object)p.input).interium$movement(new net.minecraft.util.math.Vec2f((float)(input.x*co-input.y*si),(float)(input.x*si+input.y*co)));}
            BlockHitResult actual=new BlockHitResult(hit,face,support,false);if(DeltaVisualSettings.flag("scaffold.ray",true)){var ray=c.world.raycast(new net.minecraft.world.RaycastContext(eye,eye.add(p.getRotationVec(1).multiply(range)),net.minecraft.world.RaycastContext.ShapeType.COLLIDER,net.minecraft.world.RaycastContext.FluidHandling.NONE,p));if(ray.getType()!=net.minecraft.util.hit.HitResult.Type.BLOCK||!ray.getBlockPos().equals(support)||ray.getSide()!=face)continue;actual=ray;}
            if(slot!=old)p.getInventory().setSelectedSlot(slot);ActionResult result;
            try{result=c.interactionManager.interactBlock(p,Hand.MAIN_HAND,actual);}finally{if(slot!=old&&DeltaVisualSettings.flag("scaffold.restore",true))p.getInventory().setSelectedSlot(old);}
            cooldown=(int)DeltaVisualSettings.number("scaffold.delay",2,0,10);
            if(result.isAccepted()) c.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
    private boolean allowedSupport(BlockState state) {
        return state.isIn(BlockTags.WOOL) || state.isIn(BlockTags.PLANKS) || state.isIn(BlockTags.DIRT)
                || state.isIn(BlockTags.SAND) || state.isIn(BlockTags.BASE_STONE_OVERWORLD)
                || state.isIn(BlockTags.STONE_BRICKS) || state.isIn(BlockTags.TERRACOTTA)
                || state.isOf(Blocks.COBBLESTONE) || state.isOf(Blocks.END_STONE) || state.isOf(Blocks.SANDSTONE)
                || state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.GLASS);
    }
}
