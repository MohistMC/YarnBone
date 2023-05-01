/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ChorusPlantBlock
extends ConnectingBlock {
    protected ChorusPlantBlock(AbstractBlock.Settings arg) {
        super(0.3125f, arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(UP, false)).with(DOWN, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.withConnectionProperties(ctx.getWorld(), ctx.getBlockPos());
    }

    public BlockState withConnectionProperties(BlockView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos.down());
        BlockState lv2 = world.getBlockState(pos.up());
        BlockState lv3 = world.getBlockState(pos.north());
        BlockState lv4 = world.getBlockState(pos.east());
        BlockState lv5 = world.getBlockState(pos.south());
        BlockState lv6 = world.getBlockState(pos.west());
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(DOWN, lv.isOf(this) || lv.isOf(Blocks.CHORUS_FLOWER) || lv.isOf(Blocks.END_STONE))).with(UP, lv2.isOf(this) || lv2.isOf(Blocks.CHORUS_FLOWER))).with(NORTH, lv3.isOf(this) || lv3.isOf(Blocks.CHORUS_FLOWER))).with(EAST, lv4.isOf(this) || lv4.isOf(Blocks.CHORUS_FLOWER))).with(SOUTH, lv5.isOf(this) || lv5.isOf(Blocks.CHORUS_FLOWER))).with(WEST, lv6.isOf(this) || lv6.isOf(Blocks.CHORUS_FLOWER));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
        boolean bl = neighborState.isOf(this) || neighborState.isOf(Blocks.CHORUS_FLOWER) || direction == Direction.DOWN && neighborState.isOf(Blocks.END_STONE);
        return (BlockState)state.with((Property)FACING_PROPERTIES.get(direction), bl);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos.down());
        boolean bl = !world.getBlockState(pos.up()).isAir() && !lv.isAir();
        for (Direction lv2 : Direction.Type.HORIZONTAL) {
            BlockPos lv3 = pos.offset(lv2);
            BlockState lv4 = world.getBlockState(lv3);
            if (!lv4.isOf(this)) continue;
            if (bl) {
                return false;
            }
            BlockState lv5 = world.getBlockState(lv3.down());
            if (!lv5.isOf(this) && !lv5.isOf(Blocks.END_STONE)) continue;
            return true;
        }
        return lv.isOf(this) || lv.isOf(Blocks.END_STONE);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}

