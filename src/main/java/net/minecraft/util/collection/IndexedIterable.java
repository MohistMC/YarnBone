/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.collection;

import org.jetbrains.annotations.Nullable;

public interface IndexedIterable<T>
extends Iterable<T> {
    public static final int ABSENT_RAW_ID = -1;

    public int getRawId(T var1);

    @Nullable
    public T get(int var1);

    default public T getOrThrow(int index) {
        T object = this.get(index);
        if (object == null) {
            throw new IllegalArgumentException("No value with id " + index);
        }
        return object;
    }

    public int size();
}

