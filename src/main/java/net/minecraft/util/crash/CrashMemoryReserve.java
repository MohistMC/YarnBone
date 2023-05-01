/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.crash;

import org.jetbrains.annotations.Nullable;

public class CrashMemoryReserve {
    @Nullable
    private static byte[] reservedMemory = null;

    public static void reserveMemory() {
        reservedMemory = new byte[0xA00000];
    }

    public static void releaseMemory() {
        reservedMemory = new byte[0];
    }
}

