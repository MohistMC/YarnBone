/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LongJumpTask<E extends MobEntity>
extends MultiTickTask<E> {
    protected static final int MAX_COOLDOWN = 20;
    private static final int TARGET_RETAIN_TIME = 40;
    protected static final int PATHING_DISTANCE = 8;
    private static final int RUN_TIME = 200;
    private static final List<Integer> RAM_RANGES = Lists.newArrayList(65, 70, 75, 80);
    private final UniformIntProvider cooldownRange;
    protected final int verticalRange;
    protected final int horizontalRange;
    protected final float maxRange;
    protected List<Target> targets = Lists.newArrayList();
    protected Optional<Vec3d> lastPos = Optional.empty();
    @Nullable
    protected Vec3d lastTarget;
    protected int cooldown;
    protected long targetTime;
    private final Function<E, SoundEvent> entityToSound;
    private final BiPredicate<E, BlockPos> jumpToPredicate;

    public LongJumpTask(UniformIntProvider cooldownRange, int verticalRange, int horizontalRange, float maxRange, Function<E, SoundEvent> entityToSound) {
        this(cooldownRange, verticalRange, horizontalRange, maxRange, entityToSound, LongJumpTask::shouldJumpTo);
    }

    public static <E extends MobEntity> boolean shouldJumpTo(E entity, BlockPos pos) {
        World lv = entity.world;
        BlockPos lv2 = pos.down();
        return lv.getBlockState(lv2).isOpaqueFullCube(lv, lv2) && entity.getPathfindingPenalty(LandPathNodeMaker.getLandNodeType(lv, pos.mutableCopy())) == 0.0f;
    }

    public LongJumpTask(UniformIntProvider cooldownRange, int verticalRange, int horizontalRange, float maxRange, Function<E, SoundEvent> entityToSound, BiPredicate<E, BlockPos> jumpToPredicate) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleState.VALUE_ABSENT), 200);
        this.cooldownRange = cooldownRange;
        this.verticalRange = verticalRange;
        this.horizontalRange = horizontalRange;
        this.maxRange = maxRange;
        this.entityToSound = entityToSound;
        this.jumpToPredicate = jumpToPredicate;
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, MobEntity arg2) {
        boolean bl;
        boolean bl2 = bl = arg2.isOnGround() && !arg2.isTouchingWater() && !arg2.isInLava() && !arg.getBlockState(arg2.getBlockPos()).isOf(Blocks.HONEY_BLOCK);
        if (!bl) {
            arg2.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, this.cooldownRange.get(arg.random) / 2);
        }
        return bl;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
        boolean bl;
        boolean bl2 = bl = this.lastPos.isPresent() && this.lastPos.get().equals(arg2.getPos()) && this.cooldown > 0 && !arg2.isInsideWaterOrBubbleColumn() && (this.lastTarget != null || !this.targets.isEmpty());
        if (!bl && arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
            arg2.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, this.cooldownRange.get(arg.random) / 2);
            arg2.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        }
        return bl;
    }

    @Override
    protected void run(ServerWorld arg, E arg22, long l) {
        this.lastTarget = null;
        this.cooldown = 20;
        this.lastPos = Optional.of(((Entity)arg22).getPos());
        BlockPos lv = ((Entity)arg22).getBlockPos();
        int i = lv.getX();
        int j = lv.getY();
        int k = lv.getZ();
        this.targets = BlockPos.stream(i - this.horizontalRange, j - this.verticalRange, k - this.horizontalRange, i + this.horizontalRange, j + this.verticalRange, k + this.horizontalRange).filter(arg2 -> !arg2.equals(lv)).map(arg2 -> new Target(arg2.toImmutable(), MathHelper.ceil(lv.getSquaredDistance((Vec3i)arg2)))).collect(Collectors.toCollection(Lists::newArrayList));
    }

    @Override
    protected void keepRunning(ServerWorld arg, E arg2, long l) {
        if (this.lastTarget != null) {
            if (l - this.targetTime >= 40L) {
                ((Entity)arg2).setYaw(((MobEntity)arg2).bodyYaw);
                ((LivingEntity)arg2).setNoDrag(true);
                double d = this.lastTarget.length();
                double e = d + ((LivingEntity)arg2).getJumpBoostVelocityModifier();
                ((Entity)arg2).setVelocity(this.lastTarget.multiply(e / d));
                ((LivingEntity)arg2).getBrain().remember(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                arg.playSoundFromEntity(null, (Entity)arg2, this.entityToSound.apply(arg2), SoundCategory.NEUTRAL, 1.0f, 1.0f);
            }
        } else {
            --this.cooldown;
            this.findTarget(arg, arg2, l);
        }
    }

    protected void findTarget(ServerWorld world, E entity, long time) {
        while (!this.targets.isEmpty()) {
            Vec3d lv3;
            Vec3d lv4;
            Target lv;
            BlockPos lv2;
            Optional<Target> optional = this.getTarget(world);
            if (optional.isEmpty() || !this.canJumpTo(world, entity, lv2 = (lv = optional.get()).getPos()) || (lv4 = this.getJumpingVelocity((MobEntity)entity, lv3 = Vec3d.ofCenter(lv2))) == null) continue;
            ((LivingEntity)entity).getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(lv2));
            EntityNavigation lv5 = ((MobEntity)entity).getNavigation();
            Path lv6 = lv5.findPathTo(lv2, 0, 8);
            if (lv6 != null && lv6.reachesTarget()) continue;
            this.lastTarget = lv4;
            this.targetTime = time;
            return;
        }
    }

    protected Optional<Target> getTarget(ServerWorld world) {
        Optional<Target> optional = Weighting.getRandom(world.random, this.targets);
        optional.ifPresent(this.targets::remove);
        return optional;
    }

    private boolean canJumpTo(ServerWorld world, E entity, BlockPos pos) {
        BlockPos lv = ((Entity)entity).getBlockPos();
        int i = lv.getX();
        int j = lv.getZ();
        if (i == pos.getX() && j == pos.getZ()) {
            return false;
        }
        return this.jumpToPredicate.test(entity, pos);
    }

    @Nullable
    protected Vec3d getJumpingVelocity(MobEntity entity, Vec3d pos) {
        ArrayList<Integer> list = Lists.newArrayList(RAM_RANGES);
        Collections.shuffle(list);
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            int i = (Integer)iterator.next();
            Vec3d lv = this.getJumpingVelocity(entity, pos, i);
            if (lv == null) continue;
            return lv;
        }
        return null;
    }

    @Nullable
    private Vec3d getJumpingVelocity(MobEntity entity, Vec3d pos, int range) {
        Vec3d lv = entity.getPos();
        Vec3d lv2 = new Vec3d(pos.x - lv.x, 0.0, pos.z - lv.z).normalize().multiply(0.5);
        pos = pos.subtract(lv2);
        Vec3d lv3 = pos.subtract(lv);
        float f = (float)range * (float)Math.PI / 180.0f;
        double d = Math.atan2(lv3.z, lv3.x);
        double e = lv3.subtract(0.0, lv3.y, 0.0).lengthSquared();
        double g = Math.sqrt(e);
        double h = lv3.y;
        double j = Math.sin(2.0f * f);
        double k = 0.08;
        double l = Math.pow(Math.cos(f), 2.0);
        double m = Math.sin(f);
        double n = Math.cos(f);
        double o = Math.sin(d);
        double p = Math.cos(d);
        double q = e * 0.08 / (g * j - 2.0 * h * l);
        if (q < 0.0) {
            return null;
        }
        double r = Math.sqrt(q);
        if (r > (double)this.maxRange) {
            return null;
        }
        double s = r * n;
        double t = r * m;
        int u = MathHelper.ceil(g / s) * 2;
        double v = 0.0;
        Vec3d lv4 = null;
        EntityDimensions lv5 = entity.getDimensions(EntityPose.LONG_JUMPING);
        for (int w = 0; w < u - 1; ++w) {
            double x = m / n * (v += g / (double)u) - Math.pow(v, 2.0) * 0.08 / (2.0 * q * Math.pow(n, 2.0));
            double y = v * p;
            double z = v * o;
            Vec3d lv6 = new Vec3d(lv.x + y, lv.y + x, lv.z + z);
            if (lv4 != null && !this.canReach(entity, lv5, lv4, lv6)) {
                return null;
            }
            lv4 = lv6;
        }
        return new Vec3d(s * p, t, s * o).multiply(0.95f);
    }

    private boolean canReach(MobEntity entity, EntityDimensions dimensions, Vec3d arg3, Vec3d arg4) {
        Vec3d lv = arg4.subtract(arg3);
        double d = Math.min(dimensions.width, dimensions.height);
        int i = MathHelper.ceil(lv.length() / d);
        Vec3d lv2 = lv.normalize();
        Vec3d lv3 = arg3;
        for (int j = 0; j < i; ++j) {
            Vec3d vec3d = lv3 = j == i - 1 ? arg4 : lv3.add(lv2.multiply(d * (double)0.9f));
            if (entity.world.isSpaceEmpty(entity, dimensions.getBoxAt(lv3))) continue;
            return false;
        }
        return true;
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (MobEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (E)((MobEntity)entity), time);
    }

    public static class Target
    extends Weighted.Absent {
        private final BlockPos pos;

        public Target(BlockPos pos, int weight) {
            super(weight);
            this.pos = pos;
        }

        public BlockPos getPos() {
            return this.pos;
        }
    }
}

