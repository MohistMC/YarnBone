/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;

public class HangingSignBlockEntity
extends SignBlockEntity {
    private static final int MAX_TEXT_WIDTH = 50;
    private static final int TEXT_LINE_HEIGHT = 9;

    public HangingSignBlockEntity(BlockPos arg, BlockState arg2) {
        super((BlockEntityType)BlockEntityType.HANGING_SIGN, arg, arg2);
    }

    @Override
    public int getTextLineHeight() {
        return 9;
    }

    @Override
    public int getMaxTextWidth() {
        return 50;
    }
}

