/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.chunk;

import net.minecraft.util.math.BlockPos;

public interface BlockEntityTickInvoker {
    public void tick();

    public boolean isRemoved();

    public BlockPos getPos();

    public String getName();
}

