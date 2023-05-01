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
import net.minecraft.util.math.random.Random;

public final class BinomialLootNumberProvider
implements LootNumberProvider {
    final LootNumberProvider n;
    final LootNumberProvider p;

    BinomialLootNumberProvider(LootNumberProvider n, LootNumberProvider p) {
        this.n = n;
        this.p = p;
    }

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.BINOMIAL;
    }

    @Override
    public int nextInt(LootContext context) {
        int i = this.n.nextInt(context);
        float f = this.p.nextFloat(context);
        Random lv = context.getRandom();
        int j = 0;
        for (int k = 0; k < i; ++k) {
            if (!(lv.nextFloat() < f)) continue;
            ++j;
        }
        return j;
    }

    @Override
    public float nextFloat(LootContext context) {
        return this.nextInt(context);
    }

    public static BinomialLootNumberProvider create(int n, float p) {
        return new BinomialLootNumberProvider(ConstantLootNumberProvider.create(n), ConstantLootNumberProvider.create(p));
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Sets.union(this.n.getRequiredParameters(), this.p.getRequiredParameters());
    }

    public static class Serializer
    implements JsonSerializer<BinomialLootNumberProvider> {
        @Override
        public BinomialLootNumberProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootNumberProvider lv = JsonHelper.deserialize(jsonObject, "n", jsonDeserializationContext, LootNumberProvider.class);
            LootNumberProvider lv2 = JsonHelper.deserialize(jsonObject, "p", jsonDeserializationContext, LootNumberProvider.class);
            return new BinomialLootNumberProvider(lv, lv2);
        }

        @Override
        public void toJson(JsonObject jsonObject, BinomialLootNumberProvider arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("n", jsonSerializationContext.serialize(arg.n));
            jsonObject.add("p", jsonSerializationContext.serialize(arg.p));
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

