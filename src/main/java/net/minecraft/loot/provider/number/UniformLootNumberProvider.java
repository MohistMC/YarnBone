/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.provider.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.MathHelper;

public class UniformLootNumberProvider
implements LootNumberProvider {
    final LootNumberProvider min;
    final LootNumberProvider max;

    UniformLootNumberProvider(LootNumberProvider min, LootNumberProvider max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.UNIFORM;
    }

    public static UniformLootNumberProvider create(float min, float max) {
        return new UniformLootNumberProvider(ConstantLootNumberProvider.create(min), ConstantLootNumberProvider.create(max));
    }

    @Override
    public int nextInt(LootContext context) {
        return MathHelper.nextInt(context.getRandom(), this.min.nextInt(context), this.max.nextInt(context));
    }

    @Override
    public float nextFloat(LootContext context) {
        return MathHelper.nextFloat(context.getRandom(), this.min.nextFloat(context), this.max.nextFloat(context));
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Sets.union(this.min.getRequiredParameters(), this.max.getRequiredParameters());
    }

    public static class Serializer
    implements JsonSerializer<UniformLootNumberProvider> {
        @Override
        public UniformLootNumberProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootNumberProvider lv = JsonHelper.deserialize(jsonObject, "min", jsonDeserializationContext, LootNumberProvider.class);
            LootNumberProvider lv2 = JsonHelper.deserialize(jsonObject, "max", jsonDeserializationContext, LootNumberProvider.class);
            return new UniformLootNumberProvider(lv, lv2);
        }

        @Override
        public void toJson(JsonObject jsonObject, UniformLootNumberProvider arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("min", jsonSerializationContext.serialize(arg.min));
            jsonObject.add("max", jsonSerializationContext.serialize(arg.max));
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

