/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.chunk.light;

import java.util.Locale;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.SkyLightStorage;
import org.apache.commons.lang3.mutable.MutableInt;

public final class ChunkSkyLightProvider
extends ChunkLightProvider<SkyLightStorage.Data, SkyLightStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
        super(chunkProvider, LightType.SKY, new SkyLightStorage(chunkProvider));
    }

    @Override
    protected int getPropagatedLevel(long sourceId, long targetId, int level) {
        boolean bl2;
        VoxelShape lv5;
        int t;
        int s;
        if (targetId == Long.MAX_VALUE || sourceId == Long.MAX_VALUE) {
            return 15;
        }
        if (level >= 15) {
            return level;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState lv = this.getStateForLighting(targetId, mutableInt);
        if (mutableInt.getValue() >= 15) {
            return 15;
        }
        int j = BlockPos.unpackLongX(sourceId);
        int k = BlockPos.unpackLongY(sourceId);
        int n = BlockPos.unpackLongZ(sourceId);
        int o = BlockPos.unpackLongX(targetId);
        int p = BlockPos.unpackLongY(targetId);
        int q = BlockPos.unpackLongZ(targetId);
        int r = Integer.signum(o - j);
        Direction lv2 = Direction.fromVector(r, s = Integer.signum(p - k), t = Integer.signum(q - n));
        if (lv2 == null) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Light was spread in illegal direction %d, %d, %d", r, s, t));
        }
        BlockState lv3 = this.getStateForLighting(sourceId, null);
        VoxelShape lv4 = this.getOpaqueShape(lv3, sourceId, lv2);
        if (VoxelShapes.unionCoversFullCube(lv4, lv5 = this.getOpaqueShape(lv, targetId, lv2.getOpposite()))) {
            return 15;
        }
        boolean bl = j == o && n == q;
        boolean bl3 = bl2 = bl && k > p;
        if (bl2 && level == 0 && mutableInt.getValue() == 0) {
            return 0;
        }
        return level + Math.max(1, mutableInt.getValue());
    }

    @Override
    protected void propagateLevel(long id, int level, boolean decrease) {
        long s;
        long t;
        int o;
        long m = ChunkSectionPos.fromBlockPos(id);
        int j = BlockPos.unpackLongY(id);
        int k = ChunkSectionPos.getLocalCoord(j);
        int n = ChunkSectionPos.getSectionCoord(j);
        if (k != 0) {
            o = 0;
        } else {
            int p = 0;
            while (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.offset(m, 0, -p - 1, 0)) && ((SkyLightStorage)this.lightStorage).isAboveMinHeight(n - p - 1)) {
                ++p;
            }
            o = p;
        }
        long q = BlockPos.add(id, 0, -1 - o * 16, 0);
        long r = ChunkSectionPos.fromBlockPos(q);
        if (m == r || ((SkyLightStorage)this.lightStorage).hasSection(r)) {
            this.propagateLevel(id, q, level, decrease);
        }
        if (m == (t = ChunkSectionPos.fromBlockPos(s = BlockPos.offset(id, Direction.UP))) || ((SkyLightStorage)this.lightStorage).hasSection(t)) {
            this.propagateLevel(id, s, level, decrease);
        }
        block1: for (Direction lv : HORIZONTAL_DIRECTIONS) {
            int u = 0;
            do {
                long v;
                long w;
                if (m == (w = ChunkSectionPos.fromBlockPos(v = BlockPos.add(id, lv.getOffsetX(), -u, lv.getOffsetZ())))) {
                    this.propagateLevel(id, v, level, decrease);
                    continue block1;
                }
                if (!((SkyLightStorage)this.lightStorage).hasSection(w)) continue;
                long x = BlockPos.add(id, 0, -u, 0);
                this.propagateLevel(x, v, level, decrease);
            } while (++u <= o * 16);
        }
    }

    @Override
    protected int recalculateLevel(long id, long excludedId, int maxLevel) {
        int j = maxLevel;
        long n = ChunkSectionPos.fromBlockPos(id);
        ChunkNibbleArray lv = ((SkyLightStorage)this.lightStorage).getLightSection(n, true);
        for (Direction lv2 : DIRECTIONS) {
            int k;
            long o = BlockPos.offset(id, lv2);
            if (o == excludedId) continue;
            long p = ChunkSectionPos.fromBlockPos(o);
            ChunkNibbleArray lv3 = n == p ? lv : ((SkyLightStorage)this.lightStorage).getLightSection(p, true);
            if (lv3 != null) {
                k = this.getCurrentLevelFromSection(lv3, o);
            } else {
                if (lv2 == Direction.DOWN) continue;
                k = 15 - ((SkyLightStorage)this.lightStorage).getLight(o, true);
            }
            int q = this.getPropagatedLevel(o, id, k);
            if (j > q) {
                j = q;
            }
            if (j != 0) continue;
            return j;
        }
        return j;
    }

    @Override
    protected void resetLevel(long id) {
        ((SkyLightStorage)this.lightStorage).updateAll();
        long m = ChunkSectionPos.fromBlockPos(id);
        if (((SkyLightStorage)this.lightStorage).hasSection(m)) {
            super.resetLevel(id);
        } else {
            id = BlockPos.removeChunkSectionLocalY(id);
            while (!((SkyLightStorage)this.lightStorage).hasSection(m) && !((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(m)) {
                m = ChunkSectionPos.offset(m, Direction.UP);
                id = BlockPos.add(id, 0, 16, 0);
            }
            if (((SkyLightStorage)this.lightStorage).hasSection(m)) {
                super.resetLevel(id);
            }
        }
    }

    @Override
    public String displaySectionLevel(long sectionPos) {
        return super.displaySectionLevel(sectionPos) + (((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(sectionPos) ? "*" : "");
    }
}

