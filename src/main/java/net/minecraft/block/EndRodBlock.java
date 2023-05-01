/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RodBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EndRodBlock
extends RodBlock {
    protected EndRodBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv = ctx.getSide();
        BlockState lv2 = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(lv.getOpposite()));
        if (lv2.isOf(this) && lv2.get(FACING) == lv) {
            return (BlockState)this.getDefaultState().with(FACING, lv.getOpposite());
        }
        return (BlockState)this.getDefaultState().with(FACING, lv);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Direction lv = state.get(FACING);
        double d = (double)pos.getX() + 0.55 - (double)(random.nextFloat() * 0.1f);
        double e = (double)pos.getY() + 0.55 - (double)(random.nextFloat() * 0.1f);
        double f = (double)pos.getZ() + 0.55 - (double)(random.nextFloat() * 0.1f);
        double g = 0.4f - (random.nextFloat() + random.nextFloat()) * 0.4f;
        if (random.nextInt(5) == 0) {
            world.addParticle(ParticleTypes.END_ROD, d + (double)lv.getOffsetX() * g, e + (double)lv.getOffsetY() * g, f + (double)lv.getOffsetZ() * g, random.nextGaussian() * 0.005, random.nextGaussian() * 0.005, random.nextGaussian() * 0.005);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.NORMAL;
    }
}

