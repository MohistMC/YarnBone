/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk.light;

import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LevelPropagator;
import net.minecraft.world.chunk.light.LightStorage;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkLightProvider<M extends ChunkToNibbleArrayMap<M>, S extends LightStorage<M>>
extends LevelPropagator
implements ChunkLightingView {
    public static final long field_31708 = Long.MAX_VALUE;
    private static final Direction[] DIRECTIONS = Direction.values();
    protected final ChunkProvider chunkProvider;
    protected final LightType type;
    protected final S lightStorage;
    private boolean field_15794;
    protected final BlockPos.Mutable reusableBlockPos = new BlockPos.Mutable();
    private static final int field_31709 = 2;
    private final long[] cachedChunkPositions = new long[2];
    private final BlockView[] cachedChunks = new BlockView[2];

    public ChunkLightProvider(ChunkProvider chunkProvider, LightType type, S lightStorage) {
        super(16, 256, 8192);
        this.chunkProvider = chunkProvider;
        this.type = type;
        this.lightStorage = lightStorage;
        this.clearChunkCache();
    }

    @Override
    protected void resetLevel(long id) {
        ((LightStorage)this.lightStorage).updateAll();
        if (((LightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(id))) {
            super.resetLevel(id);
        }
    }

    @Nullable
    private BlockView getChunk(int chunkX, int chunkZ) {
        long l = ChunkPos.toLong(chunkX, chunkZ);
        for (int k = 0; k < 2; ++k) {
            if (l != this.cachedChunkPositions[k]) continue;
            return this.cachedChunks[k];
        }
        BlockView lv = this.chunkProvider.getChunk(chunkX, chunkZ);
        for (int m = 1; m > 0; --m) {
            this.cachedChunkPositions[m] = this.cachedChunkPositions[m - 1];
            this.cachedChunks[m] = this.cachedChunks[m - 1];
        }
        this.cachedChunkPositions[0] = l;
        this.cachedChunks[0] = lv;
        return lv;
    }

    private void clearChunkCache() {
        Arrays.fill(this.cachedChunkPositions, ChunkPos.MARKER);
        Arrays.fill(this.cachedChunks, null);
    }

    protected BlockState getStateForLighting(long pos, @Nullable MutableInt opacity) {
        boolean bl;
        int j;
        if (pos == Long.MAX_VALUE) {
            if (opacity != null) {
                opacity.setValue(0);
            }
            return Blocks.AIR.getDefaultState();
        }
        int i = ChunkSectionPos.getSectionCoord(BlockPos.unpackLongX(pos));
        BlockView lv = this.getChunk(i, j = ChunkSectionPos.getSectionCoord(BlockPos.unpackLongZ(pos)));
        if (lv == null) {
            if (opacity != null) {
                opacity.setValue(16);
            }
            return Blocks.BEDROCK.getDefaultState();
        }
        this.reusableBlockPos.set(pos);
        BlockState lv2 = lv.getBlockState(this.reusableBlockPos);
        boolean bl2 = bl = lv2.isOpaque() && lv2.hasSidedTransparency();
        if (opacity != null) {
            opacity.setValue(lv2.getOpacity(this.chunkProvider.getWorld(), this.reusableBlockPos));
        }
        return bl ? lv2 : Blocks.AIR.getDefaultState();
    }

    protected VoxelShape getOpaqueShape(BlockState world, long pos, Direction facing) {
        return world.isOpaque() ? world.getCullingFace(this.chunkProvider.getWorld(), this.reusableBlockPos.set(pos), facing) : VoxelShapes.empty();
    }

    public static int getRealisticOpacity(BlockView world, BlockState state1, BlockPos pos1, BlockState state2, BlockPos pos2, Direction direction, int opacity2) {
        VoxelShape lv2;
        boolean bl2;
        boolean bl = state1.isOpaque() && state1.hasSidedTransparency();
        boolean bl3 = bl2 = state2.isOpaque() && state2.hasSidedTransparency();
        if (!bl && !bl2) {
            return opacity2;
        }
        VoxelShape lv = bl ? state1.getCullingShape(world, pos1) : VoxelShapes.empty();
        VoxelShape voxelShape = lv2 = bl2 ? state2.getCullingShape(world, pos2) : VoxelShapes.empty();
        if (VoxelShapes.adjacentSidesCoverSquare(lv, lv2, direction)) {
            return 16;
        }
        return opacity2;
    }

    @Override
    protected boolean isMarker(long id) {
        return id == Long.MAX_VALUE;
    }

    @Override
    protected int recalculateLevel(long id, long excludedId, int maxLevel) {
        return 0;
    }

    @Override
    protected int getLevel(long id) {
        if (id == Long.MAX_VALUE) {
            return 0;
        }
        return 15 - ((LightStorage)this.lightStorage).get(id);
    }

    protected int getCurrentLevelFromSection(ChunkNibbleArray section, long blockPos) {
        return 15 - section.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
    }

    @Override
    protected void setLevel(long id, int level) {
        ((LightStorage)this.lightStorage).set(id, Math.min(15, 15 - level));
    }

    @Override
    protected int getPropagatedLevel(long sourceId, long targetId, int level) {
        return 0;
    }

    @Override
    public boolean hasUpdates() {
        return this.hasPendingUpdates() || ((LevelPropagator)this.lightStorage).hasPendingUpdates() || ((LightStorage)this.lightStorage).hasLightUpdates();
    }

    @Override
    public int doLightUpdates(int i, boolean doSkylight, boolean skipEdgeLightPropagation) {
        if (!this.field_15794) {
            if (((LevelPropagator)this.lightStorage).hasPendingUpdates() && (i = ((LevelPropagator)this.lightStorage).applyPendingUpdates(i)) == 0) {
                return i;
            }
            ((LightStorage)this.lightStorage).updateLight(this, doSkylight, skipEdgeLightPropagation);
        }
        this.field_15794 = true;
        if (this.hasPendingUpdates()) {
            i = this.applyPendingUpdates(i);
            this.clearChunkCache();
            if (i == 0) {
                return i;
            }
        }
        this.field_15794 = false;
        ((LightStorage)this.lightStorage).notifyChanges();
        return i;
    }

    protected void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray lightArray, boolean nonEdge) {
        ((LightStorage)this.lightStorage).enqueueSectionData(sectionPos, lightArray, nonEdge);
    }

    @Override
    @Nullable
    public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
        return ((LightStorage)this.lightStorage).getLightSection(pos.asLong());
    }

    @Override
    public int getLightLevel(BlockPos pos) {
        return ((LightStorage)this.lightStorage).getLight(pos.asLong());
    }

    public String displaySectionLevel(long sectionPos) {
        return "" + ((LightStorage)this.lightStorage).getLevel(sectionPos);
    }

    @Override
    public void checkBlock(BlockPos pos) {
        long l = pos.asLong();
        this.resetLevel(l);
        for (Direction lv : DIRECTIONS) {
            this.resetLevel(BlockPos.offset(l, lv));
        }
    }

    @Override
    public void addLightSource(BlockPos pos, int level) {
    }

    @Override
    public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
        ((LightStorage)this.lightStorage).setSectionStatus(pos.asLong(), notReady);
    }

    @Override
    public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        long l = ChunkSectionPos.withZeroY(ChunkSectionPos.asLong(pos.x, 0, pos.z));
        ((LightStorage)this.lightStorage).setColumnEnabled(l, retainData);
    }

    public void setRetainColumn(ChunkPos pos, boolean retainData) {
        long l = ChunkSectionPos.withZeroY(ChunkSectionPos.asLong(pos.x, 0, pos.z));
        ((LightStorage)this.lightStorage).setRetainColumn(l, retainData);
    }
}

