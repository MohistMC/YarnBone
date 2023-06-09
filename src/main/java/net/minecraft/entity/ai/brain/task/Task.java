/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.server.world.ServerWorld;

public interface Task<E extends LivingEntity> {
    public MultiTickTask.Status getStatus();

    public boolean tryStarting(ServerWorld var1, E var2, long var3);

    public void tick(ServerWorld var1, E var2, long var3);

    public void stop(ServerWorld var1, E var2, long var3);

    public String getName();
}

