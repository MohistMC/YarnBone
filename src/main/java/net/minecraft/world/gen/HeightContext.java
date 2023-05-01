/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class HeightContext {
    private final int minY;
    private final int height;

    public HeightContext(ChunkGenerator generator, HeightLimitView world) {
        this.minY = Math.max(world.getBottomY(), generator.getMinimumY());
        this.height = Math.min(world.getHeight(), generator.getWorldHeight());
    }

    public int getMinY() {
        return this.minY;
    }

    public int getHeight() {
        return this.height;
    }
}

