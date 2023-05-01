/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util;

import net.minecraft.util.Formatting;

public enum Rarity {
    COMMON(Formatting.WHITE),
    UNCOMMON(Formatting.YELLOW),
    RARE(Formatting.AQUA),
    EPIC(Formatting.LIGHT_PURPLE);

    public final Formatting formatting;

    private Rarity(Formatting formatting) {
        this.formatting = formatting;
    }
}

