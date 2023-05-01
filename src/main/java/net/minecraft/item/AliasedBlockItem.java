/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class AliasedBlockItem
extends BlockItem {
    public AliasedBlockItem(Block arg, Item.Settings arg2) {
        super(arg, arg2);
    }

    @Override
    public String getTranslationKey() {
        return this.getOrCreateTranslationKey();
    }
}

