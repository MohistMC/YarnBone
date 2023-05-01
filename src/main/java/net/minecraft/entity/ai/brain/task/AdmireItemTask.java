/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PiglinBrain;

public class AdmireItemTask {
    public static Task<LivingEntity> create(int duration) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), context.queryMemoryAbsent(MemoryModuleType.ADMIRING_ITEM), context.queryMemoryAbsent(MemoryModuleType.ADMIRING_DISABLED), context.queryMemoryAbsent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply(context, (nearestVisibleWantedItem, admiringItem, admiringDisabled, disableWalkToAdmireItem) -> (world, entity, time) -> {
            ItemEntity lv = (ItemEntity)context.getValue(nearestVisibleWantedItem);
            if (!PiglinBrain.isGoldenItem(lv.getStack())) {
                return false;
            }
            admiringItem.remember(true, duration);
            return true;
        }));
    }
}

