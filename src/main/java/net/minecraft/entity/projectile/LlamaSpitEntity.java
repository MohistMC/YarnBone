/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LlamaSpitEntity
extends ProjectileEntity {
    public LlamaSpitEntity(EntityType<? extends LlamaSpitEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
    }

    public LlamaSpitEntity(World world, LlamaEntity owner) {
        this((EntityType<? extends LlamaSpitEntity>)EntityType.LLAMA_SPIT, world);
        this.setOwner(owner);
        this.setPosition(owner.getX() - (double)(owner.getWidth() + 1.0f) * 0.5 * (double)MathHelper.sin(owner.bodyYaw * ((float)Math.PI / 180)), owner.getEyeY() - (double)0.1f, owner.getZ() + (double)(owner.getWidth() + 1.0f) * 0.5 * (double)MathHelper.cos(owner.bodyYaw * ((float)Math.PI / 180)));
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d lv = this.getVelocity();
        HitResult lv2 = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(lv2);
        double d = this.getX() + lv.x;
        double e = this.getY() + lv.y;
        double f = this.getZ() + lv.z;
        this.updateRotation();
        float g = 0.99f;
        float h = 0.06f;
        if (this.world.getStatesInBox(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
            this.discard();
            return;
        }
        if (this.isInsideWaterOrBubbleColumn()) {
            this.discard();
            return;
        }
        this.setVelocity(lv.multiply(0.99f));
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.06f, 0.0));
        }
        this.setPosition(d, e, f);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            entityHitResult.getEntity().damage(this.getDamageSources().mobProjectile(this, lv), 1.0f);
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.world.isClient) {
            this.discard();
        }
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        double d = packet.getVelocityX();
        double e = packet.getVelocityY();
        double f = packet.getVelocityZ();
        for (int i = 0; i < 7; ++i) {
            double g = 0.4 + 0.1 * (double)i;
            this.world.addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), d * g, e, f * g);
        }
        this.setVelocity(d, e, f);
    }
}

