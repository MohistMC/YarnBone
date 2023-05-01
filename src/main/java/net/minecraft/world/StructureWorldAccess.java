/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public interface StructureWorldAccess
extends ServerWorldAccess {
    public long getSeed();

    default public boolean isValidForSetBlock(BlockPos pos) {
        return true;
    }

    default public void setCurrentlyGeneratingStructureName(@Nullable Supplier<String> structureName) {
    }
}

