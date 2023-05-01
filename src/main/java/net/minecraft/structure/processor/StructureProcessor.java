/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure.processor;

import java.util.List;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class StructureProcessor {
    @Nullable
    public abstract StructureTemplate.StructureBlockInfo process(WorldView var1, BlockPos var2, BlockPos var3, StructureTemplate.StructureBlockInfo var4, StructureTemplate.StructureBlockInfo var5, StructurePlacementData var6);

    protected abstract StructureProcessorType<?> getType();

    public void reprocess(WorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, List<StructureTemplate.StructureBlockInfo> processedOnFirstRound) {
    }
}

