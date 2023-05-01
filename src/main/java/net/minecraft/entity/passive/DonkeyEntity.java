/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DonkeyEntity
extends AbstractDonkeyEntity {
    public DonkeyEntity(EntityType<? extends DonkeyEntity> arg, World arg2) {
        super((EntityType<? extends AbstractDonkeyEntity>)arg, arg2);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.ENTITY_DONKEY_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_DONKEY_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_DONKEY_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_DONKEY_HURT;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        }
        if (other instanceof DonkeyEntity || other instanceof HorseEntity) {
            return this.canBreed() && ((AbstractHorseEntity)other).canBreed();
        }
        return false;
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        EntityType<AbstractDonkeyEntity> lv = entity instanceof HorseEntity ? EntityType.MULE : EntityType.DONKEY;
        AbstractHorseEntity lv2 = lv.create(world);
        if (lv2 != null) {
            this.setChildAttributes(entity, lv2);
        }
        return lv2;
    }
}

