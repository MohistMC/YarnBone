/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class ScheduleActivityTask {
    public static Task<LivingEntity> create() {
        return TaskTriggerer.task(context -> context.point((world, entity, time) -> {
            entity.getBrain().refreshActivities(world.getTimeOfDay(), world.getTime());
            return true;
        }));
    }
}

