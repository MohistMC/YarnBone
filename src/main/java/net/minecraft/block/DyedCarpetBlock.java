/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.util.DyeColor;

public class DyedCarpetBlock
extends CarpetBlock {
    private final DyeColor dyeColor;

    protected DyedCarpetBlock(DyeColor dyeColor, AbstractBlock.Settings settings) {
        super(settings);
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }
}

