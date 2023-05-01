/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.JigsawReplacementStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SinglePoolElement
extends StructurePoolElement {
    private static final Codec<Either<Identifier, StructureTemplate>> LOCATION_CODEC = Codec.of(SinglePoolElement::encodeLocation, Identifier.CODEC.map(Either::left));
    public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(SinglePoolElement.locationGetter(), SinglePoolElement.processorsGetter(), SinglePoolElement.projectionGetter()).apply((Applicative)instance, SinglePoolElement::new));
    protected final Either<Identifier, StructureTemplate> location;
    protected final RegistryEntry<StructureProcessorList> processors;

    private static <T> DataResult<T> encodeLocation(Either<Identifier, StructureTemplate> location, DynamicOps<T> ops, T prefix) {
        Optional<Identifier> optional = location.left();
        if (!optional.isPresent()) {
            return DataResult.error(() -> "Can not serialize a runtime pool element");
        }
        return Identifier.CODEC.encode(optional.get(), ops, prefix);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, RegistryEntry<StructureProcessorList>> processorsGetter() {
        return ((MapCodec)StructureProcessorType.REGISTRY_CODEC.fieldOf("processors")).forGetter(pool -> pool.processors);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<Identifier, StructureTemplate>> locationGetter() {
        return ((MapCodec)LOCATION_CODEC.fieldOf("location")).forGetter(pool -> pool.location);
    }

    protected SinglePoolElement(Either<Identifier, StructureTemplate> location, RegistryEntry<StructureProcessorList> processors, StructurePool.Projection projection) {
        super(projection);
        this.location = location;
        this.processors = processors;
    }

    @Override
    public Vec3i getStart(StructureTemplateManager structureTemplateManager, BlockRotation rotation) {
        StructureTemplate lv = this.getStructure(structureTemplateManager);
        return lv.getRotatedSize(rotation);
    }

    private StructureTemplate getStructure(StructureTemplateManager structureTemplateManager) {
        return this.location.map(structureTemplateManager::getTemplateOrBlank, Function.identity());
    }

    public List<StructureTemplate.StructureBlockInfo> getDataStructureBlocks(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, boolean mirroredAndRotated) {
        StructureTemplate lv = this.getStructure(structureTemplateManager);
        ObjectArrayList<StructureTemplate.StructureBlockInfo> list = lv.getInfosForBlock(pos, new StructurePlacementData().setRotation(rotation), Blocks.STRUCTURE_BLOCK, mirroredAndRotated);
        ArrayList<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();
        for (StructureTemplate.StructureBlockInfo lv2 : list) {
            StructureBlockMode lv3;
            if (lv2.nbt == null || (lv3 = StructureBlockMode.valueOf(lv2.nbt.getString("mode"))) != StructureBlockMode.DATA) continue;
            list2.add(lv2);
        }
        return list2;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getStructureBlockInfos(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, Random random) {
        StructureTemplate lv = this.getStructure(structureTemplateManager);
        ObjectArrayList<StructureTemplate.StructureBlockInfo> objectArrayList = lv.getInfosForBlock(pos, new StructurePlacementData().setRotation(rotation), Blocks.JIGSAW, true);
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    @Override
    public BlockBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation) {
        StructureTemplate lv = this.getStructure(structureTemplateManager);
        return lv.calculateBoundingBox(new StructurePlacementData().setRotation(rotation), pos);
    }

    @Override
    public boolean generate(StructureTemplateManager structureTemplateManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos pivot, BlockRotation rotation, BlockBox box, Random random, boolean keepJigsaws) {
        StructurePlacementData lv2;
        StructureTemplate lv = this.getStructure(structureTemplateManager);
        if (lv.place(world, pos, pivot, lv2 = this.createPlacementData(rotation, box, keepJigsaws), random, 18)) {
            List<StructureTemplate.StructureBlockInfo> list = StructureTemplate.process(world, pos, pivot, lv2, this.getDataStructureBlocks(structureTemplateManager, pos, rotation, false));
            for (StructureTemplate.StructureBlockInfo lv3 : list) {
                this.method_16756(world, lv3, pos, rotation, random, box);
            }
            return true;
        }
        return false;
    }

    protected StructurePlacementData createPlacementData(BlockRotation rotation, BlockBox box, boolean keepJigsaws) {
        StructurePlacementData lv = new StructurePlacementData();
        lv.setBoundingBox(box);
        lv.setRotation(rotation);
        lv.setUpdateNeighbors(true);
        lv.setIgnoreEntities(false);
        lv.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
        lv.setInitializeMobs(true);
        if (!keepJigsaws) {
            lv.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
        }
        this.processors.value().getList().forEach(lv::addProcessor);
        this.getProjection().getProcessors().forEach(lv::addProcessor);
        return lv;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.SINGLE_POOL_ELEMENT;
    }

    public String toString() {
        return "Single[" + this.location + "]";
    }
}

