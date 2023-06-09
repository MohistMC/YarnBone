/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.structure.ShipwreckGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class ShipwreckStructure
extends Structure {
    public static final Codec<ShipwreckStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(ShipwreckStructure.configCodecBuilder(instance), ((MapCodec)Codec.BOOL.fieldOf("is_beached")).forGetter(arg -> arg.beached)).apply((Applicative<ShipwreckStructure, ?>)instance, ShipwreckStructure::new));
    public final boolean beached;

    public ShipwreckStructure(Structure.Config config, boolean beached) {
        super(config);
        this.beached = beached;
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        Heightmap.Type lv = this.beached ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.OCEAN_FLOOR_WG;
        return ShipwreckStructure.getStructurePosition(context, lv, collector -> this.addPieces((StructurePiecesCollector)collector, context));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        BlockRotation lv = BlockRotation.random(context.random());
        BlockPos lv2 = new BlockPos(context.chunkPos().getStartX(), 90, context.chunkPos().getStartZ());
        ShipwreckGenerator.addParts(context.structureTemplateManager(), lv2, lv, collector, context.random(), this.beached);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.SHIPWRECK;
    }
}

