/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;

public class BreedTask
extends MultiTickTask<AnimalEntity> {
    private static final int MAX_RANGE = 3;
    private static final int MIN_BREED_TIME = 60;
    private static final int RUN_TIME = 110;
    private final EntityType<? extends AnimalEntity> targetType;
    private final float speed;
    private long breedTime;

    public BreedTask(EntityType<? extends AnimalEntity> targetType, float speed) {
        super(ImmutableMap.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED), 110);
        this.targetType = targetType;
        this.speed = speed;
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, AnimalEntity arg2) {
        return arg2.isInLove() && this.findBreedTarget(arg2).isPresent();
    }

    @Override
    protected void run(ServerWorld arg, AnimalEntity arg2, long l) {
        AnimalEntity lv = this.findBreedTarget(arg2).get();
        arg2.getBrain().remember(MemoryModuleType.BREED_TARGET, lv);
        lv.getBrain().remember(MemoryModuleType.BREED_TARGET, arg2);
        LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, this.speed);
        int i = 60 + arg2.getRandom().nextInt(50);
        this.breedTime = l + (long)i;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, AnimalEntity arg2, long l) {
        if (!this.hasBreedTarget(arg2)) {
            return false;
        }
        AnimalEntity lv = this.getBreedTarget(arg2);
        return lv.isAlive() && arg2.canBreedWith(lv) && LookTargetUtil.canSee(arg2.getBrain(), lv) && l <= this.breedTime;
    }

    @Override
    protected void keepRunning(ServerWorld arg, AnimalEntity arg2, long l) {
        AnimalEntity lv = this.getBreedTarget(arg2);
        LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, this.speed);
        if (!arg2.isInRange(lv, 3.0)) {
            return;
        }
        if (l >= this.breedTime) {
            arg2.breed(arg, lv);
            arg2.getBrain().forget(MemoryModuleType.BREED_TARGET);
            lv.getBrain().forget(MemoryModuleType.BREED_TARGET);
        }
    }

    @Override
    protected void finishRunning(ServerWorld arg, AnimalEntity arg2, long l) {
        arg2.getBrain().forget(MemoryModuleType.BREED_TARGET);
        arg2.getBrain().forget(MemoryModuleType.WALK_TARGET);
        arg2.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        this.breedTime = 0L;
    }

    private AnimalEntity getBreedTarget(AnimalEntity animal) {
        return (AnimalEntity)animal.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
    }

    private boolean hasBreedTarget(AnimalEntity animal) {
        Brain<PassiveEntity> lv = animal.getBrain();
        return lv.hasMemoryModule(MemoryModuleType.BREED_TARGET) && lv.getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.targetType;
    }

    private Optional<? extends AnimalEntity> findBreedTarget(AnimalEntity animal) {
        return animal.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).get().findFirst(entity -> {
            AnimalEntity lv;
            return entity.getType() == this.targetType && entity instanceof AnimalEntity && animal.canBreedWith(lv = (AnimalEntity)entity);
        }).map(AnimalEntity.class::cast);
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (AnimalEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (AnimalEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (AnimalEntity)entity, time);
    }
}

