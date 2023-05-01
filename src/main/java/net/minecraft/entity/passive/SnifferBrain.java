/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

public class SnifferBrain {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_42676 = 6;
    static final List<SensorType<? extends Sensor<? super SnifferEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS);
    static final List<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.IS_PANICKING, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryModuleType.SNIFFER_DIGGING, MemoryModuleType.SNIFFER_HAPPY, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.BREED_TARGET, new MemoryModuleType[0]);
    private static final int field_42677 = 9600;
    private static final float field_42678 = 1.0f;
    private static final float field_42679 = 2.0f;
    private static final float field_42680 = 1.25f;

    protected static Brain<?> create(Brain<SnifferEntity> brain) {
        SnifferBrain.addCoreActivities(brain);
        SnifferBrain.addIdleActivities(brain);
        SnifferBrain.addSniffActivities(brain);
        SnifferBrain.addDigActivities(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<SnifferEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8f), new FleeTask(2.0f){

            @Override
            protected void run(ServerWorld arg, PathAwareEntity arg2, long l) {
                arg2.getBrain().forget(MemoryModuleType.SNIFFER_DIGGING);
                arg2.getBrain().forget(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
                ((SnifferEntity)arg2).startState(SnifferEntity.State.IDLING);
                super.run(arg, arg2, l);
            }

            @Override
            protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
                this.run(world, (PathAwareEntity)entity, time);
            }
        }, new WanderAroundTask(10000, 15000)));
    }

    private static void addSniffActivities(Brain<SnifferEntity> brain) {
        brain.setTaskList(Activity.SNIFF, ImmutableList.of(Pair.of(0, new SearchingTask())), Set.of(Pair.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryModuleState.VALUE_PRESENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT)));
    }

    private static void addDigActivities(Brain<SnifferEntity> brain) {
        brain.setTaskList(Activity.DIG, ImmutableList.of(Pair.of(0, new DiggingTask(160, 180)), Pair.of(0, new FinishDiggingTask(40))), Set.of(Pair.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryModuleState.VALUE_PRESENT)));
    }

    private static void addIdleActivities(Brain<SnifferEntity> brain) {
        brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(0, new LookAroundTask(45, 90)), Pair.of(0, new FeelHappyTask(40, 100)), Pair.of(0, new RandomTask(ImmutableList.of(Pair.of(GoTowardsLookTargetTask.create(1.0f, 3), 2), Pair.of(new ScentingTask(40, 80), 1), Pair.of(new SniffingTask(40, 80), 1), Pair.of(new BreedTask(EntityType.SNIFFER, 1.0f), 1), Pair.of(LookAtMobTask.create(EntityType.PLAYER, 6.0f), 1), Pair.of(StrollTask.create(1.0f), 1), Pair.of(new WaitTask(5, 20), 2))))), Set.of(Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryModuleState.VALUE_ABSENT)));
    }

    static void updateActivities(SnifferEntity sniffer) {
        sniffer.getBrain().resetPossibleActivities(ImmutableList.of(Activity.DIG, Activity.SNIFF, Activity.IDLE));
    }

    static class SearchingTask
    extends MultiTickTask<SnifferEntity> {
        SearchingTask() {
            super(Map.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryModuleState.VALUE_PRESENT), 600);
        }

        @Override
        protected boolean shouldRun(ServerWorld arg, SnifferEntity arg2) {
            return !arg2.isPanicking() && !arg2.isTouchingWater();
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            if (arg2.isPanicking() && !arg2.isTouchingWater()) {
                return false;
            }
            Optional<BlockPos> optional = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET).map(WalkTarget::getLookTarget).map(LookTarget::getBlockPos);
            Optional<BlockPos> optional2 = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
            if (optional.isEmpty() || optional2.isEmpty()) {
                return false;
            }
            return optional2.get().equals(optional.get());
        }

        @Override
        protected void run(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.SEARCHING);
        }

        @Override
        protected void finishRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            if (arg2.canDig()) {
                arg2.getBrain().remember(MemoryModuleType.SNIFFER_DIGGING, true);
            }
            arg2.getBrain().forget(MemoryModuleType.WALK_TARGET);
            arg2.getBrain().forget(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
        }

        @Override
        protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
            this.finishRunning(world, (SnifferEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (SnifferEntity)entity, time);
        }
    }

    static class DiggingTask
    extends MultiTickTask<SnifferEntity> {
        DiggingTask(int minRunTime, int maxRunTime) {
            super(Map.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SNIFFER_DIGGING, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleState.VALUE_ABSENT), minRunTime, maxRunTime);
        }

        @Override
        protected boolean shouldRun(ServerWorld arg, SnifferEntity arg2) {
            return !arg2.isPanicking() && !arg2.isTouchingWater();
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            return arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent() && !arg2.isPanicking();
        }

        @Override
        protected void run(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.DIGGING);
        }

        @Override
        protected void finishRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.getBrain().remember(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 9600L);
        }

        @Override
        protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
            this.finishRunning(world, (SnifferEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (SnifferEntity)entity, time);
        }
    }

    static class FinishDiggingTask
    extends MultiTickTask<SnifferEntity> {
        FinishDiggingTask(int runTime) {
            super(Map.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SNIFFER_DIGGING, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleState.VALUE_PRESENT), runTime, runTime);
        }

        @Override
        protected boolean shouldRun(ServerWorld arg, SnifferEntity arg2) {
            return true;
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            return arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent();
        }

        @Override
        protected void run(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.RISING);
        }

        @Override
        protected void finishRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            boolean bl = this.isTimeLimitExceeded(l);
            arg2.startState(SnifferEntity.State.IDLING).finishDigging(bl);
            arg2.getBrain().forget(MemoryModuleType.SNIFFER_DIGGING);
            arg2.getBrain().remember(MemoryModuleType.SNIFFER_HAPPY, true);
        }

        @Override
        protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
            this.finishRunning(world, (SnifferEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (SnifferEntity)entity, time);
        }
    }

    static class FeelHappyTask
    extends MultiTickTask<SnifferEntity> {
        FeelHappyTask(int minRunTime, int maxRunTime) {
            super(Map.of(MemoryModuleType.SNIFFER_HAPPY, MemoryModuleState.VALUE_PRESENT), minRunTime, maxRunTime);
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            return true;
        }

        @Override
        protected void run(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.FEELING_HAPPY);
        }

        @Override
        protected void finishRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.IDLING);
            arg2.getBrain().forget(MemoryModuleType.SNIFFER_HAPPY);
        }

        @Override
        protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
            this.finishRunning(world, (SnifferEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (SnifferEntity)entity, time);
        }
    }

    static class ScentingTask
    extends MultiTickTask<SnifferEntity> {
        ScentingTask(int minRunTime, int maxRunTime) {
            super(Map.of(MemoryModuleType.SNIFFER_DIGGING, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SNIFFER_HAPPY, MemoryModuleState.VALUE_ABSENT), minRunTime, maxRunTime);
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            return true;
        }

        @Override
        protected void run(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.SCENTING);
        }

        @Override
        protected void finishRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.IDLING);
        }

        @Override
        protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
            this.finishRunning(world, (SnifferEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (SnifferEntity)entity, time);
        }
    }

    static class SniffingTask
    extends MultiTickTask<SnifferEntity> {
        SniffingTask(int minRunTime, int maxRunTime) {
            super(Map.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleState.VALUE_ABSENT), minRunTime, maxRunTime);
        }

        @Override
        protected boolean shouldRun(ServerWorld arg, SnifferEntity arg2) {
            return !arg2.isBaby() && !arg2.isTouchingWater();
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            return !arg2.isPanicking();
        }

        @Override
        protected void run(ServerWorld arg, SnifferEntity arg2, long l) {
            arg2.startState(SnifferEntity.State.SNIFFING);
        }

        @Override
        protected void finishRunning(ServerWorld arg, SnifferEntity arg2, long l) {
            boolean bl = this.isTimeLimitExceeded(l);
            arg2.startState(SnifferEntity.State.IDLING);
            if (bl) {
                arg2.findSniffingTargetPos().ifPresent(pos -> {
                    arg2.getBrain().remember(MemoryModuleType.SNIFFER_SNIFFING_TARGET, pos);
                    arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget((BlockPos)pos, 1.25f, 0));
                });
            }
        }

        @Override
        protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
            this.finishRunning(world, (SnifferEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (SnifferEntity)entity, time);
        }
    }
}

