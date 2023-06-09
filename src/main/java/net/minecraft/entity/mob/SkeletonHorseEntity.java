/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.SkeletonHorseTrapTriggerGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SkeletonHorseEntity
extends AbstractHorseEntity {
    private final SkeletonHorseTrapTriggerGoal trapTriggerGoal = new SkeletonHorseTrapTriggerGoal(this);
    private static final int DESPAWN_AGE = 18000;
    private boolean trapped;
    private int trapTime;

    public SkeletonHorseEntity(EntityType<? extends SkeletonHorseEntity> arg, World arg2) {
        super((EntityType<? extends AbstractHorseEntity>)arg, arg2);
    }

    public static DefaultAttributeContainer.Builder createSkeletonHorseAttributes() {
        return SkeletonHorseEntity.createBaseHorseAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);
    }

    @Override
    protected void initAttributes(Random random) {
        this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(SkeletonHorseEntity.getChildJumpStrengthBonus(random::nextDouble));
    }

    @Override
    protected void initCustomGoals() {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSubmergedIn(FluidTags.WATER)) {
            return SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT_WATER;
        }
        return SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SKELETON_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SKELETON_HORSE_HURT;
    }

    @Override
    protected SoundEvent getSwimSound() {
        if (this.onGround) {
            if (this.hasPassengers()) {
                ++this.soundTicks;
                if (this.soundTicks > 5 && this.soundTicks % 3 == 0) {
                    return SoundEvents.ENTITY_SKELETON_HORSE_GALLOP_WATER;
                }
                if (this.soundTicks <= 5) {
                    return SoundEvents.ENTITY_SKELETON_HORSE_STEP_WATER;
                }
            } else {
                return SoundEvents.ENTITY_SKELETON_HORSE_STEP_WATER;
            }
        }
        return SoundEvents.ENTITY_SKELETON_HORSE_SWIM;
    }

    @Override
    protected void playSwimSound(float volume) {
        if (this.onGround) {
            super.playSwimSound(0.3f);
        } else {
            super.playSwimSound(Math.min(0.1f, volume * 25.0f));
        }
    }

    @Override
    protected void playJumpSound() {
        if (this.isTouchingWater()) {
            this.playSound(SoundEvents.ENTITY_SKELETON_HORSE_JUMP_WATER, 0.4f, 1.0f);
        } else {
            super.playJumpSound();
        }
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    @Override
    public double getMountedHeightOffset() {
        return super.getMountedHeightOffset() - 0.1875;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.isTrapped() && this.trapTime++ >= 18000) {
            this.discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("SkeletonTrap", this.isTrapped());
        nbt.putInt("SkeletonTrapTime", this.trapTime);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setTrapped(nbt.getBoolean("SkeletonTrap"));
        this.trapTime = nbt.getInt("SkeletonTrapTime");
    }

    @Override
    protected float getBaseMovementSpeedMultiplier() {
        return 0.96f;
    }

    public boolean isTrapped() {
        return this.trapped;
    }

    public void setTrapped(boolean trapped) {
        if (trapped == this.trapped) {
            return;
        }
        this.trapped = trapped;
        if (trapped) {
            this.goalSelector.add(1, this.trapTriggerGoal);
        } else {
            this.goalSelector.remove(this.trapTriggerGoal);
        }
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.SKELETON_HORSE.create(world);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.isTame()) {
            return ActionResult.PASS;
        }
        return super.interactMob(player, hand);
    }
}

