/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;

public class WardenAttackablesSensor
extends NearestLivingEntitiesSensor<WardenEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.copyOf(Iterables.concat(super.getOutputMemoryModules(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    @Override
    protected void sense(ServerWorld arg, WardenEntity arg2) {
        super.sense(arg, arg2);
        WardenAttackablesSensor.findNearestTarget(arg2, entity -> entity.getType() == EntityType.PLAYER).or(() -> WardenAttackablesSensor.findNearestTarget(arg2, arg -> arg.getType() != EntityType.PLAYER)).ifPresentOrElse(entity -> arg2.getBrain().remember(MemoryModuleType.NEAREST_ATTACKABLE, entity), () -> arg2.getBrain().forget(MemoryModuleType.NEAREST_ATTACKABLE));
    }

    private static Optional<LivingEntity> findNearestTarget(WardenEntity warden, Predicate<LivingEntity> targetPredicate) {
        return warden.getBrain().getOptionalRegisteredMemory(MemoryModuleType.MOBS).stream().flatMap(Collection::stream).filter(warden::isValidTarget).filter(targetPredicate).findFirst();
    }

    @Override
    protected int getHorizontalExpansion() {
        return 24;
    }

    @Override
    protected int getHeightExpansion() {
        return 24;
    }
}

