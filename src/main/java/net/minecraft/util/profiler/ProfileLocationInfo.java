/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.profiler;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public interface ProfileLocationInfo {
    public long getTotalTime();

    public long getMaxTime();

    public long getVisitCount();

    public Object2LongMap<String> getCounts();
}

