/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;

public interface TestListener {
    public void onStarted(GameTestState var1);

    public void onPassed(GameTestState var1);

    public void onFailed(GameTestState var1);
}

