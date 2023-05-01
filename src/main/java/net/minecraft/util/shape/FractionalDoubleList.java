/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;

public class FractionalDoubleList
extends AbstractDoubleList {
    private final int sectionCount;

    FractionalDoubleList(int sectionCount) {
        if (sectionCount <= 0) {
            throw new IllegalArgumentException("Need at least 1 part");
        }
        this.sectionCount = sectionCount;
    }

    @Override
    public double getDouble(int position) {
        return (double)position / (double)this.sectionCount;
    }

    @Override
    public int size() {
        return this.sectionCount + 1;
    }
}

