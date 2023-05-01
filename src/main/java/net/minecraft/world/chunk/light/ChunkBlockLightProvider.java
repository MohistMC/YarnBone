/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.chunk.light;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.BlockLightStorage;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.apache.commons.lang3.mutable.MutableInt;

public final class ChunkBlockLightProvider
extends ChunkLightProvider<BlockLightStorage.Data, BlockLightStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    public ChunkBlockLightProvider(ChunkProvider chunkProvider) {
        super(chunkProvider, LightType.BLOCK, new BlockLightStorage(chunkProvider));
    }

    private int getLightSourceLuminance(long blockPos) {
        int i = BlockPos.unpackLongX(blockPos);
        int j = BlockPos.unpackLongY(blockPos);
        int k = BlockPos.unpackLongZ(blockPos);
        BlockView lv = this.chunkProvider.getChunk(ChunkSectionPos.getSectionCoord(i), ChunkSectionPos.getSectionCoord(k));
        if (lv != null) {
            return lv.getLuminance(this.mutablePos.set(i, j, k));
        }
        return 0;
    }

    @Override
    protected int getPropagatedLevel(long sourceId, long targetId, int level) {
        VoxelShape lv5;
        int n;
        int k;
        if (targetId == Long.MAX_VALUE) {
            return 15;
        }
        if (sourceId == Long.MAX_VALUE) {
            return level + 15 - this.getLightSourceLuminance(targetId);
        }
        if (level >= 15) {
            return level;
        }
        int j = Integer.signum(BlockPos.unpackLongX(targetId) - BlockPos.unpackLongX(sourceId));
        Direction lv = Direction.fromVector(j, k = Integer.signum(BlockPos.unpackLongY(targetId) - BlockPos.unpackLongY(sourceId)), n = Integer.signum(BlockPos.unpackLongZ(targetId) - BlockPos.unpackLongZ(sourceId)));
        if (lv == null) {
            return 15;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState lv2 = this.getStateForLighting(targetId, mutableInt);
        if (mutableInt.getValue() >= 15) {
            return 15;
        }
        BlockState lv3 = this.getStateForLighting(sourceId, null);
        VoxelShape lv4 = this.getOpaqueShape(lv3, sourceId, lv);
        if (VoxelShapes.unionCoversFullCube(lv4, lv5 = this.getOpaqueShape(lv2, targetId, lv.getOpposite()))) {
            return 15;
        }
        return level + Math.max(1, mutableInt.getValue());
    }

    @Override
    protected void propagateLevel(long id, int level, boolean decrease) {
        long m = ChunkSectionPos.fromBlockPos(id);
        for (Direction lv : DIRECTIONS) {
            long n = BlockPos.offset(id, lv);
            long o = ChunkSectionPos.fromBlockPos(n);
            if (m != o && !((BlockLightStorage)this.lightStorage).hasSection(o)) continue;
            this.propagateLevel(id, n, level, decrease);
        }
    }

    @Override
    protected int recalculateLevel(long id, long excludedId, int maxLevel) {
        int j = maxLevel;
        if (Long.MAX_VALUE != excludedId) {
            int k = this.getPropagatedLevel(Long.MAX_VALUE, id, 0);
            if (j > k) {
                j = k;
            }
            if (j == 0) {
                return j;
            }
        }
        long n = ChunkSectionPos.fromBlockPos(id);
        ChunkNibbleArray lv = ((BlockLightStorage)this.lightStorage).getLightSection(n, true);
        for (Direction lv2 : DIRECTIONS) {
            long p;
            ChunkNibbleArray lv3;
            long o = BlockPos.offset(id, lv2);
            if (o == excludedId || (lv3 = n == (p = ChunkSectionPos.fromBlockPos(o)) ? lv : ((BlockLightStorage)this.lightStorage).getLightSection(p, true)) == null) continue;
            int q = this.getPropagatedLevel(o, id, this.getCurrentLevelFromSection(lv3, o));
            if (j > q) {
                j = q;
            }
            if (j != 0) continue;
            return j;
        }
        return j;
    }

    @Override
    public void addLightSource(BlockPos pos, int level) {
        ((BlockLightStorage)this.lightStorage).updateAll();
        this.updateLevel(Long.MAX_VALUE, pos.asLong(), 15 - level, true);
    }
}

