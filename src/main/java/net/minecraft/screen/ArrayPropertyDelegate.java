/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.screen;

import net.minecraft.screen.PropertyDelegate;

public class ArrayPropertyDelegate
implements PropertyDelegate {
    private final int[] data;

    public ArrayPropertyDelegate(int size) {
        this.data = new int[size];
    }

    @Override
    public int get(int index) {
        return this.data[index];
    }

    @Override
    public void set(int index, int value) {
        this.data[index] = value;
    }

    @Override
    public int size() {
        return this.data.length;
    }
}

