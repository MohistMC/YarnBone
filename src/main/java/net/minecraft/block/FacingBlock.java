/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;

public abstract class FacingBlock
extends Block {
    public static final DirectionProperty FACING = Properties.FACING;

    protected FacingBlock(AbstractBlock.Settings arg) {
        super(arg);
    }
}

