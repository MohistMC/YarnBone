/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class ThrownEntity
extends ProjectileEntity {
    protected ThrownEntity(EntityType<? extends ThrownEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
    }

    protected ThrownEntity(EntityType<? extends ThrownEntity> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    protected ThrownEntity(EntityType<? extends ThrownEntity> type, LivingEntity owner, World world) {
        this(type, owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ(), world);
        this.setOwner(owner);
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(e)) {
            e = 4.0;
        }
        return distance < (e *= 64.0) * e;
    }

    @Override
    public void tick() {
        float h;
        super.tick();
        HitResult lv = ProjectileUtil.getCollision(this, this::canHit);
        boolean bl = false;
        if (lv.getType() == HitResult.Type.BLOCK) {
            BlockPos lv2 = ((BlockHitResult)lv).getBlockPos();
            BlockState lv3 = this.world.getBlockState(lv2);
            if (lv3.isOf(Blocks.NETHER_PORTAL)) {
                this.setInNetherPortal(lv2);
                bl = true;
            } else if (lv3.isOf(Blocks.END_GATEWAY)) {
                BlockEntity lv4 = this.world.getBlockEntity(lv2);
                if (lv4 instanceof EndGatewayBlockEntity && EndGatewayBlockEntity.canTeleport(this)) {
                    EndGatewayBlockEntity.tryTeleportingEntity(this.world, lv2, lv3, this, (EndGatewayBlockEntity)lv4);
                }
                bl = true;
            }
        }
        if (lv.getType() != HitResult.Type.MISS && !bl) {
            this.onCollision(lv);
        }
        this.checkBlockCollision();
        Vec3d lv5 = this.getVelocity();
        double d = this.getX() + lv5.x;
        double e = this.getY() + lv5.y;
        double f = this.getZ() + lv5.z;
        this.updateRotation();
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                float g = 0.25f;
                this.world.addParticle(ParticleTypes.BUBBLE, d - lv5.x * 0.25, e - lv5.y * 0.25, f - lv5.z * 0.25, lv5.x, lv5.y, lv5.z);
            }
            h = 0.8f;
        } else {
            h = 0.99f;
        }
        this.setVelocity(lv5.multiply(h));
        if (!this.hasNoGravity()) {
            Vec3d lv6 = this.getVelocity();
            this.setVelocity(lv6.x, lv6.y - (double)this.getGravity(), lv6.z);
        }
        this.setPosition(d, e, f);
    }

    protected float getGravity() {
        return 0.03f;
    }
}

