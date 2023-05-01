/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.tick;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;

public interface TickScheduler<T> {
    public void scheduleTick(OrderedTick<T> var1);

    public boolean isQueued(BlockPos var1, T var2);

    public int getTickCount();
}

