/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

public interface Clearable {
    public void clear();

    public static void clear(@Nullable Object o) {
        if (o instanceof Clearable) {
            ((Clearable)o).clear();
        }
    }
}

