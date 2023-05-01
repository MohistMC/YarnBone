/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import org.jetbrains.annotations.Nullable;

public class SkeletonHorseTrapTriggerGoal
extends Goal {
    private final SkeletonHorseEntity skeletonHorse;

    public SkeletonHorseTrapTriggerGoal(SkeletonHorseEntity skeletonHorse) {
        this.skeletonHorse = skeletonHorse;
    }

    @Override
    public boolean canStart() {
        return this.skeletonHorse.world.isPlayerInRange(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ(), 10.0);
    }

    @Override
    public void tick() {
        ServerWorld lv = (ServerWorld)this.skeletonHorse.world;
        LocalDifficulty lv2 = lv.getLocalDifficulty(this.skeletonHorse.getBlockPos());
        this.skeletonHorse.setTrapped(false);
        this.skeletonHorse.setTame(true);
        this.skeletonHorse.setBreedingAge(0);
        LightningEntity lv3 = EntityType.LIGHTNING_BOLT.create(lv);
        if (lv3 == null) {
            return;
        }
        lv3.refreshPositionAfterTeleport(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ());
        lv3.setCosmetic(true);
        lv.spawnEntity(lv3);
        SkeletonEntity lv4 = this.getSkeleton(lv2, this.skeletonHorse);
        if (lv4 == null) {
            return;
        }
        lv4.startRiding(this.skeletonHorse);
        lv.spawnEntityAndPassengers(lv4);
        for (int i = 0; i < 3; ++i) {
            SkeletonEntity lv6;
            AbstractHorseEntity lv5 = this.getHorse(lv2);
            if (lv5 == null || (lv6 = this.getSkeleton(lv2, lv5)) == null) continue;
            lv6.startRiding(lv5);
            lv5.addVelocity(this.skeletonHorse.getRandom().nextTriangular(0.0, 1.1485), 0.0, this.skeletonHorse.getRandom().nextTriangular(0.0, 1.1485));
            lv.spawnEntityAndPassengers(lv5);
        }
    }

    @Nullable
    private AbstractHorseEntity getHorse(LocalDifficulty localDifficulty) {
        SkeletonHorseEntity lv = EntityType.SKELETON_HORSE.create(this.skeletonHorse.world);
        if (lv != null) {
            lv.initialize((ServerWorld)this.skeletonHorse.world, localDifficulty, SpawnReason.TRIGGERED, null, null);
            lv.setPosition(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ());
            lv.timeUntilRegen = 60;
            lv.setPersistent();
            lv.setTame(true);
            lv.setBreedingAge(0);
        }
        return lv;
    }

    @Nullable
    private SkeletonEntity getSkeleton(LocalDifficulty localDifficulty, AbstractHorseEntity vehicle) {
        SkeletonEntity lv = EntityType.SKELETON.create(vehicle.world);
        if (lv != null) {
            lv.initialize((ServerWorld)vehicle.world, localDifficulty, SpawnReason.TRIGGERED, null, null);
            lv.setPosition(vehicle.getX(), vehicle.getY(), vehicle.getZ());
            lv.timeUntilRegen = 60;
            lv.setPersistent();
            if (lv.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
                lv.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }
            lv.equipStack(EquipmentSlot.MAINHAND, EnchantmentHelper.enchant(lv.getRandom(), this.removeEnchantments(lv.getMainHandStack()), (int)(5.0f + localDifficulty.getClampedLocalDifficulty() * (float)lv.getRandom().nextInt(18)), false));
            lv.equipStack(EquipmentSlot.HEAD, EnchantmentHelper.enchant(lv.getRandom(), this.removeEnchantments(lv.getEquippedStack(EquipmentSlot.HEAD)), (int)(5.0f + localDifficulty.getClampedLocalDifficulty() * (float)lv.getRandom().nextInt(18)), false));
        }
        return lv;
    }

    private ItemStack removeEnchantments(ItemStack stack) {
        stack.removeSubNbt("Enchantments");
        return stack;
    }
}

