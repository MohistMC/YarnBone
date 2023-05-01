/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util;

public enum ActionResult {
    SUCCESS,
    CONSUME,
    CONSUME_PARTIAL,
    PASS,
    FAIL;


    public boolean isAccepted() {
        return this == SUCCESS || this == CONSUME || this == CONSUME_PARTIAL;
    }

    public boolean shouldSwingHand() {
        return this == SUCCESS;
    }

    public boolean shouldIncrementStat() {
        return this == SUCCESS || this == CONSUME;
    }

    public static ActionResult success(boolean swingHand) {
        return swingHand ? SUCCESS : CONSUME;
    }
}

