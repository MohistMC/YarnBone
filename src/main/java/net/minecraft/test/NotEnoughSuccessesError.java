/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;

class NotEnoughSuccessesError
extends Throwable {
    public NotEnoughSuccessesError(int attempts, int successes, GameTestState test) {
        super("Not enough successes: " + successes + " out of " + attempts + " attempts. Required successes: " + test.getRequiredSuccesses() + ". max attempts: " + test.getMaxAttempts() + ".", test.getThrowable());
    }
}

