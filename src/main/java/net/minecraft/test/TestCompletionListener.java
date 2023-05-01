/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;

public interface TestCompletionListener {
    public void onTestFailed(GameTestState var1);

    public void onTestPassed(GameTestState var1);

    default public void onStopped() {
    }
}

