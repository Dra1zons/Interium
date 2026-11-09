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

/** Simple, non-rotating block placement; not a server bypass. */
final class Scaffold {
    private int cooldown;
    void tick(MinecraftClient c) {
        if (!InteriumClient.scaffoldEnabled || c.player == null || c.world == null || c.interactionManager == null || c.currentScreen != null) {cooldown=0; return;}
        if (cooldown > 0) {cooldown--;return;}
        if (!(c.player.getMainHandStack().getItem() instanceof BlockItem)) return;
        BlockItem item=(BlockItem)c.player.getMainHandStack().getItem();
        BlockPos dest=c.player.getBlockPos().down();
        if (!c.world.getBlockState(dest).isReplaceable() || !item.getBlock().getDefaultState().canPlaceAt(c.world,dest)) return;
        // Restrict supports to predictable, non-interactive full cubes. This avoids opening chests etc.
        for(Direction direction:new Direction[]{Direction.DOWN,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}) {
            BlockPos support=dest.offset(direction); BlockState state=c.world.getBlockState(support);
            if (!allowedSupport(state) || !state.isFullCube(c.world,support) || state.hasBlockEntity()) continue;
            Direction face=direction.getOpposite(); Vec3d hit=Vec3d.ofCenter(support).offset(face,0.5);
            if(c.player.getEyePos().squaredDistanceTo(hit)>20.25) continue;
            ActionResult result=c.interactionManager.interactBlock(c.player,Hand.MAIN_HAND,new BlockHitResult(hit,face,support,false));
            cooldown=2;
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
