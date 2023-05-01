/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class EscapeDangerGoal
extends Goal {
    public static final int RANGE_Y = 1;
    protected final PathAwareEntity mob;
    protected final double speed;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected boolean active;

    public EscapeDangerGoal(PathAwareEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        BlockPos lv;
        if (!this.isInDanger()) {
            return false;
        }
        if (this.mob.isOnFire() && (lv = this.locateClosestWater(this.mob.world, this.mob, 5)) != null) {
            this.targetX = lv.getX();
            this.targetY = lv.getY();
            this.targetZ = lv.getZ();
            return true;
        }
        return this.findTarget();
    }

    protected boolean isInDanger() {
        return this.mob.getAttacker() != null || this.mob.shouldEscapePowderSnow() || this.mob.isOnFire();
    }

    protected boolean findTarget() {
        Vec3d lv = NoPenaltyTargeting.find(this.mob, 5, 4);
        if (lv == null) {
            return false;
        }
        this.targetX = lv.x;
        this.targetY = lv.y;
        this.targetZ = lv.z;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
        this.active = true;
    }

    @Override
    public void stop() {
        this.active = false;
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.getNavigation().isIdle();
    }

    @Nullable
    protected BlockPos locateClosestWater(BlockView world, Entity entity, int rangeX) {
        BlockPos lv = entity.getBlockPos();
        if (!world.getBlockState(lv).getCollisionShape(world, lv).isEmpty()) {
            return null;
        }
        return BlockPos.findClosest(entity.getBlockPos(), rangeX, 1, pos -> world.getFluidState((BlockPos)pos).isIn(FluidTags.WATER)).orElse(null);
    }
}

