/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk.light;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.LightingView;
import org.jetbrains.annotations.Nullable;

public interface ChunkLightingView
extends LightingView {
    @Nullable
    public ChunkNibbleArray getLightSection(ChunkSectionPos var1);

    public int getLightLevel(BlockPos var1);

    public static enum Empty implements ChunkLightingView
    {
        INSTANCE;


        @Override
        @Nullable
        public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
            return null;
        }

        @Override
        public int getLightLevel(BlockPos pos) {
            return 0;
        }

        @Override
        public void checkBlock(BlockPos pos) {
        }

        @Override
        public void addLightSource(BlockPos pos, int level) {
        }

        @Override
        public boolean hasUpdates() {
            return false;
        }

        @Override
        public int doLightUpdates(int i, boolean doSkylight, boolean skipEdgeLightPropagation) {
            return i;
        }

        @Override
        public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
        }

        @Override
        public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        }
    }
}

