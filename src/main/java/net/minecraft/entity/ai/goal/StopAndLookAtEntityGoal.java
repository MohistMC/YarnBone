/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.MobEntity;

public class StopAndLookAtEntityGoal
extends LookAtEntityGoal {
    public StopAndLookAtEntityGoal(MobEntity arg, Class<? extends LivingEntity> class_, float f) {
        super(arg, class_, f);
        this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.MOVE));
    }

    public StopAndLookAtEntityGoal(MobEntity arg, Class<? extends LivingEntity> class_, float f, float g) {
        super(arg, class_, f, g);
        this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.MOVE));
    }
}

