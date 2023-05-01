/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.chunk;

import net.minecraft.block.BlockState;

public interface BlockColumn {
    public BlockState getState(int var1);

    public void setState(int var1, BlockState var2);
}

