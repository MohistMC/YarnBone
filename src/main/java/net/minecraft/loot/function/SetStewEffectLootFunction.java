/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;

public class SetStewEffectLootFunction
extends ConditionalLootFunction {
    final Map<StatusEffect, LootNumberProvider> effects;

    SetStewEffectLootFunction(LootCondition[] conditions, Map<StatusEffect, LootNumberProvider> effects) {
        super(conditions);
        this.effects = ImmutableMap.copyOf(effects);
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_STEW_EFFECT;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.effects.values().stream().flatMap(numberProvider -> numberProvider.getRequiredParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (!stack.isOf(Items.SUSPICIOUS_STEW) || this.effects.isEmpty()) {
            return stack;
        }
        Random lv = context.getRandom();
        int i = lv.nextInt(this.effects.size());
        Map.Entry<StatusEffect, LootNumberProvider> entry = Iterables.get(this.effects.entrySet(), i);
        StatusEffect lv2 = entry.getKey();
        int j = entry.getValue().nextInt(context);
        if (!lv2.isInstant()) {
            j *= 20;
        }
        SuspiciousStewItem.addEffectToStew(stack, lv2, j);
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final Map<StatusEffect, LootNumberProvider> map = Maps.newLinkedHashMap();

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder withEffect(StatusEffect effect, LootNumberProvider durationRange) {
            this.map.put(effect, durationRange);
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetStewEffectLootFunction(this.getConditions(), this.map);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetStewEffectLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetStewEffectLootFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, arg, jsonSerializationContext);
            if (!arg.effects.isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (StatusEffect lv : arg.effects.keySet()) {
                    JsonObject jsonObject2 = new JsonObject();
                    Identifier lv2 = Registries.STATUS_EFFECT.getId(lv);
                    if (lv2 == null) {
                        throw new IllegalArgumentException("Don't know how to serialize mob effect " + lv);
                    }
                    jsonObject2.add("type", new JsonPrimitive(lv2.toString()));
                    jsonObject2.add("duration", jsonSerializationContext.serialize(arg.effects.get(lv)));
                    jsonArray.add(jsonObject2);
                }
                jsonObject.add("effects", jsonArray);
            }
        }

        @Override
        public SetStewEffectLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            LinkedHashMap<StatusEffect, LootNumberProvider> map = Maps.newLinkedHashMap();
            if (jsonObject.has("effects")) {
                JsonArray jsonArray = JsonHelper.getArray(jsonObject, "effects");
                for (JsonElement jsonElement : jsonArray) {
                    String string = JsonHelper.getString(jsonElement.getAsJsonObject(), "type");
                    StatusEffect lv = Registries.STATUS_EFFECT.getOrEmpty(new Identifier(string)).orElseThrow(() -> new JsonSyntaxException("Unknown mob effect '" + string + "'"));
                    LootNumberProvider lv2 = JsonHelper.deserialize(jsonElement.getAsJsonObject(), "duration", jsonDeserializationContext, LootNumberProvider.class);
                    map.put(lv, lv2);
                }
            }
            return new SetStewEffectLootFunction(args, map);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

