/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.screen;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public interface ScreenHandlerListener {
    public void onSlotUpdate(ScreenHandler var1, int var2, ItemStack var3);

    public void onPropertyUpdate(ScreenHandler var1, int var2, int var3);
}

