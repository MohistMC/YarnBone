/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.effect;

import net.minecraft.util.Formatting;

public enum StatusEffectCategory {
    BENEFICIAL(Formatting.BLUE),
    HARMFUL(Formatting.RED),
    NEUTRAL(Formatting.BLUE);

    private final Formatting formatting;

    private StatusEffectCategory(Formatting format) {
        this.formatting = format;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }
}

