/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.UndergroundConfiguredFeatures;

public class MossBlock
extends Block
implements Fertilizable {
    public MossBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return world.getBlockState(pos.up()).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        world.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE).flatMap(arg -> arg.getEntry(UndergroundConfiguredFeatures.MOSS_PATCH_BONEMEAL)).ifPresent(arg4 -> ((ConfiguredFeature)arg4.value()).generate(world, world.getChunkManager().getChunkGenerator(), random, pos.up()));
    }
}

