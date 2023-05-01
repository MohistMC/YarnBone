/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum DoubleBlockHalf implements StringIdentifiable
{
    UPPER,
    LOWER;


    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this == UPPER ? "upper" : "lower";
    }
}

