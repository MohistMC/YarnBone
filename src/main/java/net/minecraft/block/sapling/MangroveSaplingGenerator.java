/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.sapling;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import org.jetbrains.annotations.Nullable;

public class MangroveSaplingGenerator
extends SaplingGenerator {
    private final float tallChance;

    public MangroveSaplingGenerator(float tallChance) {
        this.tallChance = tallChance;
    }

    @Override
    @Nullable
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        if (random.nextFloat() < this.tallChance) {
            return TreeConfiguredFeatures.TALL_MANGROVE;
        }
        return TreeConfiguredFeatures.MANGROVE;
    }
}

