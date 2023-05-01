/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.StemBlock;

public class MelonBlock
extends GourdBlock {
    protected MelonBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public StemBlock getStem() {
        return (StemBlock)Blocks.MELON_STEM;
    }

    @Override
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock)Blocks.ATTACHED_MELON_STEM;
    }
}

