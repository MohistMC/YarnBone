/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum Tilt implements StringIdentifiable
{
    NONE("none", true),
    UNSTABLE("unstable", false),
    PARTIAL("partial", true),
    FULL("full", true);

    private final String name;
    private final boolean stable;

    private Tilt(String name, boolean stable) {
        this.name = name;
        this.stable = stable;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public boolean isStable() {
        return this.stable;
    }
}

