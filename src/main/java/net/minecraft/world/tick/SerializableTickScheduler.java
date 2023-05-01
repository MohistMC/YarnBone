/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.tick;

import java.util.function.Function;
import net.minecraft.nbt.NbtElement;

public interface SerializableTickScheduler<T> {
    public NbtElement toNbt(long var1, Function<T, String> var3);
}

