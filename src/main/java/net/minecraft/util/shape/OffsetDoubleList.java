/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList
extends AbstractDoubleList {
    private final DoubleList oldList;
    private final double offset;

    public OffsetDoubleList(DoubleList oldList, double offset) {
        this.oldList = oldList;
        this.offset = offset;
    }

    @Override
    public double getDouble(int position) {
        return this.oldList.getDouble(position) + this.offset;
    }

    @Override
    public int size() {
        return this.oldList.size();
    }
}

