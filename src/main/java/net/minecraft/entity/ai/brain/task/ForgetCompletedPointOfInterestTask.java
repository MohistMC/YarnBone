/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.function.Predicate;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestType;

public class ForgetCompletedPointOfInterestTask {
    private static final int MAX_RANGE = 16;

    public static Task<LivingEntity> create(Predicate<RegistryEntry<PointOfInterestType>> poiTypePredicate, MemoryModuleType<GlobalPos> poiPosModule) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(poiPosModule)).apply(context, poiPos -> (world, entity, time) -> {
            GlobalPos lv = (GlobalPos)context.getValue(poiPos);
            BlockPos lv2 = lv.getPos();
            if (world.getRegistryKey() != lv.getDimension() || !lv2.isWithinDistance(entity.getPos(), 16.0)) {
                return false;
            }
            ServerWorld lv3 = world.getServer().getWorld(lv.getDimension());
            if (lv3 == null || !lv3.getPointOfInterestStorage().test(lv2, poiTypePredicate)) {
                poiPos.forget();
            } else if (ForgetCompletedPointOfInterestTask.isBedOccupiedByOthers(lv3, lv2, entity)) {
                poiPos.forget();
                world.getPointOfInterestStorage().releaseTicket(lv2);
                DebugInfoSender.sendPointOfInterest(world, lv2);
            }
            return true;
        }));
    }

    private static boolean isBedOccupiedByOthers(ServerWorld world, BlockPos pos, LivingEntity entity) {
        BlockState lv = world.getBlockState(pos);
        return lv.isIn(BlockTags.BEDS) && lv.get(BedBlock.OCCUPIED) != false && !entity.isSleeping();
    }
}

