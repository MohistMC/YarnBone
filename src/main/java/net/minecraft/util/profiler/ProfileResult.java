/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.profiler;

import java.nio.file.Path;
import java.util.List;
import net.minecraft.util.profiler.ProfilerTiming;

public interface ProfileResult {
    public static final char SPLITTER_CHAR = '\u001e';

    public List<ProfilerTiming> getTimings(String var1);

    public boolean save(Path var1);

    public long getStartTime();

    public int getStartTick();

    public long getEndTime();

    public int getEndTick();

    default public long getTimeSpan() {
        return this.getEndTime() - this.getStartTime();
    }

    default public int getTickSpan() {
        return this.getEndTick() - this.getStartTick();
    }

    public String getRootTimings();

    public static String getHumanReadableName(String path) {
        return path.replace('\u001e', '.');
    }
}

