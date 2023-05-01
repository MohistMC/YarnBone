/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.loot.condition;

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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.jetbrains.annotations.Nullable;

public class TimeCheckLootCondition
implements LootCondition {
    @Nullable
    final Long period;
    final BoundedIntUnaryOperator value;

    TimeCheckLootCondition(@Nullable Long period, BoundedIntUnaryOperator value) {
        this.period = period;
        this.value = value;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.TIME_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.value.getRequiredParameters();
    }

    @Override
    public boolean test(LootContext arg) {
        ServerWorld lv = arg.getWorld();
        long l = lv.getTimeOfDay();
        if (this.period != null) {
            l %= this.period.longValue();
        }
        return this.value.test(arg, (int)l);
    }

    public static Builder create(BoundedIntUnaryOperator value) {
        return new Builder(value);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        @Nullable
        private Long period;
        private final BoundedIntUnaryOperator value;

        public Builder(BoundedIntUnaryOperator value) {
            this.value = value;
        }

        public Builder period(long period) {
            this.period = period;
            return this;
        }

        @Override
        public TimeCheckLootCondition build() {
            return new TimeCheckLootCondition(this.period, this.value);
        }

        @Override
        public /* synthetic */ LootCondition build() {
            return this.build();
        }
    }

    public static class Serializer
    implements JsonSerializer<TimeCheckLootCondition> {
        @Override
        public void toJson(JsonObject jsonObject, TimeCheckLootCondition arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("period", arg.period);
            jsonObject.add("value", jsonSerializationContext.serialize(arg.value));
        }

        @Override
        public TimeCheckLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Long long_ = jsonObject.has("period") ? Long.valueOf(JsonHelper.getLong(jsonObject, "period")) : null;
            BoundedIntUnaryOperator lv = JsonHelper.deserialize(jsonObject, "value", jsonDeserializationContext, BoundedIntUnaryOperator.class);
            return new TimeCheckLootCondition(long_, lv);
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

