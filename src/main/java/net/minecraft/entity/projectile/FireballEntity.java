/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class FireballEntity
extends AbstractFireballEntity {
    private int explosionPower = 1;

    public FireballEntity(EntityType<? extends FireballEntity> arg, World arg2) {
        super((EntityType<? extends AbstractFireballEntity>)arg, arg2);
    }

    public FireballEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ, int explosionPower) {
        super((EntityType<? extends AbstractFireballEntity>)EntityType.FIREBALL, owner, velocityX, velocityY, velocityZ, world);
        this.explosionPower = explosionPower;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.world.isClient) {
            boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
            this.world.createExplosion((Entity)this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, bl, World.ExplosionSourceType.MOB);
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.world.isClient) {
            return;
        }
        Entity lv = entityHitResult.getEntity();
        Entity lv2 = this.getOwner();
        lv.damage(this.getDamageSources().fireball(this, lv2), 6.0f);
        if (lv2 instanceof LivingEntity) {
            this.applyDamageEffects((LivingEntity)lv2, lv);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ExplosionPower", NbtElement.NUMBER_TYPE)) {
            this.explosionPower = nbt.getByte("ExplosionPower");
        }
    }
}

