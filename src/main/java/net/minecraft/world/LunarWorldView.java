/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world;

import net.minecraft.world.WorldView;
import net.minecraft.world.dimension.DimensionType;

public interface LunarWorldView
extends WorldView {
    public long getLunarTime();

    default public float getMoonSize() {
        return DimensionType.MOON_SIZES[this.getDimension().getMoonPhase(this.getLunarTime())];
    }

    default public float getSkyAngle(float tickDelta) {
        return this.getDimension().getSkyAngle(this.getLunarTime());
    }

    default public int getMoonPhase() {
        return this.getDimension().getMoonPhase(this.getLunarTime());
    }
}

