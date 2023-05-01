/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.StemBlock;

public abstract class GourdBlock
extends Block {
    public GourdBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    public abstract StemBlock getStem();

    public abstract AttachedStemBlock getAttachedStem();
}

