/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class TagEntry
extends LeafEntry {
    final TagKey<Item> name;
    final boolean expand;

    TagEntry(TagKey<Item> name, boolean expand, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.name = name;
        this.expand = expand;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.TAG;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        Registries.ITEM.iterateEntries(this.name).forEach(entry -> lootConsumer.accept(new ItemStack((RegistryEntry<Item>)entry)));
    }

    private boolean grow(LootContext context, Consumer<LootChoice> lootChoiceExpander) {
        if (this.test(context)) {
            for (final RegistryEntry<Item> lv : Registries.ITEM.iterateEntries(this.name)) {
                lootChoiceExpander.accept(new LeafEntry.Choice(){

                    @Override
                    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
                        lootConsumer.accept(new ItemStack(lv));
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean expand(LootContext arg, Consumer<LootChoice> consumer) {
        if (this.expand) {
            return this.grow(arg, consumer);
        }
        return super.expand(arg, consumer);
    }

    public static LeafEntry.Builder<?> builder(TagKey<Item> name) {
        return TagEntry.builder((int weight, int quality, LootCondition[] conditions, LootFunction[] functions) -> new TagEntry(name, false, weight, quality, conditions, functions));
    }

    public static LeafEntry.Builder<?> expandBuilder(TagKey<Item> name) {
        return TagEntry.builder((int weight, int quality, LootCondition[] conditions, LootFunction[] functions) -> new TagEntry(name, true, weight, quality, conditions, functions));
    }

    public static class Serializer
    extends LeafEntry.Serializer<TagEntry> {
        @Override
        public void addEntryFields(JsonObject jsonObject, TagEntry arg, JsonSerializationContext jsonSerializationContext) {
            super.addEntryFields(jsonObject, arg, jsonSerializationContext);
            jsonObject.addProperty("name", arg.name.id().toString());
            jsonObject.addProperty("expand", arg.expand);
        }

        @Override
        protected TagEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] args, LootFunction[] args2) {
            Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "name"));
            TagKey<Item> lv2 = TagKey.of(RegistryKeys.ITEM, lv);
            boolean bl = JsonHelper.getBoolean(jsonObject, "expand");
            return new TagEntry(lv2, bl, i, j, args, args2);
        }

        @Override
        protected /* synthetic */ LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            return this.fromJson(entryJson, context, weight, quality, conditions, functions);
        }
    }
}

