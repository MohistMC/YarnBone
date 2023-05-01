/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure.processor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class JigsawReplacementStructureProcessor
extends StructureProcessor {
    public static final Codec<JigsawReplacementStructureProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static final JigsawReplacementStructureProcessor INSTANCE = new JigsawReplacementStructureProcessor();

    private JigsawReplacementStructureProcessor() {
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        BlockState lv3;
        BlockState lv = currentBlockInfo.state;
        if (!lv.isOf(Blocks.JIGSAW)) {
            return currentBlockInfo;
        }
        String string = currentBlockInfo.nbt.getString("final_state");
        try {
            BlockArgumentParser.BlockResult lv2 = BlockArgumentParser.block(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), string, true);
            lv3 = lv2.blockState();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            throw new RuntimeException(commandSyntaxException);
        }
        if (lv3.isOf(Blocks.STRUCTURE_VOID)) {
            return null;
        }
        return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos, lv3, null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}

