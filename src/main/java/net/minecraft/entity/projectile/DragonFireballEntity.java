/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class DragonFireballEntity
extends ExplosiveProjectileEntity {
    public static final float DAMAGE_RANGE = 4.0f;

    public DragonFireballEntity(EntityType<? extends DragonFireballEntity> arg, World arg2) {
        super((EntityType<? extends ExplosiveProjectileEntity>)arg, arg2);
    }

    public DragonFireballEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ) {
        super(EntityType.DRAGON_FIREBALL, owner, directionX, directionY, directionZ, world);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (hitResult.getType() == HitResult.Type.ENTITY && this.isOwner(((EntityHitResult)hitResult).getEntity())) {
            return;
        }
        if (!this.world.isClient) {
            List<LivingEntity> list = this.world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(4.0, 2.0, 4.0));
            AreaEffectCloudEntity lv = new AreaEffectCloudEntity(this.world, this.getX(), this.getY(), this.getZ());
            Entity lv2 = this.getOwner();
            if (lv2 instanceof LivingEntity) {
                lv.setOwner((LivingEntity)lv2);
            }
            lv.setParticleType(ParticleTypes.DRAGON_BREATH);
            lv.setRadius(3.0f);
            lv.setDuration(600);
            lv.setRadiusGrowth((7.0f - lv.getRadius()) / (float)lv.getDuration());
            lv.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1));
            if (!list.isEmpty()) {
                for (LivingEntity lv3 : list) {
                    double d = this.squaredDistanceTo(lv3);
                    if (!(d < 16.0)) continue;
                    lv.setPosition(lv3.getX(), lv3.getY(), lv3.getZ());
                    break;
                }
            }
            this.world.syncWorldEvent(WorldEvents.DRAGON_BREATH_CLOUD_SPAWNS, this.getBlockPos(), this.isSilent() ? -1 : 1);
            this.world.spawnEntity(lv);
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
    protected ParticleEffect getParticleType() {
        return ParticleTypes.DRAGON_BREATH;
    }

    @Override
    protected boolean isBurning() {
        return false;
    }
}

