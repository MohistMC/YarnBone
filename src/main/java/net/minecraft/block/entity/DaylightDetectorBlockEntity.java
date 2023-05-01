/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class DaylightDetectorBlockEntity
extends BlockEntity {
    public DaylightDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.DAYLIGHT_DETECTOR, pos, state);
    }
}

