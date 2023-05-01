/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface FluidModificationItem {
    default public void onEmptied(@Nullable PlayerEntity player, World world, ItemStack stack, BlockPos pos) {
    }

    public boolean placeFluid(@Nullable PlayerEntity var1, World var2, BlockPos var3, @Nullable BlockHitResult var4);
}

