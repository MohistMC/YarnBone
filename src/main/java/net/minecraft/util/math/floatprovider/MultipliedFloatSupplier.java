/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.math.floatprovider;

import java.util.Arrays;
import net.minecraft.util.math.floatprovider.FloatSupplier;
import net.minecraft.util.math.random.Random;

public class MultipliedFloatSupplier
implements FloatSupplier {
    private final FloatSupplier[] multipliers;

    public MultipliedFloatSupplier(FloatSupplier ... multipliers) {
        this.multipliers = multipliers;
    }

    @Override
    public float get(Random random) {
        float f = 1.0f;
        for (int i = 0; i < this.multipliers.length; ++i) {
            f *= this.multipliers[i].get(random);
        }
        return f;
    }

    public String toString() {
        return "MultipliedFloats" + Arrays.toString(this.multipliers);
    }
}

