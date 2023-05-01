/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.entry;

import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.JsonSerializer;

public class LootPoolEntryType
extends JsonSerializableType<LootPoolEntry> {
    public LootPoolEntryType(JsonSerializer<? extends LootPoolEntry> arg) {
        super(arg);
    }
}

