/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.provider.number;

import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.ScoreLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;

public class LootNumberProviderTypes {
    public static final LootNumberProviderType CONSTANT = LootNumberProviderTypes.register("constant", new ConstantLootNumberProvider.Serializer());
    public static final LootNumberProviderType UNIFORM = LootNumberProviderTypes.register("uniform", new UniformLootNumberProvider.Serializer());
    public static final LootNumberProviderType BINOMIAL = LootNumberProviderTypes.register("binomial", new BinomialLootNumberProvider.Serializer());
    public static final LootNumberProviderType SCORE = LootNumberProviderTypes.register("score", new ScoreLootNumberProvider.Serializer());

    private static LootNumberProviderType register(String id, JsonSerializer<? extends LootNumberProvider> jsonSerializer) {
        return Registry.register(Registries.LOOT_NUMBER_PROVIDER_TYPE, new Identifier(id), new LootNumberProviderType(jsonSerializer));
    }

    public static Object createGsonSerializer() {
        return JsonSerializing.createSerializerBuilder(Registries.LOOT_NUMBER_PROVIDER_TYPE, "provider", "type", LootNumberProvider::getType).elementSerializer(CONSTANT, new ConstantLootNumberProvider.CustomSerializer()).defaultType(UNIFORM).build();
    }
}

