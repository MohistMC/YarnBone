/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.biome.source;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public interface BiomeSupplier {
    public RegistryEntry<Biome> getBiome(int var1, int var2, int var3, MultiNoiseUtil.MultiNoiseSampler var4);
}

