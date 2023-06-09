/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class TwistingVinesPlantBlock
extends AbstractPlantBlock {
    public static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public TwistingVinesPlantBlock(AbstractBlock.Settings arg) {
        super(arg, Direction.UP, SHAPE, false);
    }

    @Override
    protected AbstractPlantStemBlock getStem() {
        return (AbstractPlantStemBlock)Blocks.TWISTING_VINES;
    }
}

