/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InfestedBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class RotatedInfestedBlock
extends InfestedBlock {
    public RotatedInfestedBlock(Block arg, AbstractBlock.Settings arg2) {
        super(arg, arg2);
        this.setDefaultState((BlockState)this.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return PillarBlock.changeRotation(state, rotation);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PillarBlock.AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(PillarBlock.AXIS, ctx.getSide().getAxis());
    }
}

