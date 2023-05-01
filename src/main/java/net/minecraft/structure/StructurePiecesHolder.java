/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure;

import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import org.jetbrains.annotations.Nullable;

public interface StructurePiecesHolder {
    public void addPiece(StructurePiece var1);

    @Nullable
    public StructurePiece getIntersecting(BlockBox var1);
}

