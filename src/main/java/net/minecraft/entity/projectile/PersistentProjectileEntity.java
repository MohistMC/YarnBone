/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class PersistentProjectileEntity
extends ProjectileEntity {
    private static final double field_30657 = 2.0;
    private static final TrackedData<Byte> PROJECTILE_FLAGS = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Byte> PIERCE_LEVEL = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int CRITICAL_FLAG = 1;
    private static final int NO_CLIP_FLAG = 2;
    private static final int SHOT_FROM_CROSSBOW_FLAG = 4;
    @Nullable
    private BlockState inBlockState;
    protected boolean inGround;
    protected int inGroundTime;
    public PickupPermission pickupType = PickupPermission.DISALLOWED;
    public int shake;
    private int life;
    private double damage = 2.0;
    private int punch;
    private SoundEvent sound = this.getHitSound();
    @Nullable
    private IntOpenHashSet piercedEntities;
    @Nullable
    private List<Entity> piercingKilledEntities;

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
    }

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, LivingEntity owner, World world) {
        this(type, owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ(), world);
        this.setOwner(owner);
        if (owner instanceof PlayerEntity) {
            this.pickupType = PickupPermission.ALLOWED;
        }
    }

    public void setSound(SoundEvent sound) {
        this.sound = sound;
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 10.0;
        if (Double.isNaN(e)) {
            e = 1.0;
        }
        return distance < (e *= 64.0 * PersistentProjectileEntity.getRenderDistanceMultiplier()) * e;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(PROJECTILE_FLAGS, (byte)0);
        this.dataTracker.startTracking(PIERCE_LEVEL, (byte)0);
    }

    @Override
    public void setVelocity(double x, double y, double z, float speed, float divergence) {
        super.setVelocity(x, y, z, speed, divergence);
        this.life = 0;
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        super.setVelocityClient(x, y, z);
        this.life = 0;
    }

    @Override
    public void tick() {
        Vec3d lv5;
        VoxelShape lv4;
        BlockPos lv2;
        BlockState lv3;
        super.tick();
        boolean bl = this.isNoClip();
        Vec3d lv = this.getVelocity();
        if (this.prevPitch == 0.0f && this.prevYaw == 0.0f) {
            double d = lv.horizontalLength();
            this.setYaw((float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875));
            this.setPitch((float)(MathHelper.atan2(lv.y, d) * 57.2957763671875));
            this.prevYaw = this.getYaw();
            this.prevPitch = this.getPitch();
        }
        if (!((lv3 = this.world.getBlockState(lv2 = this.getBlockPos())).isAir() || bl || (lv4 = lv3.getCollisionShape(this.world, lv2)).isEmpty())) {
            lv5 = this.getPos();
            for (Box lv6 : lv4.getBoundingBoxes()) {
                if (!lv6.offset(lv2).contains(lv5)) continue;
                this.inGround = true;
                break;
            }
        }
        if (this.shake > 0) {
            --this.shake;
        }
        if (this.isTouchingWaterOrRain() || lv3.isOf(Blocks.POWDER_SNOW)) {
            this.extinguish();
        }
        if (this.inGround && !bl) {
            if (this.inBlockState != lv3 && this.shouldFall()) {
                this.fall();
            } else if (!this.world.isClient) {
                this.age();
            }
            ++this.inGroundTime;
            return;
        }
        this.inGroundTime = 0;
        Vec3d lv7 = this.getPos();
        HitResult lv8 = this.world.raycast(new RaycastContext(lv7, lv5 = lv7.add(lv), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        if (lv8.getType() != HitResult.Type.MISS) {
            lv5 = lv8.getPos();
        }
        while (!this.isRemoved()) {
            EntityHitResult lv9 = this.getEntityCollision(lv7, lv5);
            if (lv9 != null) {
                lv8 = lv9;
            }
            if (lv8 != null && lv8.getType() == HitResult.Type.ENTITY) {
                Entity lv10 = ((EntityHitResult)lv8).getEntity();
                Entity lv11 = this.getOwner();
                if (lv10 instanceof PlayerEntity && lv11 instanceof PlayerEntity && !((PlayerEntity)lv11).shouldDamagePlayer((PlayerEntity)lv10)) {
                    lv8 = null;
                    lv9 = null;
                }
            }
            if (lv8 != null && !bl) {
                this.onCollision(lv8);
                this.velocityDirty = true;
            }
            if (lv9 == null || this.getPierceLevel() <= 0) break;
            lv8 = null;
        }
        lv = this.getVelocity();
        double e = lv.x;
        double f = lv.y;
        double g = lv.z;
        if (this.isCritical()) {
            for (int i = 0; i < 4; ++i) {
                this.world.addParticle(ParticleTypes.CRIT, this.getX() + e * (double)i / 4.0, this.getY() + f * (double)i / 4.0, this.getZ() + g * (double)i / 4.0, -e, -f + 0.2, -g);
            }
        }
        double h = this.getX() + e;
        double j = this.getY() + f;
        double k = this.getZ() + g;
        double l = lv.horizontalLength();
        if (bl) {
            this.setYaw((float)(MathHelper.atan2(-e, -g) * 57.2957763671875));
        } else {
            this.setYaw((float)(MathHelper.atan2(e, g) * 57.2957763671875));
        }
        this.setPitch((float)(MathHelper.atan2(f, l) * 57.2957763671875));
        this.setPitch(PersistentProjectileEntity.updateRotation(this.prevPitch, this.getPitch()));
        this.setYaw(PersistentProjectileEntity.updateRotation(this.prevYaw, this.getYaw()));
        float m = 0.99f;
        float n = 0.05f;
        if (this.isTouchingWater()) {
            for (int o = 0; o < 4; ++o) {
                float p = 0.25f;
                this.world.addParticle(ParticleTypes.BUBBLE, h - e * 0.25, j - f * 0.25, k - g * 0.25, e, f, g);
            }
            m = this.getDragInWater();
        }
        this.setVelocity(lv.multiply(m));
        if (!this.hasNoGravity() && !bl) {
            Vec3d lv12 = this.getVelocity();
            this.setVelocity(lv12.x, lv12.y - (double)0.05f, lv12.z);
        }
        this.setPosition(h, j, k);
        this.checkBlockCollision();
    }

    private boolean shouldFall() {
        return this.inGround && this.world.isSpaceEmpty(new Box(this.getPos(), this.getPos()).expand(0.06));
    }

    private void fall() {
        this.inGround = false;
        Vec3d lv = this.getVelocity();
        this.setVelocity(lv.multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        this.life = 0;
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        super.move(movementType, movement);
        if (movementType != MovementType.SELF && this.shouldFall()) {
            this.fall();
        }
    }

    protected void age() {
        ++this.life;
        if (this.life >= 1200) {
            this.discard();
        }
    }

    private void clearPiercingStatus() {
        if (this.piercingKilledEntities != null) {
            this.piercingKilledEntities.clear();
        }
        if (this.piercedEntities != null) {
            this.piercedEntities.clear();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        DamageSource lv3;
        Entity lv2;
        super.onEntityHit(entityHitResult);
        Entity lv = entityHitResult.getEntity();
        float f = (float)this.getVelocity().length();
        int i = MathHelper.ceil(MathHelper.clamp((double)f * this.damage, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercedEntities == null) {
                this.piercedEntities = new IntOpenHashSet(5);
            }
            if (this.piercingKilledEntities == null) {
                this.piercingKilledEntities = Lists.newArrayListWithCapacity(5);
            }
            if (this.piercedEntities.size() < this.getPierceLevel() + 1) {
                this.piercedEntities.add(lv.getId());
            } else {
                this.discard();
                return;
            }
        }
        if (this.isCritical()) {
            long l = this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(l + (long)i, Integer.MAX_VALUE);
        }
        if ((lv2 = this.getOwner()) == null) {
            lv3 = this.getDamageSources().arrow(this, this);
        } else {
            lv3 = this.getDamageSources().arrow(this, lv2);
            if (lv2 instanceof LivingEntity) {
                ((LivingEntity)lv2).onAttacking(lv);
            }
        }
        boolean bl = lv.getType() == EntityType.ENDERMAN;
        int j = lv.getFireTicks();
        if (this.isOnFire() && !bl) {
            lv.setOnFireFor(5);
        }
        if (lv.damage(lv3, i)) {
            if (bl) {
                return;
            }
            if (lv instanceof LivingEntity) {
                LivingEntity lv4 = (LivingEntity)lv;
                if (!this.world.isClient && this.getPierceLevel() <= 0) {
                    lv4.setStuckArrowCount(lv4.getStuckArrowCount() + 1);
                }
                if (this.punch > 0) {
                    double d = Math.max(0.0, 1.0 - lv4.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                    Vec3d lv5 = this.getVelocity().multiply(1.0, 0.0, 1.0).normalize().multiply((double)this.punch * 0.6 * d);
                    if (lv5.lengthSquared() > 0.0) {
                        lv4.addVelocity(lv5.x, 0.1, lv5.z);
                    }
                }
                if (!this.world.isClient && lv2 instanceof LivingEntity) {
                    EnchantmentHelper.onUserDamaged(lv4, lv2);
                    EnchantmentHelper.onTargetDamaged((LivingEntity)lv2, lv4);
                }
                this.onHit(lv4);
                if (lv2 != null && lv4 != lv2 && lv4 instanceof PlayerEntity && lv2 instanceof ServerPlayerEntity && !this.isSilent()) {
                    ((ServerPlayerEntity)lv2).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, GameStateChangeS2CPacket.field_33328));
                }
                if (!lv.isAlive() && this.piercingKilledEntities != null) {
                    this.piercingKilledEntities.add(lv4);
                }
                if (!this.world.isClient && lv2 instanceof ServerPlayerEntity) {
                    ServerPlayerEntity lv6 = (ServerPlayerEntity)lv2;
                    if (this.piercingKilledEntities != null && this.isShotFromCrossbow()) {
                        Criteria.KILLED_BY_CROSSBOW.trigger(lv6, this.piercingKilledEntities);
                    } else if (!lv.isAlive() && this.isShotFromCrossbow()) {
                        Criteria.KILLED_BY_CROSSBOW.trigger(lv6, Arrays.asList(lv));
                    }
                }
            }
            this.playSound(this.sound, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            lv.setFireTicks(j);
            this.setVelocity(this.getVelocity().multiply(-0.1));
            this.setYaw(this.getYaw() + 180.0f);
            this.prevYaw += 180.0f;
            if (!this.world.isClient && this.getVelocity().lengthSquared() < 1.0E-7) {
                if (this.pickupType == PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1f);
                }
                this.discard();
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.inBlockState = this.world.getBlockState(blockHitResult.getBlockPos());
        super.onBlockHit(blockHitResult);
        Vec3d lv = blockHitResult.getPos().subtract(this.getX(), this.getY(), this.getZ());
        this.setVelocity(lv);
        Vec3d lv2 = lv.normalize().multiply(0.05f);
        this.setPos(this.getX() - lv2.x, this.getY() - lv2.y, this.getZ() - lv2.z);
        this.playSound(this.getSound(), 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
        this.inGround = true;
        this.shake = 7;
        this.setCritical(false);
        this.setPierceLevel((byte)0);
        this.setSound(SoundEvents.ENTITY_ARROW_HIT);
        this.setShotFromCrossbow(false);
        this.clearPiercingStatus();
    }

    protected SoundEvent getHitSound() {
        return SoundEvents.ENTITY_ARROW_HIT;
    }

    protected final SoundEvent getSound() {
        return this.sound;
    }

    protected void onHit(LivingEntity target) {
    }

    @Nullable
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return ProjectileUtil.getEntityCollision(this.world, this, currentPosition, nextPosition, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), this::canHit);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && (this.piercedEntities == null || !this.piercedEntities.contains(entity.getId()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putShort("life", (short)this.life);
        if (this.inBlockState != null) {
            nbt.put("inBlockState", NbtHelper.fromBlockState(this.inBlockState));
        }
        nbt.putByte("shake", (byte)this.shake);
        nbt.putBoolean("inGround", this.inGround);
        nbt.putByte("pickup", (byte)this.pickupType.ordinal());
        nbt.putDouble("damage", this.damage);
        nbt.putBoolean("crit", this.isCritical());
        nbt.putByte("PierceLevel", this.getPierceLevel());
        nbt.putString("SoundEvent", Registries.SOUND_EVENT.getId(this.sound).toString());
        nbt.putBoolean("ShotFromCrossbow", this.isShotFromCrossbow());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.life = nbt.getShort("life");
        if (nbt.contains("inBlockState", NbtElement.COMPOUND_TYPE)) {
            this.inBlockState = NbtHelper.toBlockState(this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("inBlockState"));
        }
        this.shake = nbt.getByte("shake") & 0xFF;
        this.inGround = nbt.getBoolean("inGround");
        if (nbt.contains("damage", NbtElement.NUMBER_TYPE)) {
            this.damage = nbt.getDouble("damage");
        }
        this.pickupType = PickupPermission.fromOrdinal(nbt.getByte("pickup"));
        this.setCritical(nbt.getBoolean("crit"));
        this.setPierceLevel(nbt.getByte("PierceLevel"));
        if (nbt.contains("SoundEvent", NbtElement.STRING_TYPE)) {
            this.sound = Registries.SOUND_EVENT.getOrEmpty(new Identifier(nbt.getString("SoundEvent"))).orElse(this.getHitSound());
        }
        this.setShotFromCrossbow(nbt.getBoolean("ShotFromCrossbow"));
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        if (entity instanceof PlayerEntity) {
            this.pickupType = ((PlayerEntity)entity).getAbilities().creativeMode ? PickupPermission.CREATIVE_ONLY : PickupPermission.ALLOWED;
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (this.world.isClient || !this.inGround && !this.isNoClip() || this.shake > 0) {
            return;
        }
        if (this.tryPickup(player)) {
            player.sendPickup(this, 1);
            this.discard();
        }
    }

    protected boolean tryPickup(PlayerEntity player) {
        switch (this.pickupType) {
            case ALLOWED: {
                return player.getInventory().insertStack(this.asItemStack());
            }
            case CREATIVE_ONLY: {
                return player.getAbilities().creativeMode;
            }
        }
        return false;
    }

    protected abstract ItemStack asItemStack();

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamage() {
        return this.damage;
    }

    public void setPunch(int punch) {
        this.punch = punch;
    }

    public int getPunch() {
        return this.punch;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0.13f;
    }

    public void setCritical(boolean critical) {
        this.setProjectileFlag(CRITICAL_FLAG, critical);
    }

    public void setPierceLevel(byte level) {
        this.dataTracker.set(PIERCE_LEVEL, level);
    }

    private void setProjectileFlag(int index, boolean flag) {
        byte b = this.dataTracker.get(PROJECTILE_FLAGS);
        if (flag) {
            this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b | index));
        } else {
            this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b & ~index));
        }
    }

    public boolean isCritical() {
        byte b = this.dataTracker.get(PROJECTILE_FLAGS);
        return (b & 1) != 0;
    }

    public boolean isShotFromCrossbow() {
        byte b = this.dataTracker.get(PROJECTILE_FLAGS);
        return (b & 4) != 0;
    }

    public byte getPierceLevel() {
        return this.dataTracker.get(PIERCE_LEVEL);
    }

    public void applyEnchantmentEffects(LivingEntity entity, float damageModifier) {
        int i = EnchantmentHelper.getEquipmentLevel(Enchantments.POWER, entity);
        int j = EnchantmentHelper.getEquipmentLevel(Enchantments.PUNCH, entity);
        this.setDamage((double)(damageModifier * 2.0f) + this.random.nextTriangular((double)this.world.getDifficulty().getId() * 0.11, 0.57425));
        if (i > 0) {
            this.setDamage(this.getDamage() + (double)i * 0.5 + 0.5);
        }
        if (j > 0) {
            this.setPunch(j);
        }
        if (EnchantmentHelper.getEquipmentLevel(Enchantments.FLAME, entity) > 0) {
            this.setOnFireFor(100);
        }
    }

    protected float getDragInWater() {
        return 0.6f;
    }

    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
        this.setProjectileFlag(NO_CLIP_FLAG, noClip);
    }

    public boolean isNoClip() {
        if (!this.world.isClient) {
            return this.noClip;
        }
        return (this.dataTracker.get(PROJECTILE_FLAGS) & 2) != 0;
    }

    public void setShotFromCrossbow(boolean shotFromCrossbow) {
        this.setProjectileFlag(SHOT_FROM_CROSSBOW_FLAG, shotFromCrossbow);
    }

    public static enum PickupPermission {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;


        public static PickupPermission fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal > PickupPermission.values().length) {
                ordinal = 0;
            }
            return PickupPermission.values()[ordinal];
        }
    }
}

