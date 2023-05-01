/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.chunk;

public class EntryMissingException
extends RuntimeException {
    public EntryMissingException(int index) {
        super("Missing Palette entry for index " + index + ".");
    }
}

