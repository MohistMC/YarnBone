/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.LevelPrioritizedQueue;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SimulationDistanceLevelPropagator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ThreadedAnvilChunkStorage
extends VersionedChunkStorage
implements ChunkHolder.PlayersWatchingChunkProvider {
    private static final byte PROTO_CHUNK = -1;
    private static final byte UNMARKED_CHUNK = 0;
    private static final byte LEVEL_CHUNK = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29674 = 200;
    private static final int field_36291 = 20;
    private static final int field_36384 = 10000;
    private static final int field_29675 = 3;
    public static final int field_29669 = 33;
    public static final int MAX_LEVEL = 33 + ChunkStatus.getMaxDistanceFromFull();
    public static final int field_29670 = 31;
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders = new Long2ObjectLinkedOpenHashMap();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = this.currentChunkHolders.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> chunksToUnload = new Long2ObjectLinkedOpenHashMap();
    private final LongSet loadedChunks = new LongOpenHashSet();
    final ServerWorld world;
    private final ServerLightingProvider lightingProvider;
    private final ThreadExecutor<Runnable> mainThreadExecutor;
    private ChunkGenerator chunkGenerator;
    private final NoiseConfig noiseConfig;
    private final StructurePlacementCalculator structurePlacementCalculator;
    private final Supplier<PersistentStateManager> persistentStateManagerFactory;
    private final PointOfInterestStorage pointOfInterestStorage;
    final LongSet unloadedChunks = new LongOpenHashSet();
    private boolean chunkHolderListDirty;
    private final ChunkTaskPrioritySystem chunkTaskPrioritySystem;
    private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> worldGenExecutor;
    private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> mainExecutor;
    private final WorldGenerationProgressListener worldGenerationProgressListener;
    private final ChunkStatusChangeListener chunkStatusChangeListener;
    private final TicketManager ticketManager;
    private final AtomicInteger totalChunksLoadedCount = new AtomicInteger();
    private final StructureTemplateManager structureTemplateManager;
    private final String saveDir;
    private final PlayerChunkWatchingManager playerChunkWatchingManager = new PlayerChunkWatchingManager();
    private final Int2ObjectMap<EntityTracker> entityTrackers = new Int2ObjectOpenHashMap<EntityTracker>();
    private final Long2ByteMap chunkToType = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkToNextSaveTimeMs = new Long2LongOpenHashMap();
    private final Queue<Runnable> unloadTaskQueue = Queues.newConcurrentLinkedQueue();
    int watchDistance;

    public ThreadedAnvilChunkStorage(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> persistentStateManagerFactory, int viewDistance, boolean dsync) {
        super(session.getWorldDirectory(world.getRegistryKey()).resolve("region"), dataFixer, dsync);
        this.structureTemplateManager = structureTemplateManager;
        Path path = session.getWorldDirectory(world.getRegistryKey());
        this.saveDir = path.getFileName().toString();
        this.world = world;
        this.chunkGenerator = chunkGenerator;
        DynamicRegistryManager lv = world.getRegistryManager();
        long l = world.getSeed();
        if (chunkGenerator instanceof NoiseChunkGenerator) {
            NoiseChunkGenerator lv2 = (NoiseChunkGenerator)chunkGenerator;
            this.noiseConfig = NoiseConfig.create(lv2.getSettings().value(), lv.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        } else {
            this.noiseConfig = NoiseConfig.create(ChunkGeneratorSettings.createMissingSettings(), lv.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        }
        this.structurePlacementCalculator = chunkGenerator.createStructurePlacementCalculator(lv.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), this.noiseConfig, l);
        this.mainThreadExecutor = mainThreadExecutor;
        TaskExecutor<Runnable> lv3 = TaskExecutor.create(executor, "worldgen");
        MessageListener<Runnable> lv4 = MessageListener.create("main", mainThreadExecutor::send);
        this.worldGenerationProgressListener = worldGenerationProgressListener;
        this.chunkStatusChangeListener = chunkStatusChangeListener;
        TaskExecutor<Runnable> lv5 = TaskExecutor.create(executor, "light");
        this.chunkTaskPrioritySystem = new ChunkTaskPrioritySystem(ImmutableList.of(lv3, lv4, lv5), executor, Integer.MAX_VALUE);
        this.worldGenExecutor = this.chunkTaskPrioritySystem.createExecutor(lv3, false);
        this.mainExecutor = this.chunkTaskPrioritySystem.createExecutor(lv4, false);
        this.lightingProvider = new ServerLightingProvider(chunkProvider, this, this.world.getDimension().hasSkyLight(), lv5, this.chunkTaskPrioritySystem.createExecutor(lv5, false));
        this.ticketManager = new TicketManager(executor, mainThreadExecutor);
        this.persistentStateManagerFactory = persistentStateManagerFactory;
        this.pointOfInterestStorage = new PointOfInterestStorage(path.resolve("poi"), dataFixer, dsync, lv, world);
        this.setViewDistance(viewDistance);
    }

    protected ChunkGenerator getChunkGenerator() {
        return this.chunkGenerator;
    }

    protected StructurePlacementCalculator getStructurePlacementCalculator() {
        return this.structurePlacementCalculator;
    }

    protected NoiseConfig getNoiseConfig() {
        return this.noiseConfig;
    }

    public void verifyChunkGenerator() {
        DataResult<JsonElement> dataResult = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, this.chunkGenerator);
        DataResult dataResult2 = dataResult.flatMap(json -> ChunkGenerator.CODEC.parse(JsonOps.INSTANCE, json));
        dataResult2.result().ifPresent(chunkGenerator -> {
            this.chunkGenerator = chunkGenerator;
        });
    }

    private static double getSquaredDistance(ChunkPos pos, Entity entity) {
        double d = ChunkSectionPos.getOffsetPos(pos.x, 8);
        double e = ChunkSectionPos.getOffsetPos(pos.z, 8);
        double f = d - entity.getX();
        double g = e - entity.getZ();
        return f * f + g * g;
    }

    public static boolean isWithinDistance(int x1, int z1, int x2, int z2, int distance) {
        int s;
        int t;
        int n = Math.max(0, Math.abs(x1 - x2) - 1);
        int o = Math.max(0, Math.abs(z1 - z2) - 1);
        long p = Math.max(0, Math.max(n, o) - 1);
        long q = Math.min(n, o);
        long r = q * q + p * p;
        return r <= (long)(t = (s = distance - 1) * s);
    }

    private static boolean isOnDistanceEdge(int x1, int z1, int x2, int z2, int distance) {
        if (!ThreadedAnvilChunkStorage.isWithinDistance(x1, z1, x2, z2, distance)) {
            return false;
        }
        if (!ThreadedAnvilChunkStorage.isWithinDistance(x1 + 1, z1, x2, z2, distance)) {
            return true;
        }
        if (!ThreadedAnvilChunkStorage.isWithinDistance(x1, z1 + 1, x2, z2, distance)) {
            return true;
        }
        if (!ThreadedAnvilChunkStorage.isWithinDistance(x1 - 1, z1, x2, z2, distance)) {
            return true;
        }
        return !ThreadedAnvilChunkStorage.isWithinDistance(x1, z1 - 1, x2, z2, distance);
    }

    protected ServerLightingProvider getLightingProvider() {
        return this.lightingProvider;
    }

    @Nullable
    protected ChunkHolder getCurrentChunkHolder(long pos) {
        return this.currentChunkHolders.get(pos);
    }

    @Nullable
    protected ChunkHolder getChunkHolder(long pos) {
        return this.chunkHolders.get(pos);
    }

    protected IntSupplier getCompletedLevelSupplier(long pos) {
        return () -> {
            ChunkHolder lv = this.getChunkHolder(pos);
            if (lv == null) {
                return LevelPrioritizedQueue.LEVEL_COUNT - 1;
            }
            return Math.min(lv.getCompletedLevel(), LevelPrioritizedQueue.LEVEL_COUNT - 1);
        };
    }

    public String getChunkLoadingDebugInfo(ChunkPos chunkPos) {
        ChunkHolder lv = this.getChunkHolder(chunkPos.toLong());
        if (lv == null) {
            return "null";
        }
        String string = lv.getLevel() + "\n";
        ChunkStatus lv2 = lv.getCurrentStatus();
        Chunk lv3 = lv.getCurrentChunk();
        if (lv2 != null) {
            string = string + "St: \u00a7" + lv2.getIndex() + lv2 + "\u00a7r\n";
        }
        if (lv3 != null) {
            string = string + "Ch: \u00a7" + lv3.getStatus().getIndex() + lv3.getStatus() + "\u00a7r\n";
        }
        ChunkHolder.LevelType lv4 = lv.getLevelType();
        string = string + "\u00a7" + lv4.ordinal() + lv4;
        return string + "\u00a7r";
    }

    private CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> getRegion(ChunkPos centerChunk, final int margin, IntFunction<ChunkStatus> distanceToStatus) {
        ArrayList<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> list = new ArrayList<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>>();
        ArrayList<ChunkHolder> list2 = new ArrayList<ChunkHolder>();
        final int j = centerChunk.x;
        final int k = centerChunk.z;
        for (int l = -margin; l <= margin; ++l) {
            for (int m = -margin; m <= margin; ++m) {
                int n = Math.max(Math.abs(m), Math.abs(l));
                final ChunkPos lv = new ChunkPos(j + m, k + l);
                long o = lv.toLong();
                ChunkHolder lv2 = this.getCurrentChunkHolder(o);
                if (lv2 == null) {
                    return CompletableFuture.completedFuture(Either.right(new ChunkHolder.Unloaded(){

                        public String toString() {
                            return "Unloaded " + lv;
                        }
                    }));
                }
                ChunkStatus lv3 = distanceToStatus.apply(n);
                CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = lv2.getChunkAt(lv3, this);
                list2.add(lv2);
                list.add(completableFuture);
            }
        }
        CompletableFuture completableFuture2 = Util.combineSafe(list);
        CompletionStage completableFuture3 = completableFuture2.thenApply(chunks -> {
            ArrayList<Chunk> list2 = Lists.newArrayList();
            int l = 0;
            for (final Either either : chunks) {
                if (either == null) {
                    throw this.crash(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }
                Optional optional = either.left();
                if (!optional.isPresent()) {
                    final int m = l;
                    return Either.right(new ChunkHolder.Unloaded(){

                        public String toString() {
                            return "Unloaded " + new ChunkPos(j + m % (margin * 2 + 1), k + m / (margin * 2 + 1)) + " " + either.right().get();
                        }
                    });
                }
                list2.add((Chunk)optional.get());
                ++l;
            }
            return Either.left(list2);
        });
        for (ChunkHolder lv4 : list2) {
            lv4.combineSavingFuture("getChunkRangeFuture " + centerChunk + " " + margin, (CompletableFuture<?>)completableFuture3);
        }
        return completableFuture3;
    }

    public CrashException crash(IllegalStateException exception, String details) {
        StringBuilder stringBuilder = new StringBuilder();
        Consumer<ChunkHolder> consumer = chunkHolder -> chunkHolder.collectFuturesByStatus().forEach(pair -> {
            ChunkStatus lv = (ChunkStatus)pair.getFirst();
            CompletableFuture completableFuture = (CompletableFuture)pair.getSecond();
            if (completableFuture != null && completableFuture.isDone() && completableFuture.join() == null) {
                stringBuilder.append(chunkHolder.getPos()).append(" - status: ").append(lv).append(" future: ").append(completableFuture).append(System.lineSeparator());
            }
        });
        stringBuilder.append("Updating:").append(System.lineSeparator());
        this.currentChunkHolders.values().forEach(consumer);
        stringBuilder.append("Visible:").append(System.lineSeparator());
        this.chunkHolders.values().forEach(consumer);
        CrashReport lv = CrashReport.create(exception, "Chunk loading");
        CrashReportSection lv2 = lv.addElement("Chunk loading");
        lv2.add("Details", details);
        lv2.add("Futures", stringBuilder);
        return new CrashException(lv);
    }

    public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> makeChunkEntitiesTickable(ChunkPos pos) {
        return this.getRegion(pos, 2, distance -> ChunkStatus.FULL).thenApplyAsync(either -> either.mapLeft(chunks -> (WorldChunk)chunks.get(chunks.size() / 2)), (Executor)this.mainThreadExecutor);
    }

    @Nullable
    ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int j) {
        if (j > MAX_LEVEL && level > MAX_LEVEL) {
            return holder;
        }
        if (holder != null) {
            holder.setLevel(level);
        }
        if (holder != null) {
            if (level > MAX_LEVEL) {
                this.unloadedChunks.add(pos);
            } else {
                this.unloadedChunks.remove(pos);
            }
        }
        if (level <= MAX_LEVEL && holder == null) {
            holder = this.chunksToUnload.remove(pos);
            if (holder != null) {
                holder.setLevel(level);
            } else {
                holder = new ChunkHolder(new ChunkPos(pos), level, this.world, this.lightingProvider, this.chunkTaskPrioritySystem, this);
            }
            this.currentChunkHolders.put(pos, holder);
            this.chunkHolderListDirty = true;
        }
        return holder;
    }

    @Override
    public void close() throws IOException {
        try {
            this.chunkTaskPrioritySystem.close();
            this.pointOfInterestStorage.close();
        }
        finally {
            super.close();
        }
    }

    protected void save(boolean flush) {
        if (flush) {
            List list = this.chunkHolders.values().stream().filter(ChunkHolder::isAccessible).peek(ChunkHolder::updateAccessibleStatus).collect(Collectors.toList());
            MutableBoolean mutableBoolean = new MutableBoolean();
            do {
                mutableBoolean.setFalse();
                list.stream().map(chunkHolder -> {
                    CompletableFuture<Chunk> completableFuture;
                    do {
                        completableFuture = chunkHolder.getSavingFuture();
                        this.mainThreadExecutor.runTasks(completableFuture::isDone);
                    } while (completableFuture != chunkHolder.getSavingFuture());
                    return completableFuture.join();
                }).filter(chunk -> chunk instanceof ReadOnlyChunk || chunk instanceof WorldChunk).filter(this::save).forEach(chunk -> mutableBoolean.setTrue());
            } while (mutableBoolean.isTrue());
            this.unloadChunks(() -> true);
            this.completeAll();
        } else {
            this.chunkHolders.values().forEach(this::save);
        }
    }

    protected void tick(BooleanSupplier shouldKeepTicking) {
        Profiler lv = this.world.getProfiler();
        lv.push("poi");
        this.pointOfInterestStorage.tick(shouldKeepTicking);
        lv.swap("chunk_unload");
        if (!this.world.isSavingDisabled()) {
            this.unloadChunks(shouldKeepTicking);
        }
        lv.pop();
    }

    public boolean shouldDelayShutdown() {
        return this.lightingProvider.hasUpdates() || !this.chunksToUnload.isEmpty() || !this.currentChunkHolders.isEmpty() || this.pointOfInterestStorage.hasUnsavedElements() || !this.unloadedChunks.isEmpty() || !this.unloadTaskQueue.isEmpty() || this.chunkTaskPrioritySystem.shouldDelayShutdown() || this.ticketManager.shouldDelayShutdown();
    }

    private void unloadChunks(BooleanSupplier shouldKeepTicking) {
        Runnable runnable;
        LongIterator longIterator = this.unloadedChunks.iterator();
        int i = 0;
        while (longIterator.hasNext() && (shouldKeepTicking.getAsBoolean() || i < 200 || this.unloadedChunks.size() > 2000)) {
            long l = longIterator.nextLong();
            ChunkHolder lv = this.currentChunkHolders.remove(l);
            if (lv != null) {
                this.chunksToUnload.put(l, lv);
                this.chunkHolderListDirty = true;
                ++i;
                this.tryUnloadChunk(l, lv);
            }
            longIterator.remove();
        }
        for (int j = Math.max(0, this.unloadTaskQueue.size() - 2000); (shouldKeepTicking.getAsBoolean() || j > 0) && (runnable = this.unloadTaskQueue.poll()) != null; --j) {
            runnable.run();
        }
        int k = 0;
        Iterator objectIterator = this.chunkHolders.values().iterator();
        while (k < 20 && shouldKeepTicking.getAsBoolean() && objectIterator.hasNext()) {
            if (!this.save((ChunkHolder)objectIterator.next())) continue;
            ++k;
        }
    }

    private void tryUnloadChunk(long pos, ChunkHolder holder) {
        CompletableFuture<Chunk> completableFuture = holder.getSavingFuture();
        ((CompletableFuture)completableFuture.thenAcceptAsync(chunk -> {
            CompletableFuture<Chunk> completableFuture2 = holder.getSavingFuture();
            if (completableFuture2 != completableFuture) {
                this.tryUnloadChunk(pos, holder);
                return;
            }
            if (this.chunksToUnload.remove(pos, (Object)holder) && chunk != null) {
                if (chunk instanceof WorldChunk) {
                    ((WorldChunk)chunk).setLoadedToWorld(false);
                }
                this.save((Chunk)chunk);
                if (this.loadedChunks.remove(pos) && chunk instanceof WorldChunk) {
                    WorldChunk lv = (WorldChunk)chunk;
                    this.world.unloadEntities(lv);
                }
                this.lightingProvider.updateChunkStatus(chunk.getPos());
                this.lightingProvider.tick();
                this.worldGenerationProgressListener.setChunkStatus(chunk.getPos(), null);
                this.chunkToNextSaveTimeMs.remove(chunk.getPos().toLong());
            }
        }, this.unloadTaskQueue::add)).whenComplete((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to save chunk {}", (Object)holder.getPos(), throwable);
            }
        });
    }

    protected boolean updateHolderMap() {
        if (!this.chunkHolderListDirty) {
            return false;
        }
        this.chunkHolders = this.currentChunkHolders.clone();
        this.chunkHolderListDirty = false;
        return true;
    }

    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> getChunk(ChunkHolder holder, ChunkStatus requiredStatus) {
        Optional<Chunk> optional;
        ChunkPos lv = holder.getPos();
        if (requiredStatus == ChunkStatus.EMPTY) {
            return this.loadChunk(lv);
        }
        if (requiredStatus == ChunkStatus.LIGHT) {
            this.ticketManager.addTicketWithLevel(ChunkTicketType.LIGHT, lv, 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.LIGHT), lv);
        }
        if ((optional = holder.getChunkAt(requiredStatus.getPrevious(), this).getNow(ChunkHolder.UNLOADED_CHUNK).left()).isPresent() && optional.get().getStatus().isAtLeast(requiredStatus)) {
            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = requiredStatus.runLoadTask(this.world, this.structureTemplateManager, this.lightingProvider, chunk -> this.convertToFullChunk(holder), optional.get());
            this.worldGenerationProgressListener.setChunkStatus(lv, requiredStatus);
            return completableFuture;
        }
        return this.upgradeChunk(holder, requiredStatus);
    }

    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> loadChunk(ChunkPos pos) {
        return ((CompletableFuture)((CompletableFuture)this.getUpdatedChunkNbt(pos).thenApply(nbt -> nbt.filter(nbt2 -> {
            boolean bl = ThreadedAnvilChunkStorage.containsStatus(nbt2);
            if (!bl) {
                LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)pos);
            }
            return bl;
        }))).thenApplyAsync(nbt -> {
            this.world.getProfiler().visit("chunkLoad");
            if (nbt.isPresent()) {
                ProtoChunk lv = ChunkSerializer.deserialize(this.world, this.pointOfInterestStorage, pos, (NbtCompound)nbt.get());
                this.mark(pos, ((Chunk)lv).getStatus().getChunkType());
                return Either.left(lv);
            }
            return Either.left(this.getProtoChunk(pos));
        }, (Executor)this.mainThreadExecutor)).exceptionallyAsync(throwable -> this.recoverFromException((Throwable)throwable, pos), (Executor)this.mainThreadExecutor);
    }

    private static boolean containsStatus(NbtCompound nbt) {
        return nbt.contains("Status", NbtElement.STRING_TYPE);
    }

    /*
     * Enabled aggressive block sorting
     */
    private Either<Chunk, ChunkHolder.Unloaded> recoverFromException(Throwable throwable, ChunkPos chunkPos) {
        if (!(throwable instanceof CrashException)) {
            if (!(throwable instanceof IOException)) return Either.left(this.getProtoChunk(chunkPos));
            LOGGER.error("Couldn't load chunk {}", (Object)chunkPos, (Object)throwable);
            return Either.left(this.getProtoChunk(chunkPos));
        }
        CrashException lv = (CrashException)throwable;
        Throwable throwable2 = lv.getCause();
        if (throwable2 instanceof IOException) {
            LOGGER.error("Couldn't load chunk {}", (Object)chunkPos, (Object)throwable2);
            return Either.left(this.getProtoChunk(chunkPos));
        }
        this.markAsProtoChunk(chunkPos);
        throw lv;
    }

    private Chunk getProtoChunk(ChunkPos chunkPos) {
        this.markAsProtoChunk(chunkPos);
        return new ProtoChunk(chunkPos, UpgradeData.NO_UPGRADE_DATA, this.world, this.world.getRegistryManager().get(RegistryKeys.BIOME), null);
    }

    private void markAsProtoChunk(ChunkPos pos) {
        this.chunkToType.put(pos.toLong(), (byte)-1);
    }

    private byte mark(ChunkPos pos, ChunkStatus.ChunkType type) {
        return this.chunkToType.put(pos.toLong(), type == ChunkStatus.ChunkType.PROTOCHUNK ? (byte)-1 : 1);
    }

    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> upgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus) {
        ChunkPos lv = holder.getPos();
        CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> completableFuture = this.getRegion(lv, requiredStatus.getTaskMargin(), distance -> this.getRequiredStatusForGeneration(requiredStatus, distance));
        this.world.getProfiler().visit(() -> "chunkGenerate " + requiredStatus.getId());
        Executor executor = task -> this.worldGenExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task));
        return completableFuture.thenComposeAsync(either -> either.map(chunks -> {
            try {
                CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = requiredStatus.runGenerationTask(executor, this.world, this.chunkGenerator, this.structureTemplateManager, this.lightingProvider, chunk -> this.convertToFullChunk(holder), (List<Chunk>)chunks, false);
                this.worldGenerationProgressListener.setChunkStatus(lv, requiredStatus);
                return completableFuture;
            }
            catch (Exception exception) {
                exception.getStackTrace();
                CrashReport lv = CrashReport.create(exception, "Exception generating new chunk");
                CrashReportSection lv2 = lv.addElement("Chunk to be generated");
                lv2.add("Location", String.format(Locale.ROOT, "%d,%d", arg.x, arg.z));
                lv2.add("Position hash", ChunkPos.toLong(arg.x, arg.z));
                lv2.add("Generator", this.chunkGenerator);
                this.mainThreadExecutor.execute(() -> {
                    throw new CrashException(lv);
                });
                throw new CrashException(lv);
            }
        }, unloaded -> {
            this.releaseLightTicket(lv);
            return CompletableFuture.completedFuture(Either.right(unloaded));
        }), executor);
    }

    protected void releaseLightTicket(ChunkPos pos) {
        this.mainThreadExecutor.send(Util.debugRunnable(() -> this.ticketManager.removeTicketWithLevel(ChunkTicketType.LIGHT, pos, 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.LIGHT), pos), () -> "release light ticket " + pos));
    }

    private ChunkStatus getRequiredStatusForGeneration(ChunkStatus centerChunkTargetStatus, int distance) {
        ChunkStatus lv = distance == 0 ? centerChunkTargetStatus.getPrevious() : ChunkStatus.byDistanceFromFull(ChunkStatus.getDistanceFromFull(centerChunkTargetStatus) + distance);
        return lv;
    }

    private static void addEntitiesFromNbt(ServerWorld world, List<NbtCompound> nbt) {
        if (!nbt.isEmpty()) {
            world.addEntities(EntityType.streamFromNbt(nbt, world));
        }
    }

    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> convertToFullChunk(ChunkHolder chunkHolder) {
        CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = chunkHolder.getFutureFor(ChunkStatus.FULL.getPrevious());
        return completableFuture.thenApplyAsync(either -> {
            ChunkStatus lv = ChunkHolder.getTargetStatusForLevel(chunkHolder.getLevel());
            if (!lv.isAtLeast(ChunkStatus.FULL)) {
                return ChunkHolder.UNLOADED_CHUNK;
            }
            return either.mapLeft(protoChunk -> {
                WorldChunk lv3;
                ChunkPos lv = chunkHolder.getPos();
                ProtoChunk lv2 = (ProtoChunk)protoChunk;
                if (lv2 instanceof ReadOnlyChunk) {
                    lv3 = ((ReadOnlyChunk)lv2).getWrappedChunk();
                } else {
                    lv3 = new WorldChunk(this.world, lv2, chunk -> ThreadedAnvilChunkStorage.addEntitiesFromNbt(this.world, lv2.getEntities()));
                    chunkHolder.setCompletedChunk(new ReadOnlyChunk(lv3, false));
                }
                lv3.setLevelTypeProvider(() -> ChunkHolder.getLevelType(chunkHolder.getLevel()));
                lv3.loadEntities();
                if (this.loadedChunks.add(lv.toLong())) {
                    lv3.setLoadedToWorld(true);
                    lv3.updateAllBlockEntities();
                    lv3.addChunkTickSchedulers(this.world);
                }
                return lv3;
            });
        }, task -> this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(task, chunkHolder.getPos().toLong(), chunkHolder::getLevel)));
    }

    public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> makeChunkTickable(ChunkHolder holder) {
        ChunkPos lv = holder.getPos();
        CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> completableFuture = this.getRegion(lv, 1, i -> ChunkStatus.FULL);
        CompletionStage completableFuture2 = ((CompletableFuture)completableFuture.thenApplyAsync(either -> either.mapLeft(list -> (WorldChunk)list.get(list.size() / 2)), task -> this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task)))).thenApplyAsync(either -> either.ifLeft(chunk -> {
            chunk.runPostProcessing();
            this.world.disableTickSchedulers((WorldChunk)chunk);
        }), (Executor)this.mainThreadExecutor);
        ((CompletableFuture)completableFuture2).thenAcceptAsync(either -> either.ifLeft(chunk -> {
            this.totalChunksLoadedCount.getAndIncrement();
            MutableObject mutableObject = new MutableObject();
            this.getPlayersWatchingChunk(lv, false).forEach(player -> this.sendChunkDataPackets((ServerPlayerEntity)player, mutableObject, (WorldChunk)chunk));
        }), task -> this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task)));
        return completableFuture2;
    }

    public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> makeChunkAccessible(ChunkHolder holder) {
        return this.getRegion(holder.getPos(), 1, ChunkStatus::byDistanceFromFull).thenApplyAsync(either -> either.mapLeft(chunks -> {
            WorldChunk lv = (WorldChunk)chunks.get(chunks.size() / 2);
            return lv;
        }), task -> this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task)));
    }

    public int getTotalChunksLoadedCount() {
        return this.totalChunksLoadedCount.get();
    }

    private boolean save(ChunkHolder chunkHolder) {
        if (!chunkHolder.isAccessible()) {
            return false;
        }
        Chunk lv = chunkHolder.getSavingFuture().getNow(null);
        if (lv instanceof ReadOnlyChunk || lv instanceof WorldChunk) {
            long l = lv.getPos().toLong();
            long m = this.chunkToNextSaveTimeMs.getOrDefault(l, -1L);
            long n = System.currentTimeMillis();
            if (n < m) {
                return false;
            }
            boolean bl = this.save(lv);
            chunkHolder.updateAccessibleStatus();
            if (bl) {
                this.chunkToNextSaveTimeMs.put(l, n + 10000L);
            }
            return bl;
        }
        return false;
    }

    private boolean save(Chunk chunk) {
        this.pointOfInterestStorage.saveChunk(chunk.getPos());
        if (!chunk.needsSaving()) {
            return false;
        }
        chunk.setNeedsSaving(false);
        ChunkPos lv = chunk.getPos();
        try {
            ChunkStatus lv2 = chunk.getStatus();
            if (lv2.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                if (this.isLevelChunk(lv)) {
                    return false;
                }
                if (lv2 == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                    return false;
                }
            }
            this.world.getProfiler().visit("chunkSave");
            NbtCompound lv3 = ChunkSerializer.serialize(this.world, chunk);
            this.setNbt(lv, lv3);
            this.mark(lv, lv2.getChunkType());
            return true;
        }
        catch (Exception exception) {
            LOGGER.error("Failed to save chunk {},{}", lv.x, lv.z, exception);
            return false;
        }
    }

    private boolean isLevelChunk(ChunkPos pos) {
        NbtCompound lv;
        byte b = this.chunkToType.get(pos.toLong());
        if (b != 0) {
            return b == 1;
        }
        try {
            lv = this.getUpdatedChunkNbt(pos).join().orElse(null);
            if (lv == null) {
                this.markAsProtoChunk(pos);
                return false;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Failed to read chunk {}", (Object)pos, (Object)exception);
            this.markAsProtoChunk(pos);
            return false;
        }
        ChunkStatus.ChunkType lv2 = ChunkSerializer.getChunkType(lv);
        return this.mark(pos, lv2) == 1;
    }

    protected void setViewDistance(int watchDistance) {
        int j = MathHelper.clamp(watchDistance + 1, 3, 33);
        if (j != this.watchDistance) {
            int k = this.watchDistance;
            this.watchDistance = j;
            this.ticketManager.setWatchDistance(this.watchDistance + 1);
            for (ChunkHolder lv : this.currentChunkHolders.values()) {
                ChunkPos lv2 = lv.getPos();
                MutableObject mutableObject = new MutableObject();
                this.getPlayersWatchingChunk(lv2, false).forEach(player -> {
                    ChunkSectionPos lv = player.getWatchedSection();
                    boolean bl = ThreadedAnvilChunkStorage.isWithinDistance(arg.x, arg.z, lv.getSectionX(), lv.getSectionZ(), k);
                    boolean bl2 = ThreadedAnvilChunkStorage.isWithinDistance(arg.x, arg.z, lv.getSectionX(), lv.getSectionZ(), this.watchDistance);
                    this.sendWatchPackets((ServerPlayerEntity)player, lv2, mutableObject, bl, bl2);
                });
            }
        }
    }

    protected void sendWatchPackets(ServerPlayerEntity player, ChunkPos pos, MutableObject<ChunkDataS2CPacket> packet, boolean oldWithinViewDistance, boolean newWithinViewDistance) {
        ChunkHolder lv;
        if (player.world != this.world) {
            return;
        }
        if (newWithinViewDistance && !oldWithinViewDistance && (lv = this.getChunkHolder(pos.toLong())) != null) {
            WorldChunk lv2 = lv.getWorldChunk();
            if (lv2 != null) {
                this.sendChunkDataPackets(player, packet, lv2);
            }
            DebugInfoSender.sendChunkWatchingChange(this.world, pos);
        }
        if (!newWithinViewDistance && oldWithinViewDistance) {
            player.sendUnloadChunkPacket(pos);
        }
    }

    public int getLoadedChunkCount() {
        return this.chunkHolders.size();
    }

    public ChunkTicketManager getTicketManager() {
        return this.ticketManager;
    }

    protected Iterable<ChunkHolder> entryIterator() {
        return Iterables.unmodifiableIterable(this.chunkHolders.values());
    }

    void dump(Writer writer) throws IOException {
        CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").startBody(writer);
        SimulationDistanceLevelPropagator lv2 = this.ticketManager.getSimulationDistanceTracker();
        for (Long2ObjectMap.Entry entry : this.chunkHolders.long2ObjectEntrySet()) {
            long l = entry.getLongKey();
            ChunkPos lv3 = new ChunkPos(l);
            ChunkHolder lv4 = (ChunkHolder)entry.getValue();
            Optional<Chunk> optional = Optional.ofNullable(lv4.getCurrentChunk());
            Optional<Object> optional2 = optional.flatMap(chunk -> chunk instanceof WorldChunk ? Optional.of((WorldChunk)chunk) : Optional.empty());
            lv.printRow(lv3.x, lv3.z, lv4.getLevel(), optional.isPresent(), optional.map(Chunk::getStatus).orElse(null), optional2.map(WorldChunk::getLevelType).orElse(null), ThreadedAnvilChunkStorage.getFutureStatus(lv4.getAccessibleFuture()), ThreadedAnvilChunkStorage.getFutureStatus(lv4.getTickingFuture()), ThreadedAnvilChunkStorage.getFutureStatus(lv4.getEntityTickingFuture()), this.ticketManager.getTicket(l), this.shouldTick(lv3), optional2.map(chunk -> chunk.getBlockEntities().size()).orElse(0), lv2.getTickingTicket(l), lv2.getLevel(l), optional2.map(chunk -> chunk.getBlockTickScheduler().getTickCount()).orElse(0), optional2.map(chunk -> chunk.getFluidTickScheduler().getTickCount()).orElse(0));
        }
    }

    private static String getFutureStatus(CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> future) {
        try {
            Either either = future.getNow(null);
            if (either != null) {
                return either.map(chunk -> "done", unloaded -> "unloaded");
            }
            return "not completed";
        }
        catch (CompletionException completionException) {
            return "failed " + completionException.getCause().getMessage();
        }
        catch (CancellationException cancellationException) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<NbtCompound>> getUpdatedChunkNbt(ChunkPos chunkPos) {
        return this.getNbt(chunkPos).thenApplyAsync(nbt -> nbt.map(this::updateChunkNbt), (Executor)Util.getMainWorkerExecutor());
    }

    private NbtCompound updateChunkNbt(NbtCompound nbt) {
        return this.updateChunkNbt(this.world.getRegistryKey(), this.persistentStateManagerFactory, nbt, this.chunkGenerator.getCodecKey());
    }

    boolean shouldTick(ChunkPos pos) {
        long l = pos.toLong();
        if (!this.ticketManager.shouldTick(l)) {
            return false;
        }
        for (ServerPlayerEntity lv : this.playerChunkWatchingManager.getPlayersWatchingChunk(l)) {
            if (!this.canTickChunk(lv, pos)) continue;
            return true;
        }
        return false;
    }

    public List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos pos) {
        long l = pos.toLong();
        if (!this.ticketManager.shouldTick(l)) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayerEntity lv : this.playerChunkWatchingManager.getPlayersWatchingChunk(l)) {
            if (!this.canTickChunk(lv, pos)) continue;
            builder.add(lv);
        }
        return builder.build();
    }

    private boolean canTickChunk(ServerPlayerEntity player, ChunkPos pos) {
        if (player.isSpectator()) {
            return false;
        }
        double d = ThreadedAnvilChunkStorage.getSquaredDistance(pos, player);
        return d < 16384.0;
    }

    private boolean doesNotGenerateChunks(ServerPlayerEntity player) {
        return player.isSpectator() && !this.world.getGameRules().getBoolean(GameRules.SPECTATORS_GENERATE_CHUNKS);
    }

    void handlePlayerAddedOrRemoved(ServerPlayerEntity player, boolean added) {
        boolean bl2 = this.doesNotGenerateChunks(player);
        boolean bl3 = this.playerChunkWatchingManager.isWatchInactive(player);
        int i = ChunkSectionPos.getSectionCoord(player.getBlockX());
        int j = ChunkSectionPos.getSectionCoord(player.getBlockZ());
        if (added) {
            this.playerChunkWatchingManager.add(ChunkPos.toLong(i, j), player, bl2);
            this.updateWatchedSection(player);
            if (!bl2) {
                this.ticketManager.handleChunkEnter(ChunkSectionPos.from(player), player);
            }
        } else {
            ChunkSectionPos lv = player.getWatchedSection();
            this.playerChunkWatchingManager.remove(lv.toChunkPos().toLong(), player);
            if (!bl3) {
                this.ticketManager.handleChunkLeave(lv, player);
            }
        }
        for (int k = i - this.watchDistance - 1; k <= i + this.watchDistance + 1; ++k) {
            for (int l = j - this.watchDistance - 1; l <= j + this.watchDistance + 1; ++l) {
                if (!ThreadedAnvilChunkStorage.isWithinDistance(k, l, i, j, this.watchDistance)) continue;
                ChunkPos lv2 = new ChunkPos(k, l);
                this.sendWatchPackets(player, lv2, new MutableObject<ChunkDataS2CPacket>(), !added, added);
            }
        }
    }

    private ChunkSectionPos updateWatchedSection(ServerPlayerEntity player) {
        ChunkSectionPos lv = ChunkSectionPos.from(player);
        player.setWatchedSection(lv);
        player.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(lv.getSectionX(), lv.getSectionZ()));
        return lv;
    }

    public void updatePosition(ServerPlayerEntity player) {
        boolean bl3;
        for (EntityTracker lv : this.entityTrackers.values()) {
            if (lv.entity == player) {
                lv.updateTrackedStatus(this.world.getPlayers());
                continue;
            }
            lv.updateTrackedStatus(player);
        }
        int i = ChunkSectionPos.getSectionCoord(player.getBlockX());
        int j = ChunkSectionPos.getSectionCoord(player.getBlockZ());
        ChunkSectionPos lv2 = player.getWatchedSection();
        ChunkSectionPos lv3 = ChunkSectionPos.from(player);
        long l = lv2.toChunkPos().toLong();
        long m = lv3.toChunkPos().toLong();
        boolean bl = this.playerChunkWatchingManager.isWatchDisabled(player);
        boolean bl2 = this.doesNotGenerateChunks(player);
        boolean bl4 = bl3 = lv2.asLong() != lv3.asLong();
        if (bl3 || bl != bl2) {
            this.updateWatchedSection(player);
            if (!bl) {
                this.ticketManager.handleChunkLeave(lv2, player);
            }
            if (!bl2) {
                this.ticketManager.handleChunkEnter(lv3, player);
            }
            if (!bl && bl2) {
                this.playerChunkWatchingManager.disableWatch(player);
            }
            if (bl && !bl2) {
                this.playerChunkWatchingManager.enableWatch(player);
            }
            if (l != m) {
                this.playerChunkWatchingManager.movePlayer(l, m, player);
            }
        }
        int k = lv2.getSectionX();
        int n = lv2.getSectionZ();
        if (Math.abs(k - i) <= this.watchDistance * 2 && Math.abs(n - j) <= this.watchDistance * 2) {
            int o = Math.min(i, k) - this.watchDistance - 1;
            int p = Math.min(j, n) - this.watchDistance - 1;
            int q = Math.max(i, k) + this.watchDistance + 1;
            int r = Math.max(j, n) + this.watchDistance + 1;
            for (int s = o; s <= q; ++s) {
                for (int t = p; t <= r; ++t) {
                    boolean bl42 = ThreadedAnvilChunkStorage.isWithinDistance(s, t, k, n, this.watchDistance);
                    boolean bl5 = ThreadedAnvilChunkStorage.isWithinDistance(s, t, i, j, this.watchDistance);
                    this.sendWatchPackets(player, new ChunkPos(s, t), new MutableObject<ChunkDataS2CPacket>(), bl42, bl5);
                }
            }
        } else {
            boolean bl7;
            boolean bl6;
            int p;
            int o;
            for (o = k - this.watchDistance - 1; o <= k + this.watchDistance + 1; ++o) {
                for (p = n - this.watchDistance - 1; p <= n + this.watchDistance + 1; ++p) {
                    if (!ThreadedAnvilChunkStorage.isWithinDistance(o, p, k, n, this.watchDistance)) continue;
                    bl6 = true;
                    bl7 = false;
                    this.sendWatchPackets(player, new ChunkPos(o, p), new MutableObject<ChunkDataS2CPacket>(), true, false);
                }
            }
            for (o = i - this.watchDistance - 1; o <= i + this.watchDistance + 1; ++o) {
                for (p = j - this.watchDistance - 1; p <= j + this.watchDistance + 1; ++p) {
                    if (!ThreadedAnvilChunkStorage.isWithinDistance(o, p, i, j, this.watchDistance)) continue;
                    bl6 = false;
                    bl7 = true;
                    this.sendWatchPackets(player, new ChunkPos(o, p), new MutableObject<ChunkDataS2CPacket>(), false, true);
                }
            }
        }
    }

    @Override
    public List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge) {
        Set<ServerPlayerEntity> set = this.playerChunkWatchingManager.getPlayersWatchingChunk(chunkPos.toLong());
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayerEntity lv : set) {
            ChunkSectionPos lv2 = lv.getWatchedSection();
            if ((!onlyOnWatchDistanceEdge || !ThreadedAnvilChunkStorage.isOnDistanceEdge(chunkPos.x, chunkPos.z, lv2.getSectionX(), lv2.getSectionZ(), this.watchDistance)) && (onlyOnWatchDistanceEdge || !ThreadedAnvilChunkStorage.isWithinDistance(chunkPos.x, chunkPos.z, lv2.getSectionX(), lv2.getSectionZ(), this.watchDistance))) continue;
            builder.add(lv);
        }
        return builder.build();
    }

    protected void loadEntity(Entity entity) {
        if (entity instanceof EnderDragonPart) {
            return;
        }
        EntityType<?> lv = entity.getType();
        int i = lv.getMaxTrackDistance() * 16;
        if (i == 0) {
            return;
        }
        int j = lv.getTrackTickInterval();
        if (this.entityTrackers.containsKey(entity.getId())) {
            throw Util.throwOrPause(new IllegalStateException("Entity is already tracked!"));
        }
        EntityTracker lv2 = new EntityTracker(entity, i, j, lv.alwaysUpdateVelocity());
        this.entityTrackers.put(entity.getId(), lv2);
        lv2.updateTrackedStatus(this.world.getPlayers());
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv3 = (ServerPlayerEntity)entity;
            this.handlePlayerAddedOrRemoved(lv3, true);
            for (EntityTracker lv4 : this.entityTrackers.values()) {
                if (lv4.entity == lv3) continue;
                lv4.updateTrackedStatus(lv3);
            }
        }
    }

    protected void unloadEntity(Entity entity) {
        EntityTracker lv3;
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            this.handlePlayerAddedOrRemoved(lv, false);
            for (EntityTracker lv2 : this.entityTrackers.values()) {
                lv2.stopTracking(lv);
            }
        }
        if ((lv3 = (EntityTracker)this.entityTrackers.remove(entity.getId())) != null) {
            lv3.stopTracking();
        }
    }

    protected void tickEntityMovement() {
        ArrayList<ServerPlayerEntity> list = Lists.newArrayList();
        List<ServerPlayerEntity> list2 = this.world.getPlayers();
        for (EntityTracker lv : this.entityTrackers.values()) {
            boolean bl;
            ChunkSectionPos lv2 = lv.trackedSection;
            ChunkSectionPos lv3 = ChunkSectionPos.from(lv.entity);
            boolean bl2 = bl = !Objects.equals(lv2, lv3);
            if (bl) {
                lv.updateTrackedStatus(list2);
                Entity lv4 = lv.entity;
                if (lv4 instanceof ServerPlayerEntity) {
                    list.add((ServerPlayerEntity)lv4);
                }
                lv.trackedSection = lv3;
            }
            if (!bl && !this.ticketManager.shouldTickEntities(lv3.toChunkPos().toLong())) continue;
            lv.entry.tick();
        }
        if (!list.isEmpty()) {
            for (EntityTracker lv : this.entityTrackers.values()) {
                lv.updateTrackedStatus(list);
            }
        }
    }

    public void sendToOtherNearbyPlayers(Entity entity, Packet<?> packet) {
        EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (lv != null) {
            lv.sendToOtherNearbyPlayers(packet);
        }
    }

    protected void sendToNearbyPlayers(Entity entity, Packet<?> packet) {
        EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (lv != null) {
            lv.sendToNearbyPlayers(packet);
        }
    }

    public void sendChunkBiomePackets(List<Chunk> chunks2) {
        HashMap<ServerPlayerEntity, List> map = new HashMap<ServerPlayerEntity, List>();
        for (Chunk lv : chunks2) {
            WorldChunk lv3;
            ChunkPos lv2 = lv.getPos();
            WorldChunk lv4 = lv instanceof WorldChunk ? (lv3 = (WorldChunk)lv) : this.world.getChunk(lv2.x, lv2.z);
            for (ServerPlayerEntity lv5 : this.getPlayersWatchingChunk(lv2, false)) {
                map.computeIfAbsent(lv5, player -> new ArrayList()).add(lv4);
            }
        }
        map.forEach((player, chunks) -> player.networkHandler.sendPacket(ChunkBiomeDataS2CPacket.create(chunks)));
    }

    private void sendChunkDataPackets(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk) {
        if (cachedDataPacket.getValue() == null) {
            cachedDataPacket.setValue(new ChunkDataS2CPacket(chunk, this.lightingProvider, null, null, true));
        }
        player.sendChunkPacket(chunk.getPos(), cachedDataPacket.getValue());
        DebugInfoSender.sendChunkWatchingChange(this.world, chunk.getPos());
        ArrayList<Entity> list = Lists.newArrayList();
        ArrayList<Entity> list2 = Lists.newArrayList();
        for (EntityTracker lv : this.entityTrackers.values()) {
            Entity lv2 = lv.entity;
            if (lv2 == player || !lv2.getChunkPos().equals(chunk.getPos())) continue;
            lv.updateTrackedStatus(player);
            if (lv2 instanceof MobEntity && ((MobEntity)lv2).getHoldingEntity() != null) {
                list.add(lv2);
            }
            if (lv2.getPassengerList().isEmpty()) continue;
            list2.add(lv2);
        }
        if (!list.isEmpty()) {
            for (Entity lv3 : list) {
                player.networkHandler.sendPacket(new EntityAttachS2CPacket(lv3, ((MobEntity)lv3).getHoldingEntity()));
            }
        }
        if (!list2.isEmpty()) {
            for (Entity lv3 : list2) {
                player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(lv3));
            }
        }
    }

    protected PointOfInterestStorage getPointOfInterestStorage() {
        return this.pointOfInterestStorage;
    }

    public String getSaveDir() {
        return this.saveDir;
    }

    void onChunkStatusChange(ChunkPos chunkPos, ChunkHolder.LevelType levelType) {
        this.chunkStatusChangeListener.onChunkStatusChange(chunkPos, levelType);
    }

    class TicketManager
    extends ChunkTicketManager {
        protected TicketManager(Executor workerExecutor, Executor mainThreadExecutor) {
            super(workerExecutor, mainThreadExecutor);
        }

        @Override
        protected boolean isUnloaded(long pos) {
            return ThreadedAnvilChunkStorage.this.unloadedChunks.contains(pos);
        }

        @Override
        @Nullable
        protected ChunkHolder getChunkHolder(long pos) {
            return ThreadedAnvilChunkStorage.this.getCurrentChunkHolder(pos);
        }

        @Override
        @Nullable
        protected ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int j) {
            return ThreadedAnvilChunkStorage.this.setLevel(pos, level, holder, j);
        }
    }

    class EntityTracker {
        final EntityTrackerEntry entry;
        final Entity entity;
        private final int maxDistance;
        ChunkSectionPos trackedSection;
        private final Set<EntityTrackingListener> listeners = Sets.newIdentityHashSet();

        public EntityTracker(Entity entity, int maxDistance, int tickInterval, boolean alwaysUpdateVelocity) {
            this.entry = new EntityTrackerEntry(ThreadedAnvilChunkStorage.this.world, entity, tickInterval, alwaysUpdateVelocity, this::sendToOtherNearbyPlayers);
            this.entity = entity;
            this.maxDistance = maxDistance;
            this.trackedSection = ChunkSectionPos.from(entity);
        }

        public boolean equals(Object o) {
            if (o instanceof EntityTracker) {
                return ((EntityTracker)o).entity.getId() == this.entity.getId();
            }
            return false;
        }

        public int hashCode() {
            return this.entity.getId();
        }

        public void sendToOtherNearbyPlayers(Packet<?> packet) {
            for (EntityTrackingListener lv : this.listeners) {
                lv.sendPacket(packet);
            }
        }

        public void sendToNearbyPlayers(Packet<?> packet) {
            this.sendToOtherNearbyPlayers(packet);
            if (this.entity instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
            }
        }

        public void stopTracking() {
            for (EntityTrackingListener lv : this.listeners) {
                this.entry.stopTracking(lv.getPlayer());
            }
        }

        public void stopTracking(ServerPlayerEntity player) {
            if (this.listeners.remove(player.networkHandler)) {
                this.entry.stopTracking(player);
            }
        }

        public void updateTrackedStatus(ServerPlayerEntity player) {
            boolean bl;
            if (player == this.entity) {
                return;
            }
            Vec3d lv = player.getPos().subtract(this.entity.getPos());
            double e = lv.x * lv.x + lv.z * lv.z;
            double d = Math.min(this.getMaxTrackDistance(), (ThreadedAnvilChunkStorage.this.watchDistance - 1) * 16);
            double f = d * d;
            boolean bl2 = bl = e <= f && this.entity.canBeSpectated(player);
            if (bl) {
                if (this.listeners.add(player.networkHandler)) {
                    this.entry.startTracking(player);
                }
            } else if (this.listeners.remove(player.networkHandler)) {
                this.entry.stopTracking(player);
            }
        }

        private int adjustTrackingDistance(int initialDistance) {
            return ThreadedAnvilChunkStorage.this.world.getServer().adjustTrackingDistance(initialDistance);
        }

        private int getMaxTrackDistance() {
            int i = this.maxDistance;
            for (Entity lv : this.entity.getPassengersDeep()) {
                int j = lv.getType().getMaxTrackDistance() * 16;
                if (j <= i) continue;
                i = j;
            }
            return this.adjustTrackingDistance(i);
        }

        public void updateTrackedStatus(List<ServerPlayerEntity> players) {
            for (ServerPlayerEntity lv : players) {
                this.updateTrackedStatus(lv);
            }
        }
    }
}

