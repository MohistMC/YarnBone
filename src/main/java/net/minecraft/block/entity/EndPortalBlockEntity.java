/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EndPortalBlockEntity
extends BlockEntity {
    protected EndPortalBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    public EndPortalBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntityType.END_PORTAL, pos, state);
    }

    public boolean shouldDrawSide(Direction direction) {
        return direction.getAxis() == Direction.Axis.Y;
    }
}

