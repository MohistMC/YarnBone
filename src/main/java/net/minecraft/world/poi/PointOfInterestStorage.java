/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.SectionDistanceLevelPropagator;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.storage.SerializingRegionBasedStorage;

public class PointOfInterestStorage
extends SerializingRegionBasedStorage<PointOfInterestSet> {
    public static final int field_30265 = 6;
    public static final int field_30266 = 1;
    private final PointOfInterestDistanceTracker pointOfInterestDistanceTracker;
    private final LongSet preloadedChunks = new LongOpenHashSet();

    public PointOfInterestStorage(Path path, DataFixer dataFixer, boolean dsync, DynamicRegistryManager registryManager, HeightLimitView world) {
        super(path, PointOfInterestSet::createCodec, PointOfInterestSet::new, dataFixer, DataFixTypes.POI_CHUNK, dsync, registryManager, world);
        this.pointOfInterestDistanceTracker = new PointOfInterestDistanceTracker();
    }

    public void add(BlockPos pos, RegistryEntry<PointOfInterestType> type) {
        ((PointOfInterestSet)this.getOrCreate(ChunkSectionPos.toLong(pos))).add(pos, type);
    }

    public void remove(BlockPos pos) {
        this.get(ChunkSectionPos.toLong(pos)).ifPresent(poiSet -> poiSet.remove(pos));
    }

    public long count(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).count();
    }

    public boolean hasTypeAt(RegistryKey<PointOfInterestType> type, BlockPos pos) {
        return this.test(pos, entry -> entry.matchesKey(type));
    }

    public Stream<PointOfInterest> getInSquare(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        int j = Math.floorDiv(radius, 16) + 1;
        return ChunkPos.stream(new ChunkPos(pos), j).flatMap(chunkPos -> this.getInChunk(typePredicate, (ChunkPos)chunkPos, occupationStatus)).filter(poi -> {
            BlockPos lv = poi.getPos();
            return Math.abs(lv.getX() - pos.getX()) <= radius && Math.abs(lv.getZ() - pos.getZ()) <= radius;
        });
    }

    public Stream<PointOfInterest> getInCircle(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        int j = radius * radius;
        return this.getInSquare(typePredicate, pos, radius, occupationStatus).filter(poi -> poi.getPos().getSquaredDistance(pos) <= (double)j);
    }

    @Debug
    public Stream<PointOfInterest> getInChunk(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, ChunkPos chunkPos, OccupationStatus occupationStatus) {
        return IntStream.range(this.world.getBottomSectionCoord(), this.world.getTopSectionCoord()).boxed().map(integer -> this.get(ChunkSectionPos.from(chunkPos, integer).asLong())).filter(Optional::isPresent).flatMap(optional -> ((PointOfInterestSet)optional.get()).get(typePredicate, occupationStatus));
    }

    public Stream<BlockPos> getPositions(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).map(PointOfInterest::getPos).filter(posPredicate);
    }

    public Stream<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getTypesAndPositions(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).filter(poi -> posPredicate.test(poi.getPos())).map(poi -> Pair.of(poi.getType(), poi.getPos()));
    }

    public Stream<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getSortedTypesAndPositions(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getTypesAndPositions(typePredicate, posPredicate, pos, radius, occupationStatus).sorted(Comparator.comparingDouble(pair -> ((BlockPos)pair.getSecond()).getSquaredDistance(pos)));
    }

    public Optional<BlockPos> getPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getPositions(typePredicate, posPredicate, pos, radius, occupationStatus).findFirst();
    }

    public Optional<BlockPos> getNearestPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).map(PointOfInterest::getPos).min(Comparator.comparingDouble(arg2 -> arg2.getSquaredDistance(pos)));
    }

    public Optional<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getNearestTypeAndPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).min(Comparator.comparingDouble(poi -> poi.getPos().getSquaredDistance(pos))).map(poi -> Pair.of(poi.getType(), poi.getPos()));
    }

    public Optional<BlockPos> getNearestPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).map(PointOfInterest::getPos).filter(posPredicate).min(Comparator.comparingDouble(arg2 -> arg2.getSquaredDistance(pos)));
    }

    public Optional<BlockPos> getPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BiPredicate<RegistryEntry<PointOfInterestType>, BlockPos> biPredicate, BlockPos pos, int radius) {
        return this.getInCircle(typePredicate, pos, radius, OccupationStatus.HAS_SPACE).filter(poi -> biPredicate.test(poi.getType(), poi.getPos())).findFirst().map(poi -> {
            poi.reserveTicket();
            return poi.getPos();
        });
    }

    public Optional<BlockPos> getPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> positionPredicate, OccupationStatus occupationStatus, BlockPos pos, int radius, Random random) {
        List<PointOfInterest> list = Util.copyShuffled(this.getInCircle(typePredicate, pos, radius, occupationStatus), random);
        return list.stream().filter(poi -> positionPredicate.test(poi.getPos())).findFirst().map(PointOfInterest::getPos);
    }

    public boolean releaseTicket(BlockPos pos) {
        return this.get(ChunkSectionPos.toLong(pos)).map(poiSet -> poiSet.releaseTicket(pos)).orElseThrow(() -> Util.throwOrPause(new IllegalStateException("POI never registered at " + pos)));
    }

    public boolean test(BlockPos pos, Predicate<RegistryEntry<PointOfInterestType>> predicate) {
        return this.get(ChunkSectionPos.toLong(pos)).map(poiSet -> poiSet.test(pos, predicate)).orElse(false);
    }

    public Optional<RegistryEntry<PointOfInterestType>> getType(BlockPos pos) {
        return this.get(ChunkSectionPos.toLong(pos)).flatMap(poiSet -> poiSet.getType(pos));
    }

    @Deprecated
    @Debug
    public int getFreeTickets(BlockPos pos) {
        return this.get(ChunkSectionPos.toLong(pos)).map(poiSet -> poiSet.getFreeTickets(pos)).orElse(0);
    }

    public int getDistanceFromNearestOccupied(ChunkSectionPos pos) {
        this.pointOfInterestDistanceTracker.update();
        return this.pointOfInterestDistanceTracker.getLevel(pos.asLong());
    }

    boolean isOccupied(long pos) {
        Optional optional = this.getIfLoaded(pos);
        if (optional == null) {
            return false;
        }
        return optional.map(poiSet -> poiSet.get(entry -> entry.isIn(PointOfInterestTypeTags.VILLAGE), OccupationStatus.IS_OCCUPIED).findAny().isPresent()).orElse(false);
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        super.tick(shouldKeepTicking);
        this.pointOfInterestDistanceTracker.update();
    }

    @Override
    protected void onUpdate(long pos) {
        super.onUpdate(pos);
        this.pointOfInterestDistanceTracker.update(pos, this.pointOfInterestDistanceTracker.getInitialLevel(pos), false);
    }

    @Override
    protected void onLoad(long pos) {
        this.pointOfInterestDistanceTracker.update(pos, this.pointOfInterestDistanceTracker.getInitialLevel(pos), false);
    }

    public void initForPalette(ChunkPos chunkPos, ChunkSection chunkSection) {
        ChunkSectionPos lv = ChunkSectionPos.from(chunkPos, ChunkSectionPos.getSectionCoord(chunkSection.getYOffset()));
        Util.ifPresentOrElse(this.get(lv.asLong()), poiSet -> poiSet.updatePointsOfInterest(populator -> {
            if (PointOfInterestStorage.shouldScan(chunkSection)) {
                this.scanAndPopulate(chunkSection, lv, (BiConsumer<BlockPos, RegistryEntry<PointOfInterestType>>)populator);
            }
        }), () -> {
            if (PointOfInterestStorage.shouldScan(chunkSection)) {
                PointOfInterestSet lv = (PointOfInterestSet)this.getOrCreate(lv.asLong());
                this.scanAndPopulate(chunkSection, lv, lv::add);
            }
        });
    }

    private static boolean shouldScan(ChunkSection chunkSection) {
        return chunkSection.hasAny(PointOfInterestTypes::isPointOfInterest);
    }

    private void scanAndPopulate(ChunkSection chunkSection, ChunkSectionPos sectionPos, BiConsumer<BlockPos, RegistryEntry<PointOfInterestType>> populator) {
        sectionPos.streamBlocks().forEach(pos -> {
            BlockState lv = chunkSection.getBlockState(ChunkSectionPos.getLocalCoord(pos.getX()), ChunkSectionPos.getLocalCoord(pos.getY()), ChunkSectionPos.getLocalCoord(pos.getZ()));
            PointOfInterestTypes.getTypeForState(lv).ifPresent(poiType -> populator.accept((BlockPos)pos, (RegistryEntry<PointOfInterestType>)poiType));
        });
    }

    public void preloadChunks(WorldView world, BlockPos pos, int radius) {
        ChunkSectionPos.stream(new ChunkPos(pos), Math.floorDiv(radius, 16), this.world.getBottomSectionCoord(), this.world.getTopSectionCoord()).map(sectionPos -> Pair.of(sectionPos, this.get(sectionPos.asLong()))).filter(pair -> ((Optional)pair.getSecond()).map(PointOfInterestSet::isValid).orElse(false) == false).map(pair -> ((ChunkSectionPos)pair.getFirst()).toChunkPos()).filter(chunkPos -> this.preloadedChunks.add(chunkPos.toLong())).forEach(arg2 -> world.getChunk(arg2.x, arg2.z, ChunkStatus.EMPTY));
    }

    final class PointOfInterestDistanceTracker
    extends SectionDistanceLevelPropagator {
        private final Long2ByteMap distances;

        protected PointOfInterestDistanceTracker() {
            super(7, 16, 256);
            this.distances = new Long2ByteOpenHashMap();
            this.distances.defaultReturnValue((byte)7);
        }

        @Override
        protected int getInitialLevel(long id) {
            return PointOfInterestStorage.this.isOccupied(id) ? 0 : 7;
        }

        @Override
        protected int getLevel(long id) {
            return this.distances.get(id);
        }

        @Override
        protected void setLevel(long id, int level) {
            if (level > 6) {
                this.distances.remove(id);
            } else {
                this.distances.put(id, (byte)level);
            }
        }

        public void update() {
            super.applyPendingUpdates(Integer.MAX_VALUE);
        }
    }

    public static enum OccupationStatus {
        HAS_SPACE(PointOfInterest::hasSpace),
        IS_OCCUPIED(PointOfInterest::isOccupied),
        ANY(poi -> true);

        private final Predicate<? super PointOfInterest> predicate;

        private OccupationStatus(Predicate<? super PointOfInterest> predicate) {
            this.predicate = predicate;
        }

        public Predicate<? super PointOfInterest> getPredicate() {
            return this.predicate;
        }
    }
}

