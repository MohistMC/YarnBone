/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.profiler;

import net.minecraft.util.profiler.Profiler;

public interface Recorder {
    public void stop();

    public void forceStop();

    public void startTick();

    public boolean isActive();

    public Profiler getProfiler();

    public void endTick();
}

