/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.sapling;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public abstract class SaplingGenerator {
    @Nullable
    protected abstract RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random var1, boolean var2);

    public boolean generate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random) {
        RegistryKey<ConfiguredFeature<?, ?>> lv = this.getTreeFeature(random, this.areFlowersNearby(world, pos));
        if (lv == null) {
            return false;
        }
        RegistryEntry lv2 = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getEntry(lv).orElse(null);
        if (lv2 == null) {
            return false;
        }
        ConfiguredFeature lv3 = (ConfiguredFeature)lv2.value();
        BlockState lv4 = world.getFluidState(pos).getBlockState();
        world.setBlockState(pos, lv4, Block.NO_REDRAW);
        if (lv3.generate(world, chunkGenerator, random, pos)) {
            if (world.getBlockState(pos) == lv4) {
                world.updateListeners(pos, state, lv4, Block.NOTIFY_LISTENERS);
            }
            return true;
        }
        world.setBlockState(pos, state, Block.NO_REDRAW);
        return false;
    }

    private boolean areFlowersNearby(WorldAccess world, BlockPos pos) {
        for (BlockPos lv : BlockPos.Mutable.iterate(pos.down().north(2).west(2), pos.up().south(2).east(2))) {
            if (!world.getBlockState(lv).isIn(BlockTags.FLOWERS)) continue;
            return true;
        }
        return false;
    }
}

