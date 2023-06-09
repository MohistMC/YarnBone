/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ItemEntry
extends LeafEntry {
    final Item item;

    ItemEntry(Item item, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.item = item;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.ITEM;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        lootConsumer.accept(new ItemStack(this.item));
    }

    public static LeafEntry.Builder<?> builder(ItemConvertible drop) {
        return ItemEntry.builder((int weight, int quality, LootCondition[] conditions, LootFunction[] functions) -> new ItemEntry(drop.asItem(), weight, quality, conditions, functions));
    }

    public static class Serializer
    extends LeafEntry.Serializer<ItemEntry> {
        @Override
        public void addEntryFields(JsonObject jsonObject, ItemEntry arg, JsonSerializationContext jsonSerializationContext) {
            super.addEntryFields(jsonObject, arg, jsonSerializationContext);
            Identifier lv = Registries.ITEM.getId(arg.item);
            if (lv == null) {
                throw new IllegalArgumentException("Can't serialize unknown item " + arg.item);
            }
            jsonObject.addProperty("name", lv.toString());
        }

        @Override
        protected ItemEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] args, LootFunction[] args2) {
            Item lv = JsonHelper.getItem(jsonObject, "name");
            return new ItemEntry(lv, i, j, args, args2);
        }

        @Override
        protected /* synthetic */ LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            return this.fromJson(entryJson, context, weight, quality, conditions, functions);
        }
    }
}

