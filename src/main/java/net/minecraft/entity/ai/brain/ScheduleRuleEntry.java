/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.brain;

public class ScheduleRuleEntry {
    private final int startTime;
    private final float priority;

    public ScheduleRuleEntry(int startTime, float priority) {
        this.startTime = startTime;
        this.priority = priority;
    }

    public int getStartTime() {
        return this.startTime;
    }

    public float getPriority() {
        return this.priority;
    }
}

