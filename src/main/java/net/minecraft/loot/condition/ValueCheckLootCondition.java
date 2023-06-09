/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class ValueCheckLootCondition
implements LootCondition {
    final LootNumberProvider value;
    final BoundedIntUnaryOperator range;

    ValueCheckLootCondition(LootNumberProvider value, BoundedIntUnaryOperator range) {
        this.value = value;
        this.range = range;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.VALUE_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Sets.union(this.value.getRequiredParameters(), this.range.getRequiredParameters());
    }

    @Override
    public boolean test(LootContext arg) {
        return this.range.test(arg, this.value.nextInt(arg));
    }

    public static LootCondition.Builder builder(LootNumberProvider value, BoundedIntUnaryOperator range) {
        return () -> new ValueCheckLootCondition(value, range);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Serializer
    implements JsonSerializer<ValueCheckLootCondition> {
        @Override
        public void toJson(JsonObject jsonObject, ValueCheckLootCondition arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("value", jsonSerializationContext.serialize(arg.value));
            jsonObject.add("range", jsonSerializationContext.serialize(arg.range));
        }

        @Override
        public ValueCheckLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootNumberProvider lv = JsonHelper.deserialize(jsonObject, "value", jsonDeserializationContext, LootNumberProvider.class);
            BoundedIntUnaryOperator lv2 = JsonHelper.deserialize(jsonObject, "range", jsonDeserializationContext, BoundedIntUnaryOperator.class);
            return new ValueCheckLootCondition(lv, lv2);
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

