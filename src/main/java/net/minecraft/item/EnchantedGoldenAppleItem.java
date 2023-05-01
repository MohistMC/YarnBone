/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EnchantedGoldenAppleItem
extends Item {
    public EnchantedGoldenAppleItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}

