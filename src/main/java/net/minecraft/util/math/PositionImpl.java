/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.math;

import net.minecraft.util.math.Position;

public class PositionImpl
implements Position {
    protected final double x;
    protected final double y;
    protected final double z;

    public PositionImpl(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }
}

