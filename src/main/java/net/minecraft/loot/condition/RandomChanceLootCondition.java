/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class RandomChanceLootCondition
implements LootCondition {
    final float chance;

    RandomChanceLootCondition(float chance) {
        this.chance = chance;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.RANDOM_CHANCE;
    }

    @Override
    public boolean test(LootContext arg) {
        return arg.getRandom().nextFloat() < this.chance;
    }

    public static LootCondition.Builder builder(float chance) {
        return () -> new RandomChanceLootCondition(chance);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Serializer
    implements JsonSerializer<RandomChanceLootCondition> {
        @Override
        public void toJson(JsonObject jsonObject, RandomChanceLootCondition arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("chance", Float.valueOf(arg.chance));
        }

        @Override
        public RandomChanceLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new RandomChanceLootCondition(JsonHelper.getFloat(jsonObject, "chance"));
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

