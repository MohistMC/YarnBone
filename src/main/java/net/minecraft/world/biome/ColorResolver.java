/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.biome;

import net.minecraft.world.biome.Biome;

@FunctionalInterface
public interface ColorResolver {
    public int getColor(Biome var1, double var2, double var4);
}

