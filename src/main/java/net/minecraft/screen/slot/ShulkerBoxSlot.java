/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ShulkerBoxSlot
extends Slot {
    public ShulkerBoxSlot(Inventory arg, int i, int j, int k) {
        super(arg, i, j, k);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem().canBeNested();
    }
}

