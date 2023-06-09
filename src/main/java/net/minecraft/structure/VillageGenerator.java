/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.structure;

import net.minecraft.registry.Registerable;
import net.minecraft.structure.DesertVillageData;
import net.minecraft.structure.PlainsVillageData;
import net.minecraft.structure.SavannaVillageData;
import net.minecraft.structure.SnowyVillageData;
import net.minecraft.structure.TaigaVillageData;
import net.minecraft.structure.pool.StructurePool;

public class VillageGenerator {
    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        PlainsVillageData.bootstrap(poolRegisterable);
        SnowyVillageData.bootstrap(poolRegisterable);
        SavannaVillageData.bootstrap(poolRegisterable);
        DesertVillageData.bootstrap(poolRegisterable);
        TaigaVillageData.bootstrap(poolRegisterable);
    }
}

