/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

public class GrassBlock
extends SpreadableBlock
implements Fertilizable {
    public GrassBlock(AbstractBlock.Settings arg) {
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
        BlockPos lv = pos.up();
        BlockState lv2 = Blocks.GRASS.getDefaultState();
        Optional<RegistryEntry.Reference<PlacedFeature>> optional = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE).getEntry(VegetationPlacedFeatures.GRASS_BONEMEAL);
        block0: for (int i = 0; i < 128; ++i) {
            RegistryEntry<PlacedFeature> lv5;
            BlockPos lv3 = lv;
            for (int j = 0; j < i / 16; ++j) {
                if (!world.getBlockState((lv3 = lv3.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1)).down()).isOf(this) || world.getBlockState(lv3).isFullCube(world, lv3)) continue block0;
            }
            BlockState lv4 = world.getBlockState(lv3);
            if (lv4.isOf(lv2.getBlock()) && random.nextInt(10) == 0) {
                ((Fertilizable)((Object)lv2.getBlock())).grow(world, random, lv3, lv4);
            }
            if (!lv4.isAir()) continue;
            if (random.nextInt(8) == 0) {
                List<ConfiguredFeature<?, ?>> list = world.getBiome(lv3).value().getGenerationSettings().getFlowerFeatures();
                if (list.isEmpty()) continue;
                lv5 = ((RandomPatchFeatureConfig)list.get(0).config()).feature();
            } else {
                if (!optional.isPresent()) continue;
                lv5 = (RegistryEntry<PlacedFeature>)optional.get();
            }
            ((PlacedFeature)lv5.value()).generateUnregistered(world, world.getChunkManager().getChunkGenerator(), random, lv3);
        }
    }
}

