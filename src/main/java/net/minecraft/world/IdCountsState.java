/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.PersistentState;

public class IdCountsState
extends PersistentState {
    public static final String IDCOUNTS_KEY = "idcounts";
    private final Object2IntMap<String> idCounts = new Object2IntOpenHashMap<String>();

    public IdCountsState() {
        this.idCounts.defaultReturnValue(-1);
    }

    public static IdCountsState fromNbt(NbtCompound nbt) {
        IdCountsState lv = new IdCountsState();
        for (String string : nbt.getKeys()) {
            if (!nbt.contains(string, NbtElement.NUMBER_TYPE)) continue;
            lv.idCounts.put(string, nbt.getInt(string));
        }
        return lv;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (Object2IntMap.Entry entry : this.idCounts.object2IntEntrySet()) {
            nbt.putInt((String)entry.getKey(), entry.getIntValue());
        }
        return nbt;
    }

    public int getNextMapId() {
        int i = this.idCounts.getInt("map") + 1;
        this.idCounts.put("map", i);
        this.markDirty();
        return i;
    }
}

