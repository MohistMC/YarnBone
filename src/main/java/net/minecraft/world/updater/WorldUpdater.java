/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.updater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.slf4j.Logger;

public class WorldUpdater {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory UPDATE_THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private final Registry<DimensionOptions> dimensionOptionsRegistry;
    private final Set<RegistryKey<World>> worldKeys;
    private final boolean eraseCache;
    private final LevelStorage.Session session;
    private final Thread updateThread;
    private final DataFixer dataFixer;
    private volatile boolean keepUpgradingChunks = true;
    private volatile boolean done;
    private volatile float progress;
    private volatile int totalChunkCount;
    private volatile int upgradedChunkCount;
    private volatile int skippedChunkCount;
    private final Object2FloatMap<RegistryKey<World>> dimensionProgress = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap(Util.identityHashStrategy()));
    private volatile Text status = Text.translatable("optimizeWorld.stage.counting");
    private static final Pattern REGION_FILE_PATTERN = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final PersistentStateManager persistentStateManager;

    public WorldUpdater(LevelStorage.Session session, DataFixer dataFixer, Registry<DimensionOptions> dimensionOptionsRegistry, boolean eraseCache) {
        this.dimensionOptionsRegistry = dimensionOptionsRegistry;
        this.worldKeys = dimensionOptionsRegistry.getKeys().stream().map(RegistryKeys::toWorldKey).collect(Collectors.toUnmodifiableSet());
        this.eraseCache = eraseCache;
        this.dataFixer = dataFixer;
        this.session = session;
        this.persistentStateManager = new PersistentStateManager(this.session.getWorldDirectory(World.OVERWORLD).resolve("data").toFile(), dataFixer);
        this.updateThread = UPDATE_THREAD_FACTORY.newThread(this::updateWorld);
        this.updateThread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Error upgrading world", throwable);
            this.status = Text.translatable("optimizeWorld.stage.failed");
            this.done = true;
        });
        this.updateThread.start();
    }

    public void cancel() {
        this.keepUpgradingChunks = false;
        try {
            this.updateThread.join();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void updateWorld() {
        this.totalChunkCount = 0;
        ImmutableMap.Builder<RegistryKey<World>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
        for (RegistryKey<World> lv : this.worldKeys) {
            List<ChunkPos> list = this.getChunkPositions(lv);
            builder.put(lv, list.listIterator());
            this.totalChunkCount += list.size();
        }
        if (this.totalChunkCount == 0) {
            this.done = true;
            return;
        }
        float f = this.totalChunkCount;
        ImmutableMap immutableMap = builder.build();
        ImmutableMap.Builder<RegistryKey<World>, VersionedChunkStorage> builder2 = ImmutableMap.builder();
        for (RegistryKey<World> lv2 : this.worldKeys) {
            Path path = this.session.getWorldDirectory(lv2);
            builder2.put(lv2, new VersionedChunkStorage(path.resolve("region"), this.dataFixer, true));
        }
        ImmutableMap immutableMap2 = builder2.build();
        long l = Util.getMeasuringTimeMs();
        this.status = Text.translatable("optimizeWorld.stage.upgrading");
        while (this.keepUpgradingChunks) {
            boolean bl = false;
            float g = 0.0f;
            for (RegistryKey<World> lv3 : this.worldKeys) {
                ListIterator listIterator = (ListIterator)immutableMap.get(lv3);
                VersionedChunkStorage lv4 = (VersionedChunkStorage)immutableMap2.get(lv3);
                if (listIterator.hasNext()) {
                    ChunkPos lv5 = (ChunkPos)listIterator.next();
                    boolean bl2 = false;
                    try {
                        NbtCompound lv6 = lv4.getNbt(lv5).join().orElse(null);
                        if (lv6 != null) {
                            boolean bl3;
                            int i = VersionedChunkStorage.getDataVersion(lv6);
                            ChunkGenerator lv7 = this.dimensionOptionsRegistry.getOrThrow(RegistryKeys.toDimensionKey(lv3)).chunkGenerator();
                            NbtCompound lv8 = lv4.updateChunkNbt(lv3, () -> this.persistentStateManager, lv6, lv7.getCodecKey());
                            ChunkPos lv9 = new ChunkPos(lv8.getInt("xPos"), lv8.getInt("zPos"));
                            if (!lv9.equals(lv5)) {
                                LOGGER.warn("Chunk {} has invalid position {}", (Object)lv5, (Object)lv9);
                            }
                            boolean bl4 = bl3 = i < SharedConstants.getGameVersion().getSaveVersion().getId();
                            if (this.eraseCache) {
                                bl3 = bl3 || lv8.contains("Heightmaps");
                                lv8.remove("Heightmaps");
                                bl3 = bl3 || lv8.contains("isLightOn");
                                lv8.remove("isLightOn");
                                NbtList lv10 = lv8.getList("sections", NbtElement.COMPOUND_TYPE);
                                for (int j = 0; j < lv10.size(); ++j) {
                                    NbtCompound lv11 = lv10.getCompound(j);
                                    bl3 = bl3 || lv11.contains("BlockLight");
                                    lv11.remove("BlockLight");
                                    bl3 = bl3 || lv11.contains("SkyLight");
                                    lv11.remove("SkyLight");
                                }
                            }
                            if (bl3) {
                                lv4.setNbt(lv5, lv8);
                                bl2 = true;
                            }
                        }
                    }
                    catch (CompletionException | CrashException runtimeException) {
                        Throwable throwable = runtimeException.getCause();
                        if (throwable instanceof IOException) {
                            LOGGER.error("Error upgrading chunk {}", (Object)lv5, (Object)throwable);
                        }
                        throw runtimeException;
                    }
                    if (bl2) {
                        ++this.upgradedChunkCount;
                    } else {
                        ++this.skippedChunkCount;
                    }
                    bl = true;
                }
                float h = (float)listIterator.nextIndex() / f;
                this.dimensionProgress.put(lv3, h);
                g += h;
            }
            this.progress = g;
            if (bl) continue;
            this.keepUpgradingChunks = false;
        }
        this.status = Text.translatable("optimizeWorld.stage.finished");
        for (VersionedChunkStorage lv12 : immutableMap2.values()) {
            try {
                lv12.close();
            }
            catch (IOException iOException) {
                LOGGER.error("Error upgrading chunk", iOException);
            }
        }
        this.persistentStateManager.save();
        l = Util.getMeasuringTimeMs() - l;
        LOGGER.info("World optimizaton finished after {} ms", (Object)l);
        this.done = true;
    }

    private List<ChunkPos> getChunkPositions(RegistryKey<World> world) {
        File file = this.session.getWorldDirectory(world).toFile();
        File file2 = new File(file, "region");
        File[] files = file2.listFiles((directory, name) -> name.endsWith(".mca"));
        if (files == null) {
            return ImmutableList.of();
        }
        ArrayList<ChunkPos> list = Lists.newArrayList();
        for (File file3 : files) {
            Matcher matcher = REGION_FILE_PATTERN.matcher(file3.getName());
            if (!matcher.matches()) continue;
            int i = Integer.parseInt(matcher.group(1)) << 5;
            int j = Integer.parseInt(matcher.group(2)) << 5;
            try (RegionFile lv = new RegionFile(file3.toPath(), file2.toPath(), true);){
                for (int k = 0; k < 32; ++k) {
                    for (int l = 0; l < 32; ++l) {
                        ChunkPos lv2 = new ChunkPos(k + i, l + j);
                        if (!lv.isChunkValid(lv2)) continue;
                        list.add(lv2);
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return list;
    }

    public boolean isDone() {
        return this.done;
    }

    public Set<RegistryKey<World>> getWorlds() {
        return this.worldKeys;
    }

    public float getProgress(RegistryKey<World> world) {
        return this.dimensionProgress.getFloat(world);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunkCount() {
        return this.totalChunkCount;
    }

    public int getUpgradedChunkCount() {
        return this.upgradedChunkCount;
    }

    public int getSkippedChunkCount() {
        return this.skippedChunkCount;
    }

    public Text getStatus() {
        return this.status;
    }
}

