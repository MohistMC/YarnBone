/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class BatEntity
extends AmbientEntity {
    public static final float field_30268 = 74.48451f;
    public static final int field_28637 = MathHelper.ceil(2.4166098f);
    private static final TrackedData<Byte> BAT_FLAGS = DataTracker.registerData(BatEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int ROOSTING_FLAG = 1;
    private static final TargetPredicate CLOSE_PLAYER_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(4.0);
    @Nullable
    private BlockPos hangingPosition;

    public BatEntity(EntityType<? extends BatEntity> arg, World arg2) {
        super((EntityType<? extends AmbientEntity>)arg, arg2);
        if (!arg2.isClient) {
            this.setRoosting(true);
        }
    }

    @Override
    public boolean isFlappingWings() {
        return !this.isRoosting() && this.age % field_28637 == 0;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BAT_FLAGS, (byte)0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    public float getSoundPitch() {
        return super.getSoundPitch() * 0.95f;
    }

    @Override
    @Nullable
    public SoundEvent getAmbientSound() {
        if (this.isRoosting() && this.random.nextInt(4) != 0) {
            return null;
        }
        return SoundEvents.ENTITY_BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    protected void tickCramming() {
    }

    public static DefaultAttributeContainer.Builder createBatAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0);
    }

    public boolean isRoosting() {
        return (this.dataTracker.get(BAT_FLAGS) & 1) != 0;
    }

    public void setRoosting(boolean roosting) {
        byte b = this.dataTracker.get(BAT_FLAGS);
        if (roosting) {
            this.dataTracker.set(BAT_FLAGS, (byte)(b | 1));
        } else {
            this.dataTracker.set(BAT_FLAGS, (byte)(b & 0xFFFFFFFE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRoosting()) {
            this.setVelocity(Vec3d.ZERO);
            this.setPos(this.getX(), (double)MathHelper.floor(this.getY()) + 1.0 - (double)this.getHeight(), this.getZ());
        } else {
            this.setVelocity(this.getVelocity().multiply(1.0, 0.6, 1.0));
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        BlockPos lv = this.getBlockPos();
        BlockPos lv2 = lv.up();
        if (this.isRoosting()) {
            boolean bl = this.isSilent();
            if (this.world.getBlockState(lv2).isSolidBlock(this.world, lv)) {
                if (this.random.nextInt(200) == 0) {
                    this.headYaw = this.random.nextInt(360);
                }
                if (this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, this) != null) {
                    this.setRoosting(false);
                    if (!bl) {
                        this.world.syncWorldEvent(null, WorldEvents.BAT_TAKES_OFF, lv, 0);
                    }
                }
            } else {
                this.setRoosting(false);
                if (!bl) {
                    this.world.syncWorldEvent(null, WorldEvents.BAT_TAKES_OFF, lv, 0);
                }
            }
        } else {
            if (!(this.hangingPosition == null || this.world.isAir(this.hangingPosition) && this.hangingPosition.getY() > this.world.getBottomY())) {
                this.hangingPosition = null;
            }
            if (this.hangingPosition == null || this.random.nextInt(30) == 0 || this.hangingPosition.isWithinDistance(this.getPos(), 2.0)) {
                this.hangingPosition = BlockPos.ofFloored(this.getX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7), this.getY() + (double)this.random.nextInt(6) - 2.0, this.getZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7));
            }
            double d = (double)this.hangingPosition.getX() + 0.5 - this.getX();
            double e = (double)this.hangingPosition.getY() + 0.1 - this.getY();
            double f = (double)this.hangingPosition.getZ() + 0.5 - this.getZ();
            Vec3d lv3 = this.getVelocity();
            Vec3d lv4 = lv3.add((Math.signum(d) * 0.5 - lv3.x) * (double)0.1f, (Math.signum(e) * (double)0.7f - lv3.y) * (double)0.1f, (Math.signum(f) * 0.5 - lv3.z) * (double)0.1f);
            this.setVelocity(lv4);
            float g = (float)(MathHelper.atan2(lv4.z, lv4.x) * 57.2957763671875) - 90.0f;
            float h = MathHelper.wrapDegrees(g - this.getYaw());
            this.forwardSpeed = 0.5f;
            this.setYaw(this.getYaw() + h);
            if (this.random.nextInt(100) == 0 && this.world.getBlockState(lv2).isSolidBlock(this.world, lv2)) {
                this.setRoosting(true);
            }
        }
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.world.isClient && this.isRoosting()) {
            this.setRoosting(false);
        }
        return super.damage(source, amount);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(BAT_FLAGS, nbt.getByte("BatFlags"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("BatFlags", this.dataTracker.get(BAT_FLAGS));
    }

    public static boolean canSpawn(EntityType<BatEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if (pos.getY() >= world.getSeaLevel()) {
            return false;
        }
        int i = world.getLightLevel(pos);
        int j = 4;
        if (BatEntity.isTodayAroundHalloween()) {
            j = 7;
        } else if (random.nextBoolean()) {
            return false;
        }
        if (i > random.nextInt(j)) {
            return false;
        }
        return BatEntity.canMobSpawn(type, world, spawnReason, pos, random);
    }

    private static boolean isTodayAroundHalloween() {
        LocalDate localDate = LocalDate.now();
        int i = localDate.get(ChronoField.DAY_OF_MONTH);
        int j = localDate.get(ChronoField.MONTH_OF_YEAR);
        return j == 10 && i >= 20 || j == 11 && i <= 3;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height / 2.0f;
    }
}

