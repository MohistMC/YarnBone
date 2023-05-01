/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CherryLeavesBlock
extends LeavesBlock {
    public CherryLeavesBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (random.nextInt(15) != 0) {
            return;
        }
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (lv2.isOpaque() && lv2.isSideSolidFullSquare(world, lv, Direction.UP)) {
            return;
        }
        ParticleUtil.spawnParticle(world, pos, random, ParticleTypes.DRIPPING_CHERRY_LEAVES);
    }
}

