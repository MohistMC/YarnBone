/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.thread;

import java.util.List;
import net.minecraft.util.profiler.Sampler;

public interface SampleableExecutor {
    public List<Sampler> createSamplers();
}

