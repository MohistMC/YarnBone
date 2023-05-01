/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.PathUtil;
import net.minecraft.util.ThrowableDeliverer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;
import org.jetbrains.annotations.Nullable;

public final class RegionBasedStorage
implements AutoCloseable {
    public static final String MCA_EXTENSION = ".mca";
    private static final int MAX_CACHE_SIZE = 256;
    private final Long2ObjectLinkedOpenHashMap<RegionFile> cachedRegionFiles = new Long2ObjectLinkedOpenHashMap();
    private final Path directory;
    private final boolean dsync;

    RegionBasedStorage(Path directory, boolean dsync) {
        this.directory = directory;
        this.dsync = dsync;
    }

    private RegionFile getRegionFile(ChunkPos pos) throws IOException {
        long l = ChunkPos.toLong(pos.getRegionX(), pos.getRegionZ());
        RegionFile lv = this.cachedRegionFiles.getAndMoveToFirst(l);
        if (lv != null) {
            return lv;
        }
        if (this.cachedRegionFiles.size() >= 256) {
            this.cachedRegionFiles.removeLast().close();
        }
        PathUtil.createDirectories(this.directory);
        Path path = this.directory.resolve("r." + pos.getRegionX() + "." + pos.getRegionZ() + MCA_EXTENSION);
        RegionFile lv2 = new RegionFile(path, this.directory, this.dsync);
        this.cachedRegionFiles.putAndMoveToFirst(l, lv2);
        return lv2;
    }

    @Nullable
    public NbtCompound getTagAt(ChunkPos pos) throws IOException {
        RegionFile lv = this.getRegionFile(pos);
        try (DataInputStream dataInputStream = lv.getChunkInputStream(pos);){
            if (dataInputStream == null) {
                NbtCompound nbtCompound = null;
                return nbtCompound;
            }
            NbtCompound nbtCompound = NbtIo.read(dataInputStream);
            return nbtCompound;
        }
    }

    public void scanChunk(ChunkPos chunkPos, NbtScanner scanner) throws IOException {
        RegionFile lv = this.getRegionFile(chunkPos);
        try (DataInputStream dataInputStream = lv.getChunkInputStream(chunkPos);){
            if (dataInputStream != null) {
                NbtIo.scan(dataInputStream, scanner);
            }
        }
    }

    protected void write(ChunkPos pos, @Nullable NbtCompound nbt) throws IOException {
        RegionFile lv = this.getRegionFile(pos);
        if (nbt == null) {
            lv.delete(pos);
        } else {
            try (DataOutputStream dataOutputStream = lv.getChunkOutputStream(pos);){
                NbtIo.write(nbt, (DataOutput)dataOutputStream);
            }
        }
    }

    @Override
    public void close() throws IOException {
        ThrowableDeliverer<IOException> lv = new ThrowableDeliverer<IOException>();
        for (RegionFile lv2 : this.cachedRegionFiles.values()) {
            try {
                lv2.close();
            }
            catch (IOException iOException) {
                lv.add(iOException);
            }
        }
        lv.deliver();
    }

    public void sync() throws IOException {
        for (RegionFile lv : this.cachedRegionFiles.values()) {
            lv.sync();
        }
    }
}

