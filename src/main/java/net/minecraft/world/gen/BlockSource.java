/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.jetbrains.annotations.Nullable;

public interface BlockSource {
    @Nullable
    public BlockState apply(ChunkNoiseSampler var1, int var2, int var3, int var4);
}

