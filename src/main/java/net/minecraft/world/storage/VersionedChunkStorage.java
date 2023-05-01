/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.FeatureUpdater;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.storage.NbtScannable;
import net.minecraft.world.storage.StorageIoWorker;
import org.jetbrains.annotations.Nullable;

public class VersionedChunkStorage
implements AutoCloseable {
    public static final int FEATURE_UPDATING_VERSION = 1493;
    private final StorageIoWorker worker;
    protected final DataFixer dataFixer;
    @Nullable
    private volatile FeatureUpdater featureUpdater;

    public VersionedChunkStorage(Path directory, DataFixer dataFixer, boolean dsync) {
        this.dataFixer = dataFixer;
        this.worker = new StorageIoWorker(directory, dsync, "chunk");
    }

    public boolean needsBlending(ChunkPos chunkPos, int checkRadius) {
        return this.worker.needsBlending(chunkPos, checkRadius);
    }

    public NbtCompound updateChunkNbt(RegistryKey<World> worldKey, Supplier<PersistentStateManager> persistentStateManagerFactory, NbtCompound nbt, Optional<RegistryKey<Codec<? extends ChunkGenerator>>> generatorCodecKey) {
        int i = VersionedChunkStorage.getDataVersion(nbt);
        if (i < 1493 && (nbt = DataFixTypes.CHUNK.update(this.dataFixer, nbt, i, 1493)).getCompound("Level").getBoolean("hasLegacyStructureData")) {
            FeatureUpdater lv = this.getFeatureUpdater(worldKey, persistentStateManagerFactory);
            nbt = lv.getUpdatedReferences(nbt);
        }
        VersionedChunkStorage.saveContextToNbt(nbt, worldKey, generatorCodecKey);
        nbt = DataFixTypes.CHUNK.update(this.dataFixer, nbt, Math.max(1493, i));
        if (i < SharedConstants.getGameVersion().getSaveVersion().getId()) {
            NbtHelper.putDataVersion(nbt);
        }
        nbt.remove("__context");
        return nbt;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FeatureUpdater getFeatureUpdater(RegistryKey<World> worldKey, Supplier<PersistentStateManager> stateManagerGetter) {
        FeatureUpdater lv = this.featureUpdater;
        if (lv == null) {
            VersionedChunkStorage versionedChunkStorage = this;
            synchronized (versionedChunkStorage) {
                lv = this.featureUpdater;
                if (lv == null) {
                    this.featureUpdater = lv = FeatureUpdater.create(worldKey, stateManagerGetter.get());
                }
            }
        }
        return lv;
    }

    public static void saveContextToNbt(NbtCompound nbt, RegistryKey<World> worldKey, Optional<RegistryKey<Codec<? extends ChunkGenerator>>> generatorCodecKey) {
        NbtCompound lv = new NbtCompound();
        lv.putString("dimension", worldKey.getValue().toString());
        generatorCodecKey.ifPresent(key -> lv.putString("generator", key.getValue().toString()));
        nbt.put("__context", lv);
    }

    public static int getDataVersion(NbtCompound nbt) {
        return NbtHelper.getDataVersion(nbt, -1);
    }

    public CompletableFuture<Optional<NbtCompound>> getNbt(ChunkPos chunkPos) {
        return this.worker.readChunkData(chunkPos);
    }

    public void setNbt(ChunkPos chunkPos, NbtCompound nbt) {
        this.worker.setResult(chunkPos, nbt);
        if (this.featureUpdater != null) {
            this.featureUpdater.markResolved(chunkPos.toLong());
        }
    }

    public void completeAll() {
        this.worker.completeAll(true).join();
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }

    public NbtScannable getWorker() {
        return this.worker;
    }
}

