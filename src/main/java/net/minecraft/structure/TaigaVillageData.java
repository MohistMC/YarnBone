/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VillagePlacedFeatures;

public class TaigaVillageData {
    public static final RegistryKey<StructurePool> TOWN_CENTERS_KEY = StructurePools.of("village/taiga/town_centers");
    private static final RegistryKey<StructurePool> TERMINATORS_KEY = StructurePools.of("village/taiga/terminators");

    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<PlacedFeature> lv = poolRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntry.Reference<PlacedFeature> lv2 = lv.getOrThrow(VillagePlacedFeatures.SPRUCE);
        RegistryEntry.Reference<PlacedFeature> lv3 = lv.getOrThrow(VillagePlacedFeatures.PINE);
        RegistryEntry.Reference<PlacedFeature> lv4 = lv.getOrThrow(VillagePlacedFeatures.PILE_PUMPKIN);
        RegistryEntry.Reference<PlacedFeature> lv5 = lv.getOrThrow(VillagePlacedFeatures.PATCH_TAIGA_GRASS);
        RegistryEntry.Reference<PlacedFeature> lv6 = lv.getOrThrow(VillagePlacedFeatures.PATCH_BERRY_BUSH);
        RegistryEntryLookup<StructureProcessorList> lv7 = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> lv8 = lv7.getOrThrow(StructureProcessorLists.MOSSIFY_10_PERCENT);
        RegistryEntry.Reference<StructureProcessorList> lv9 = lv7.getOrThrow(StructureProcessorLists.ZOMBIE_TAIGA);
        RegistryEntry.Reference<StructureProcessorList> lv10 = lv7.getOrThrow(StructureProcessorLists.STREET_SNOWY_OR_TAIGA);
        RegistryEntry.Reference<StructureProcessorList> lv11 = lv7.getOrThrow(StructureProcessorLists.FARM_TAIGA);
        RegistryEntryLookup<StructurePool> lv12 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv13 = lv12.getOrThrow(StructurePools.EMPTY);
        RegistryEntry.Reference<StructurePool> lv14 = lv12.getOrThrow(TERMINATORS_KEY);
        poolRegisterable.register(TOWN_CENTERS_KEY, new StructurePool(lv13, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/town_centers/taiga_meeting_point_1", lv8), 49), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/town_centers/taiga_meeting_point_2", lv8), 49), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/town_centers/taiga_meeting_point_1", lv9), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/town_centers/taiga_meeting_point_2", lv9), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/taiga/streets", new StructurePool(lv14, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/corner_01", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/corner_02", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/corner_03", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/straight_01", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/straight_02", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/straight_03", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/straight_04", lv10), 7), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/straight_05", lv10), 7), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/straight_06", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/crossroad_01", lv10), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/crossroad_02", lv10), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/crossroad_03", lv10), 2), new Pair[]{Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/crossroad_04", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/crossroad_05", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/crossroad_06", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/streets/turn_01", lv10), 3)}), StructurePool.Projection.TERRAIN_MATCHING));
        StructurePools.register(poolRegisterable, "village/taiga/zombie/streets", new StructurePool(lv14, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/corner_01", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/corner_02", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/corner_03", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/straight_01", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/straight_02", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/straight_03", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/straight_04", lv10), 7), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/straight_05", lv10), 7), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/straight_06", lv10), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/crossroad_01", lv10), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/crossroad_02", lv10), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/crossroad_03", lv10), 2), new Pair[]{Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/crossroad_04", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/crossroad_05", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/crossroad_06", lv10), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/streets/turn_01", lv10), 3)}), StructurePool.Projection.TERRAIN_MATCHING));
        StructurePools.register(poolRegisterable, "village/taiga/houses", new StructurePool(lv14, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_small_house_1", lv8), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_small_house_2", lv8), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_small_house_3", lv8), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_small_house_4", lv8), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_small_house_5", lv8), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_medium_house_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_medium_house_2", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_medium_house_3", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_medium_house_4", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_butcher_shop_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_tool_smith_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_fletcher_house_1", lv8), 2), new Pair[]{Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_shepherds_house_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_armorer_house_1", lv8), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_armorer_2", lv8), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_fisher_cottage_1", lv8), 3), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_tannery_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_cartographer_house_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_library_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_masons_house_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_weaponsmith_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_weaponsmith_2", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_temple_1", lv8), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_large_farm_1", lv11), 6), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_large_farm_2", lv11), 6), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_small_farm_1", lv8), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_animal_pen_1", lv8), 2), Pair.of(StructurePoolElement.ofEmpty(), 6)}), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/taiga/zombie/houses", new StructurePool(lv14, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_small_house_1", lv9), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_small_house_2", lv9), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_small_house_3", lv9), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_small_house_4", lv9), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_small_house_5", lv9), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_medium_house_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_medium_house_2", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_medium_house_3", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_medium_house_4", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_butcher_shop_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_tool_smith_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_fletcher_house_1", lv9), 2), new Pair[]{Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_shepherds_house_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_armorer_house_1", lv9), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_fisher_cottage_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_tannery_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_cartographer_house_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_library_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_masons_house_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_weaponsmith_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_weaponsmith_2", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_temple_1", lv9), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_large_farm_1", lv9), 6), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/zombie/houses/taiga_large_farm_2", lv9), 6), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_small_farm_1", lv9), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/taiga/houses/taiga_animal_pen_1", lv9), 2), Pair.of(StructurePoolElement.ofEmpty(), 6)}), StructurePool.Projection.RIGID));
        poolRegisterable.register(TERMINATORS_KEY, new StructurePool(lv13, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_01", lv10), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_02", lv10), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_03", lv10), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_04", lv10), 1)), StructurePool.Projection.TERRAIN_MATCHING));
        StructurePools.register(poolRegisterable, "village/taiga/decor", new StructurePool(lv13, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_lamp_post_1"), 10), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_1"), 4), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_2"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_3"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_4"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_5"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_6"), 1), Pair.of(StructurePoolElement.ofFeature(lv2), 4), Pair.of(StructurePoolElement.ofFeature(lv3), 4), Pair.of(StructurePoolElement.ofFeature(lv4), 2), Pair.of(StructurePoolElement.ofFeature(lv5), 4), Pair.of(StructurePoolElement.ofFeature(lv6), 1), new Pair[]{Pair.of(StructurePoolElement.ofEmpty(), 4)}), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/taiga/zombie/decor", new StructurePool(lv13, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_1"), 4), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_2"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_3"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/taiga_decoration_4"), 1), Pair.of(StructurePoolElement.ofFeature(lv2), 4), Pair.of(StructurePoolElement.ofFeature(lv3), 4), Pair.of(StructurePoolElement.ofFeature(lv4), 2), Pair.of(StructurePoolElement.ofFeature(lv5), 4), Pair.of(StructurePoolElement.ofFeature(lv6), 1), Pair.of(StructurePoolElement.ofEmpty(), 4)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/taiga/villagers", new StructurePool(lv13, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/villagers/nitwit"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/villagers/baby"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/villagers/unemployed"), 10)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/taiga/zombie/villagers", new StructurePool(lv13, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/zombie/villagers/nitwit"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/taiga/zombie/villagers/unemployed"), 10)), StructurePool.Projection.RIGID));
    }
}

