/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.command;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;

public class DataCommandStorage {
    private static final String COMMAND_STORAGE_PREFIX = "command_storage_";
    private final Map<String, PersistentState> storages = Maps.newHashMap();
    private final PersistentStateManager stateManager;

    public DataCommandStorage(PersistentStateManager stateManager) {
        this.stateManager = stateManager;
    }

    private PersistentState createStorage(String namespace) {
        PersistentState lv = new PersistentState();
        this.storages.put(namespace, lv);
        return lv;
    }

    public NbtCompound get(Identifier id) {
        String string = id.getNamespace();
        PersistentState lv = this.stateManager.get(data -> this.createStorage(string).readNbt((NbtCompound)data), DataCommandStorage.getSaveKey(string));
        return lv != null ? lv.get(id.getPath()) : new NbtCompound();
    }

    public void set(Identifier id, NbtCompound nbt) {
        String string = id.getNamespace();
        this.stateManager.getOrCreate(data -> this.createStorage(string).readNbt((NbtCompound)data), () -> this.createStorage(string), DataCommandStorage.getSaveKey(string)).set(id.getPath(), nbt);
    }

    public Stream<Identifier> getIds() {
        return this.storages.entrySet().stream().flatMap(entry -> ((PersistentState)entry.getValue()).getIds((String)entry.getKey()));
    }

    private static String getSaveKey(String namespace) {
        return COMMAND_STORAGE_PREFIX + namespace;
    }

    static class PersistentState
    extends net.minecraft.world.PersistentState {
        private static final String CONTENTS_KEY = "contents";
        private final Map<String, NbtCompound> map = Maps.newHashMap();

        PersistentState() {
        }

        PersistentState readNbt(NbtCompound nbt) {
            NbtCompound lv = nbt.getCompound(CONTENTS_KEY);
            for (String string : lv.getKeys()) {
                this.map.put(string, lv.getCompound(string));
            }
            return this;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            NbtCompound lv = new NbtCompound();
            this.map.forEach((key, value) -> lv.put((String)key, value.copy()));
            nbt.put(CONTENTS_KEY, lv);
            return nbt;
        }

        public NbtCompound get(String name) {
            NbtCompound lv = this.map.get(name);
            return lv != null ? lv : new NbtCompound();
        }

        public void set(String name, NbtCompound nbt) {
            if (nbt.isEmpty()) {
                this.map.remove(name);
            } else {
                this.map.put(name, nbt);
            }
            this.markDirty();
        }

        public Stream<Identifier> getIds(String namespace) {
            return this.map.keySet().stream().map(key -> new Identifier(namespace, (String)key));
        }
    }
}

