/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.structure.pool;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;

public class LegacySinglePoolElement
extends SinglePoolElement {
    public static final Codec<LegacySinglePoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(LegacySinglePoolElement.locationGetter(), LegacySinglePoolElement.processorsGetter(), LegacySinglePoolElement.projectionGetter()).apply(instance, LegacySinglePoolElement::new));

    protected LegacySinglePoolElement(Either<Identifier, StructureTemplate> either, RegistryEntry<StructureProcessorList> arg, StructurePool.Projection arg2) {
        super(either, arg, arg2);
    }

    @Override
    protected StructurePlacementData createPlacementData(BlockRotation rotation, BlockBox box, boolean keepJigsaws) {
        StructurePlacementData lv = super.createPlacementData(rotation, box, keepJigsaws);
        lv.removeProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
        lv.addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);
        return lv;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT;
    }

    @Override
    public String toString() {
        return "LegacySingle[" + this.location + "]";
    }
}

