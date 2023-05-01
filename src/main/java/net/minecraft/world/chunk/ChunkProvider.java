/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;

public interface ChunkProvider {
    @Nullable
    public BlockView getChunk(int var1, int var2);

    default public void onLightUpdate(LightType type, ChunkSectionPos pos) {
    }

    public BlockView getWorld();
}

