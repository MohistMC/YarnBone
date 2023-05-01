/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.loot.OneTwentyLootTables;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.structure.DesertTempleGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.Util;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.BasicTempleStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class DesertPyramidStructure
extends BasicTempleStructure {
    public static final Codec<DesertPyramidStructure> CODEC = DesertPyramidStructure.createCodec(DesertPyramidStructure::new);

    public DesertPyramidStructure(Structure.Config arg) {
        super(DesertTempleGenerator::new, 21, 21, arg);
    }

    @Override
    public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
        if (!world.getEnabledFeatures().contains(FeatureFlags.UPDATE_1_20)) {
            return;
        }
        SortedArraySet set = SortedArraySet.create(Vec3i::compareTo);
        for (StructurePiece lv : pieces.pieces()) {
            if (!(lv instanceof DesertTempleGenerator)) continue;
            DesertTempleGenerator lv2 = (DesertTempleGenerator)lv;
            set.addAll(lv2.getPotentialSuspiciousSandPositions());
        }
        ObjectArrayList objectArrayList = new ObjectArrayList(set.stream().toList());
        Util.shuffle(objectArrayList, random);
        int i = Math.min(set.size(), random.nextBetweenExclusive(5, 8));
        for (BlockPos lv3 : objectArrayList) {
            if (i > 0) {
                --i;
                world.setBlockState(lv3, Blocks.SUSPICIOUS_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.getBlockEntity(lv3, BlockEntityType.SUSPICIOUS_SAND).ifPresent(blockEntity -> blockEntity.setLootTable(OneTwentyLootTables.DESERT_PYRAMID_ARCHAEOLOGY, lv3.asLong()));
                continue;
            }
            world.setBlockState(lv3, Blocks.SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.DESERT_PYRAMID;
    }
}

