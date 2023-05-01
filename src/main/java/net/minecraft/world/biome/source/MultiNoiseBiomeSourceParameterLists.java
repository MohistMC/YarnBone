/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.biome.source;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;

public class MultiNoiseBiomeSourceParameterLists {
    public static final RegistryKey<MultiNoiseBiomeSourceParameterList> NETHER = MultiNoiseBiomeSourceParameterLists.of("nether");
    public static final RegistryKey<MultiNoiseBiomeSourceParameterList> OVERWORLD = MultiNoiseBiomeSourceParameterLists.of("overworld");

    public static void bootstrap(Registerable<MultiNoiseBiomeSourceParameterList> registry) {
        RegistryEntryLookup<Biome> lv = registry.getRegistryLookup(RegistryKeys.BIOME);
        registry.register(NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, lv));
        registry.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, lv));
    }

    public static void bootstrapOneTwenty(Registerable<MultiNoiseBiomeSourceParameterList> registry) {
        RegistryEntryLookup<Biome> lv = registry.getRegistryLookup(RegistryKeys.BIOME);
        registry.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD_UPDATE_1_20, lv));
    }

    private static RegistryKey<MultiNoiseBiomeSourceParameterList> of(String id) {
        return RegistryKey.of(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, new Identifier(id));
    }
}

