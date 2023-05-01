/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class TableBonusLootCondition
implements LootCondition {
    final Enchantment enchantment;
    final float[] chances;

    TableBonusLootCondition(Enchantment enchantment, float[] chances) {
        this.enchantment = enchantment;
        this.chances = chances;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.TABLE_BONUS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.TOOL);
    }

    @Override
    public boolean test(LootContext arg) {
        ItemStack lv = arg.get(LootContextParameters.TOOL);
        int i = lv != null ? EnchantmentHelper.getLevel(this.enchantment, lv) : 0;
        float f = this.chances[Math.min(i, this.chances.length - 1)];
        return arg.getRandom().nextFloat() < f;
    }

    public static LootCondition.Builder builder(Enchantment enchantment, float ... chances) {
        return () -> new TableBonusLootCondition(enchantment, chances);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Serializer
    implements JsonSerializer<TableBonusLootCondition> {
        @Override
        public void toJson(JsonObject jsonObject, TableBonusLootCondition arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("enchantment", Registries.ENCHANTMENT.getId(arg.enchantment).toString());
            jsonObject.add("chances", jsonSerializationContext.serialize(arg.chances));
        }

        @Override
        public TableBonusLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "enchantment"));
            Enchantment lv2 = Registries.ENCHANTMENT.getOrEmpty(lv).orElseThrow(() -> new JsonParseException("Invalid enchantment id: " + lv));
            float[] fs = JsonHelper.deserialize(jsonObject, "chances", jsonDeserializationContext, float[].class);
            return new TableBonusLootCondition(lv2, fs);
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

