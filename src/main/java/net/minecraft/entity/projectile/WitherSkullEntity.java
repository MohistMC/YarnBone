/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class WitherSkullEntity
extends ExplosiveProjectileEntity {
    private static final TrackedData<Boolean> CHARGED = DataTracker.registerData(WitherSkullEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public WitherSkullEntity(EntityType<? extends WitherSkullEntity> arg, World arg2) {
        super((EntityType<? extends ExplosiveProjectileEntity>)arg, arg2);
    }

    public WitherSkullEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ) {
        super(EntityType.WITHER_SKULL, owner, directionX, directionY, directionZ, world);
    }

    @Override
    protected float getDrag() {
        return this.isCharged() ? 0.73f : super.getDrag();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
        if (this.isCharged() && WitherEntity.canDestroy(blockState)) {
            return Math.min(0.8f, max);
        }
        return max;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        boolean bl;
        LivingEntity lv3;
        super.onEntityHit(entityHitResult);
        if (this.world.isClient) {
            return;
        }
        Entity lv = entityHitResult.getEntity();
        Entity lv2 = this.getOwner();
        if (lv2 instanceof LivingEntity) {
            lv3 = (LivingEntity)lv2;
            bl = lv.damage(this.getDamageSources().witherSkull(this, lv3), 8.0f);
            if (bl) {
                if (lv.isAlive()) {
                    this.applyDamageEffects(lv3, lv);
                } else {
                    lv3.heal(5.0f);
                }
            }
        } else {
            bl = lv.damage(this.getDamageSources().magic(), 5.0f);
        }
        if (bl && lv instanceof LivingEntity) {
            lv3 = (LivingEntity)lv;
            int i = 0;
            if (this.world.getDifficulty() == Difficulty.NORMAL) {
                i = 10;
            } else if (this.world.getDifficulty() == Difficulty.HARD) {
                i = 40;
            }
            if (i > 0) {
                lv3.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20 * i, 1), this.getEffectCause());
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.world.isClient) {
            this.world.createExplosion((Entity)this, this.getX(), this.getY(), this.getZ(), 1.0f, false, World.ExplosionSourceType.MOB);
            this.discard();
        }
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(CHARGED, false);
    }

    public boolean isCharged() {
        return this.dataTracker.get(CHARGED);
    }

    public void setCharged(boolean charged) {
        this.dataTracker.set(CHARGED, charged);
    }

    @Override
    protected boolean isBurning() {
        return false;
    }
}

