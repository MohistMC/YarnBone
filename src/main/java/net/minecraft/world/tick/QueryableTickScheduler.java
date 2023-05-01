/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.tick;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.TickScheduler;

public interface QueryableTickScheduler<T>
extends TickScheduler<T> {
    public boolean isTicking(BlockPos var1, T var2);
}

