/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public interface SidedInventory
extends Inventory {
    public int[] getAvailableSlots(Direction var1);

    public boolean canInsert(int var1, ItemStack var2, @Nullable Direction var3);

    public boolean canExtract(int var1, ItemStack var2, Direction var3);
}

