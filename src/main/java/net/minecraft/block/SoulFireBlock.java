/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SoulFireBlock
extends AbstractFireBlock {
    public SoulFireBlock(AbstractBlock.Settings arg) {
        super(arg, 2.0f);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (this.canPlaceAt(state, world, pos)) {
            return this.getDefaultState();
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return SoulFireBlock.isSoulBase(world.getBlockState(pos.down()));
    }

    public static boolean isSoulBase(BlockState state) {
        return state.isIn(BlockTags.SOUL_FIRE_BASE_BLOCKS);
    }

    @Override
    protected boolean isFlammable(BlockState state) {
        return true;
    }
}

