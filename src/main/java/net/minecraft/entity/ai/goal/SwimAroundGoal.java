/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SwimAroundGoal
extends WanderAroundGoal {
    public SwimAroundGoal(PathAwareEntity arg, double d, int i) {
        super(arg, d, i);
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        return LookTargetUtil.find(this.mob, 10, 7);
    }
}

