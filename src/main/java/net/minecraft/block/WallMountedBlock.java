/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class WallMountedBlock
extends HorizontalFacingBlock {
    public static final EnumProperty<WallMountLocation> FACE = Properties.WALL_MOUNT_LOCATION;

    protected WallMountedBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return WallMountedBlock.canPlaceAt(world, pos, WallMountedBlock.getDirection(state).getOpposite());
    }

    public static boolean canPlaceAt(WorldView world, BlockPos pos, Direction direction) {
        BlockPos lv = pos.offset(direction);
        return world.getBlockState(lv).isSideSolidFullSquare(world, lv, direction.getOpposite());
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        for (Direction lv : ctx.getPlacementDirections()) {
            BlockState lv2 = lv.getAxis() == Direction.Axis.Y ? (BlockState)((BlockState)this.getDefaultState().with(FACE, lv == Direction.UP ? WallMountLocation.CEILING : WallMountLocation.FLOOR)).with(FACING, ctx.getHorizontalPlayerFacing()) : (BlockState)((BlockState)this.getDefaultState().with(FACE, WallMountLocation.WALL)).with(FACING, lv.getOpposite());
            if (!lv2.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) continue;
            return lv2;
        }
        return null;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (WallMountedBlock.getDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    protected static Direction getDirection(BlockState state) {
        switch (state.get(FACE)) {
            case CEILING: {
                return Direction.DOWN;
            }
            case FLOOR: {
                return Direction.UP;
            }
        }
        return state.get(FACING);
    }
}

