/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.profiler;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.Sampler;

public interface SamplerSource {
    public Set<Sampler> getSamplers(Supplier<ReadableProfiler> var1);
}

