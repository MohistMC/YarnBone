/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry.entry;

public interface RegistryEntryOwner<T> {
    default public boolean ownerEquals(RegistryEntryOwner<T> other) {
        return other == this;
    }
}

