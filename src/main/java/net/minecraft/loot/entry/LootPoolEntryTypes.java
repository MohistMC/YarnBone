/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.entry;

import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.CombinedEntry;
import net.minecraft.loot.entry.DynamicEntry;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.GroupEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.entry.SequenceEntry;
import net.minecraft.loot.entry.TagEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;

public class LootPoolEntryTypes {
    public static final LootPoolEntryType EMPTY = LootPoolEntryTypes.register("empty", new EmptyEntry.Serializer());
    public static final LootPoolEntryType ITEM = LootPoolEntryTypes.register("item", new ItemEntry.Serializer());
    public static final LootPoolEntryType LOOT_TABLE = LootPoolEntryTypes.register("loot_table", new LootTableEntry.Serializer());
    public static final LootPoolEntryType DYNAMIC = LootPoolEntryTypes.register("dynamic", new DynamicEntry.Serializer());
    public static final LootPoolEntryType TAG = LootPoolEntryTypes.register("tag", new TagEntry.Serializer());
    public static final LootPoolEntryType ALTERNATIVES = LootPoolEntryTypes.register("alternatives", CombinedEntry.createSerializer(AlternativeEntry::new));
    public static final LootPoolEntryType SEQUENCE = LootPoolEntryTypes.register("sequence", CombinedEntry.createSerializer(SequenceEntry::new));
    public static final LootPoolEntryType GROUP = LootPoolEntryTypes.register("group", CombinedEntry.createSerializer(GroupEntry::new));

    private static LootPoolEntryType register(String id, JsonSerializer<? extends LootPoolEntry> jsonSerializer) {
        return Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, new Identifier(id), new LootPoolEntryType(jsonSerializer));
    }

    public static Object createGsonSerializer() {
        return JsonSerializing.createSerializerBuilder(Registries.LOOT_POOL_ENTRY_TYPE, "entry", "type", LootPoolEntry::getType).build();
    }
}

