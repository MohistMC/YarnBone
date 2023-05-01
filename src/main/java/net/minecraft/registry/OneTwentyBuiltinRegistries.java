/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry;

import java.util.concurrent.CompletableFuture;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;

public class OneTwentyBuiltinRegistries {
    private static final RegistryBuilder REGISTRY_BUILDER = new RegistryBuilder().addRegistry(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterials::oneTwentyBootstrap).addRegistry(RegistryKeys.TRIM_PATTERN, ArmorTrimPatterns::oneTwentyBootstrap).addRegistry(RegistryKeys.BIOME, BuiltinBiomes::bootstrapOneTwenty).addRegistry(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterLists::bootstrapOneTwenty);

    public static CompletableFuture<RegistryWrapper.WrapperLookup> createWrapperLookup(CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        return registriesFuture.thenApply(wrapperLookup -> {
            DynamicRegistryManager.Immutable lv = DynamicRegistryManager.of(Registries.REGISTRIES);
            RegistryWrapper.WrapperLookup lv2 = REGISTRY_BUILDER.createWrapperLookup(lv, (RegistryWrapper.WrapperLookup)wrapperLookup);
            BuiltinRegistries.validate(wrapperLookup.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE), lv2.getWrapperOrThrow(RegistryKeys.BIOME));
            return lv2;
        });
    }
}

