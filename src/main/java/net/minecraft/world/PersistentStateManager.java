/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PersistentStateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, PersistentState> loadedStates = Maps.newHashMap();
    private final DataFixer dataFixer;
    private final File directory;

    public PersistentStateManager(File directory, DataFixer dataFixer) {
        this.dataFixer = dataFixer;
        this.directory = directory;
    }

    private File getFile(String id) {
        return new File(this.directory, id + ".dat");
    }

    public <T extends PersistentState> T getOrCreate(Function<NbtCompound, T> readFunction, Supplier<T> supplier, String id) {
        T lv = this.get(readFunction, id);
        if (lv != null) {
            return lv;
        }
        PersistentState lv2 = (PersistentState)supplier.get();
        this.set(id, lv2);
        return (T)lv2;
    }

    @Nullable
    public <T extends PersistentState> T get(Function<NbtCompound, T> readFunction, String id) {
        PersistentState lv = this.loadedStates.get(id);
        if (lv == null && !this.loadedStates.containsKey(id)) {
            lv = this.readFromFile(readFunction, id);
            this.loadedStates.put(id, lv);
        }
        return (T)lv;
    }

    @Nullable
    private <T extends PersistentState> T readFromFile(Function<NbtCompound, T> readFunction, String id) {
        try {
            File file = this.getFile(id);
            if (file.exists()) {
                NbtCompound lv = this.readNbt(id, SharedConstants.getGameVersion().getSaveVersion().getId());
                return (T)((PersistentState)readFunction.apply(lv.getCompound("data")));
            }
        }
        catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", (Object)id, (Object)exception);
        }
        return null;
    }

    public void set(String id, PersistentState state) {
        this.loadedStates.put(id, state);
    }

    public NbtCompound readNbt(String id, int dataVersion) throws IOException {
        File file = this.getFile(id);
        try (FileInputStream fileInputStream = new FileInputStream(file);){
            NbtCompound nbtCompound;
            try (PushbackInputStream pushbackInputStream = new PushbackInputStream(fileInputStream, 2);){
                NbtCompound lv;
                if (this.isCompressed(pushbackInputStream)) {
                    lv = NbtIo.readCompressed(pushbackInputStream);
                } else {
                    try (DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);){
                        lv = NbtIo.read(dataInputStream);
                    }
                }
                int j = NbtHelper.getDataVersion(lv, 1343);
                nbtCompound = DataFixTypes.SAVED_DATA.update(this.dataFixer, lv, j, dataVersion);
            }
            return nbtCompound;
        }
    }

    private boolean isCompressed(PushbackInputStream stream) throws IOException {
        int j;
        byte[] bs = new byte[2];
        boolean bl = false;
        int i = stream.read(bs, 0, 2);
        if (i == 2 && (j = (bs[1] & 0xFF) << 8 | bs[0] & 0xFF) == 35615) {
            bl = true;
        }
        if (i != 0) {
            stream.unread(bs, 0, i);
        }
        return bl;
    }

    public void save() {
        this.loadedStates.forEach((id, state) -> {
            if (state != null) {
                state.save(this.getFile((String)id));
            }
        });
    }
}

