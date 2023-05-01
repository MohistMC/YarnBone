/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.poi;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;

public record PointOfInterestType(Set<BlockState> blockStates, int ticketCount, int searchDistance) {
    public static final Predicate<RegistryEntry<PointOfInterestType>> NONE = type -> false;

    public PointOfInterestType {
        set = Set.copyOf(set);
    }

    public boolean contains(BlockState state) {
        return this.blockStates.contains(state);
    }
}

