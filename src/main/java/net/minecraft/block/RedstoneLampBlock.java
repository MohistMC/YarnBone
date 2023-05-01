/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RedstoneLampBlock
extends Block {
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public RedstoneLampBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)this.getDefaultState().with(LIT, false));
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(LIT, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient) {
            return;
        }
        boolean bl2 = state.get(LIT);
        if (bl2 != world.isReceivingRedstonePower(pos)) {
            if (bl2) {
                world.scheduleBlockTick(pos, this, 4);
            } else {
                world.setBlockState(pos, (BlockState)state.cycle(LIT), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LIT).booleanValue() && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, (BlockState)state.cycle(LIT), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}

