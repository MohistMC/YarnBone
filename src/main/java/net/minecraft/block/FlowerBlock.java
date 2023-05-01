/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class FlowerBlock
extends PlantBlock
implements SuspiciousStewIngredient {
    protected static final float field_31094 = 3.0f;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
    private final StatusEffect effectInStew;
    private final int effectInStewDuration;

    public FlowerBlock(StatusEffect suspiciousStewEffect, int effectDuration, AbstractBlock.Settings settings) {
        super(settings);
        this.effectInStew = suspiciousStewEffect;
        this.effectInStewDuration = suspiciousStewEffect.isInstant() ? effectDuration : effectDuration * 20;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d lv = state.getModelOffset(world, pos);
        return SHAPE.offset(lv.x, lv.y, lv.z);
    }

    @Override
    public StatusEffect getEffectInStew() {
        return this.effectInStew;
    }

    @Override
    public int getEffectInStewDuration() {
        return this.effectInStewDuration;
    }
}

