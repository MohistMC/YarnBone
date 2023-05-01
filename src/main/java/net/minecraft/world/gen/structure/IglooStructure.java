/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.structure.IglooGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class IglooStructure
extends Structure {
    public static final Codec<IglooStructure> CODEC = IglooStructure.createCodec(IglooStructure::new);

    public IglooStructure(Structure.Config arg) {
        super(arg);
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        return IglooStructure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> this.addPieces((StructurePiecesCollector)collector, context));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        ChunkPos lv = context.chunkPos();
        ChunkRandom lv2 = context.random();
        BlockPos lv3 = new BlockPos(lv.getStartX(), 90, lv.getStartZ());
        BlockRotation lv4 = BlockRotation.random(lv2);
        IglooGenerator.addPieces(context.structureTemplateManager(), lv3, lv4, collector, lv2);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.IGLOO;
    }
}

