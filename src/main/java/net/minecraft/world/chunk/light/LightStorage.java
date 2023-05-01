/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.SectionDistanceLevelPropagator;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.jetbrains.annotations.Nullable;

public abstract class LightStorage<M extends ChunkToNibbleArrayMap<M>>
extends SectionDistanceLevelPropagator {
    protected static final int field_31710 = 0;
    protected static final int field_31711 = 1;
    protected static final int field_31712 = 2;
    protected static final ChunkNibbleArray EMPTY = new ChunkNibbleArray();
    private static final Direction[] DIRECTIONS = Direction.values();
    private final LightType lightType;
    private final ChunkProvider chunkProvider;
    protected final LongSet readySections = new LongOpenHashSet();
    protected final LongSet markedNotReadySections = new LongOpenHashSet();
    protected final LongSet markedReadySections = new LongOpenHashSet();
    protected volatile M uncachedStorage;
    protected final M storage;
    protected final LongSet dirtySections = new LongOpenHashSet();
    protected final LongSet notifySections = new LongOpenHashSet();
    protected final Long2ObjectMap<ChunkNibbleArray> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
    private final LongSet queuedEdgeSections = new LongOpenHashSet();
    private final LongSet columnsToRetain = new LongOpenHashSet();
    private final LongSet sectionsToRemove = new LongOpenHashSet();
    protected volatile boolean hasLightUpdates;

    protected LightStorage(LightType lightType, ChunkProvider chunkProvider, M lightData) {
        super(3, 16, 256);
        this.lightType = lightType;
        this.chunkProvider = chunkProvider;
        this.storage = lightData;
        this.uncachedStorage = ((ChunkToNibbleArrayMap)lightData).copy();
        ((ChunkToNibbleArrayMap)this.uncachedStorage).disableCache();
    }

    protected boolean hasSection(long sectionPos) {
        return this.getLightSection(sectionPos, true) != null;
    }

    @Nullable
    protected ChunkNibbleArray getLightSection(long sectionPos, boolean cached) {
        return this.getLightSection(cached ? this.storage : this.uncachedStorage, sectionPos);
    }

    @Nullable
    protected ChunkNibbleArray getLightSection(M storage, long sectionPos) {
        return ((ChunkToNibbleArrayMap)storage).get(sectionPos);
    }

    @Nullable
    public ChunkNibbleArray getLightSection(long sectionPos) {
        ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
        if (lv != null) {
            return lv;
        }
        return this.getLightSection(sectionPos, false);
    }

    protected abstract int getLight(long var1);

    protected int get(long blockPos) {
        long m = ChunkSectionPos.fromBlockPos(blockPos);
        ChunkNibbleArray lv = this.getLightSection(m, true);
        return lv.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
    }

    protected void set(long blockPos, int value) {
        long m = ChunkSectionPos.fromBlockPos(blockPos);
        if (this.dirtySections.add(m)) {
            ((ChunkToNibbleArrayMap)this.storage).replaceWithCopy(m);
        }
        ChunkNibbleArray lv = this.getLightSection(m, true);
        lv.set(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)), value);
        ChunkSectionPos.forEachChunkSectionAround(blockPos, this.notifySections::add);
    }

    @Override
    protected int getLevel(long id) {
        if (id == Long.MAX_VALUE) {
            return 2;
        }
        if (this.readySections.contains(id)) {
            return 0;
        }
        if (!this.sectionsToRemove.contains(id) && ((ChunkToNibbleArrayMap)this.storage).containsKey(id)) {
            return 1;
        }
        return 2;
    }

    @Override
    protected int getInitialLevel(long id) {
        if (this.markedNotReadySections.contains(id)) {
            return 2;
        }
        if (this.readySections.contains(id) || this.markedReadySections.contains(id)) {
            return 0;
        }
        return 2;
    }

    @Override
    protected void setLevel(long id, int level) {
        int j = this.getLevel(id);
        if (j != 0 && level == 0) {
            this.readySections.add(id);
            this.markedReadySections.remove(id);
        }
        if (j == 0 && level != 0) {
            this.readySections.remove(id);
            this.markedNotReadySections.remove(id);
        }
        if (j >= 2 && level != 2) {
            if (this.sectionsToRemove.contains(id)) {
                this.sectionsToRemove.remove(id);
            } else {
                ((ChunkToNibbleArrayMap)this.storage).put(id, this.createSection(id));
                this.dirtySections.add(id);
                this.onLoadSection(id);
                int k = ChunkSectionPos.unpackX(id);
                int m = ChunkSectionPos.unpackY(id);
                int n = ChunkSectionPos.unpackZ(id);
                for (int o = -1; o <= 1; ++o) {
                    for (int p = -1; p <= 1; ++p) {
                        for (int q = -1; q <= 1; ++q) {
                            this.notifySections.add(ChunkSectionPos.asLong(k + p, m + q, n + o));
                        }
                    }
                }
            }
        }
        if (j != 2 && level >= 2) {
            this.sectionsToRemove.add(id);
        }
        this.hasLightUpdates = !this.sectionsToRemove.isEmpty();
    }

    protected ChunkNibbleArray createSection(long sectionPos) {
        ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
        if (lv != null) {
            return lv;
        }
        return new ChunkNibbleArray();
    }

    protected void removeSection(ChunkLightProvider<?, ?> storage, long sectionPos) {
        if (storage.getPendingUpdateCount() == 0) {
            return;
        }
        if (storage.getPendingUpdateCount() < 8192) {
            storage.removePendingUpdateIf(m -> ChunkSectionPos.fromBlockPos(m) == sectionPos);
            return;
        }
        int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(sectionPos));
        int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(sectionPos));
        int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(sectionPos));
        for (int m2 = 0; m2 < 16; ++m2) {
            for (int n = 0; n < 16; ++n) {
                for (int o = 0; o < 16; ++o) {
                    long p = BlockPos.asLong(i + m2, j + n, k + o);
                    storage.removePendingUpdate(p);
                }
            }
        }
    }

    protected boolean hasLightUpdates() {
        return this.hasLightUpdates;
    }

    protected void updateLight(ChunkLightProvider<M, ?> lightProvider, boolean doSkylight, boolean skipEdgeLightPropagation) {
        long m;
        ChunkNibbleArray lv2;
        long l;
        if (!this.hasLightUpdates() && this.queuedSections.isEmpty()) {
            return;
        }
        Iterator<Long> iterator = this.sectionsToRemove.iterator();
        while (iterator.hasNext()) {
            l = (Long)iterator.next();
            this.removeSection(lightProvider, l);
            ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.remove(l);
            lv2 = ((ChunkToNibbleArrayMap)this.storage).removeChunk(l);
            if (!this.columnsToRetain.contains(ChunkSectionPos.withZeroY(l))) continue;
            if (lv != null) {
                this.queuedSections.put(l, lv);
                continue;
            }
            if (lv2 == null) continue;
            this.queuedSections.put(l, lv2);
        }
        ((ChunkToNibbleArrayMap)this.storage).clearCache();
        iterator = this.sectionsToRemove.iterator();
        while (iterator.hasNext()) {
            l = (Long)iterator.next();
            this.onUnloadSection(l);
        }
        this.sectionsToRemove.clear();
        this.hasLightUpdates = false;
        for (Long2ObjectMap.Entry entry : this.queuedSections.long2ObjectEntrySet()) {
            m = entry.getLongKey();
            if (!this.hasSection(m)) continue;
            lv2 = (ChunkNibbleArray)entry.getValue();
            if (((ChunkToNibbleArrayMap)this.storage).get(m) == lv2) continue;
            this.removeSection(lightProvider, m);
            ((ChunkToNibbleArrayMap)this.storage).put(m, lv2);
            this.dirtySections.add(m);
        }
        ((ChunkToNibbleArrayMap)this.storage).clearCache();
        if (!skipEdgeLightPropagation) {
            for (long l2 : this.queuedSections.keySet()) {
                this.updateSection(lightProvider, l2);
            }
        } else {
            for (long l3 : this.queuedEdgeSections) {
                this.updateSection(lightProvider, l3);
            }
        }
        this.queuedEdgeSections.clear();
        Iterator objectIterator = this.queuedSections.long2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            m = entry.getLongKey();
            if (!this.hasSection(m)) continue;
            objectIterator.remove();
        }
    }

    private void updateSection(ChunkLightProvider<M, ?> lightProvider, long sectionPos) {
        if (!this.hasSection(sectionPos)) {
            return;
        }
        int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(sectionPos));
        int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(sectionPos));
        int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(sectionPos));
        for (Direction lv : DIRECTIONS) {
            long m = ChunkSectionPos.offset(sectionPos, lv);
            if (this.queuedSections.containsKey(m) || !this.hasSection(m)) continue;
            for (int n = 0; n < 16; ++n) {
                for (int o = 0; o < 16; ++o) {
                    long p;
                    long q = switch (lv) {
                        case Direction.DOWN -> {
                            p = BlockPos.asLong(i + o, j, k + n);
                            yield BlockPos.asLong(i + o, j - 1, k + n);
                        }
                        case Direction.UP -> {
                            p = BlockPos.asLong(i + o, j + 16 - 1, k + n);
                            yield BlockPos.asLong(i + o, j + 16, k + n);
                        }
                        case Direction.NORTH -> {
                            p = BlockPos.asLong(i + n, j + o, k);
                            yield BlockPos.asLong(i + n, j + o, k - 1);
                        }
                        case Direction.SOUTH -> {
                            p = BlockPos.asLong(i + n, j + o, k + 16 - 1);
                            yield BlockPos.asLong(i + n, j + o, k + 16);
                        }
                        case Direction.WEST -> {
                            p = BlockPos.asLong(i, j + n, k + o);
                            yield BlockPos.asLong(i - 1, j + n, k + o);
                        }
                        default -> {
                            p = BlockPos.asLong(i + 16 - 1, j + n, k + o);
                            yield BlockPos.asLong(i + 16, j + n, k + o);
                        }
                    };
                    lightProvider.updateLevel(p, q, lightProvider.getPropagatedLevel(p, q, lightProvider.getLevel(p)), false);
                    lightProvider.updateLevel(q, p, lightProvider.getPropagatedLevel(q, p, lightProvider.getLevel(q)), false);
                }
            }
        }
    }

    protected void onLoadSection(long sectionPos) {
    }

    protected void onUnloadSection(long sectionPos) {
    }

    protected void setColumnEnabled(long columnPos, boolean enabled) {
    }

    public void setRetainColumn(long sectionPos, boolean retain) {
        if (retain) {
            this.columnsToRetain.add(sectionPos);
        } else {
            this.columnsToRetain.remove(sectionPos);
        }
    }

    protected void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray array, boolean nonEdge) {
        if (array != null) {
            this.queuedSections.put(sectionPos, array);
            if (!nonEdge) {
                this.queuedEdgeSections.add(sectionPos);
            }
        } else {
            this.queuedSections.remove(sectionPos);
        }
    }

    protected void setSectionStatus(long sectionPos, boolean notReady) {
        boolean bl2 = this.readySections.contains(sectionPos);
        if (!bl2 && !notReady) {
            this.markedReadySections.add(sectionPos);
            this.updateLevel(Long.MAX_VALUE, sectionPos, 0, true);
        }
        if (bl2 && notReady) {
            this.markedNotReadySections.add(sectionPos);
            this.updateLevel(Long.MAX_VALUE, sectionPos, 2, false);
        }
    }

    protected void updateAll() {
        if (this.hasPendingUpdates()) {
            this.applyPendingUpdates(Integer.MAX_VALUE);
        }
    }

    protected void notifyChanges() {
        if (!this.dirtySections.isEmpty()) {
            Object lv = ((ChunkToNibbleArrayMap)this.storage).copy();
            ((ChunkToNibbleArrayMap)lv).disableCache();
            this.uncachedStorage = lv;
            this.dirtySections.clear();
        }
        if (!this.notifySections.isEmpty()) {
            LongIterator longIterator = this.notifySections.iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                this.chunkProvider.onLightUpdate(this.lightType, ChunkSectionPos.from(l));
            }
            this.notifySections.clear();
        }
    }
}

