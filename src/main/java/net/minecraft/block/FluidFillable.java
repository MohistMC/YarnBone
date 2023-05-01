/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public interface FluidFillable {
    public boolean canFillWithFluid(BlockView var1, BlockPos var2, BlockState var3, Fluid var4);

    public boolean tryFillWithFluid(WorldAccess var1, BlockPos var2, BlockState var3, FluidState var4);
}

