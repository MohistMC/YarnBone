/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class ExperienceBottleEntity
extends ThrownItemEntity {
    public ExperienceBottleEntity(EntityType<? extends ExperienceBottleEntity> arg, World arg2) {
        super((EntityType<? extends ThrownItemEntity>)arg, arg2);
    }

    public ExperienceBottleEntity(World world, LivingEntity owner) {
        super((EntityType<? extends ThrownItemEntity>)EntityType.EXPERIENCE_BOTTLE, owner, world);
    }

    public ExperienceBottleEntity(World world, double x, double y, double z) {
        super((EntityType<? extends ThrownItemEntity>)EntityType.EXPERIENCE_BOTTLE, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected float getGravity() {
        return 0.07f;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.world instanceof ServerWorld) {
            this.world.syncWorldEvent(WorldEvents.SPLASH_POTION_SPLASHED, this.getBlockPos(), PotionUtil.getColor(Potions.WATER));
            int i = 3 + this.world.random.nextInt(5) + this.world.random.nextInt(5);
            ExperienceOrbEntity.spawn((ServerWorld)this.world, this.getPos(), i);
            this.discard();
        }
    }
}

