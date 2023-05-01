/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractDecorationEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final Predicate<Entity> PREDICATE = entity -> entity instanceof AbstractDecorationEntity;
    private int obstructionCheckCounter;
    protected BlockPos attachmentPos;
    protected Direction facing = Direction.SOUTH;

    protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> arg, World arg2) {
        super(arg, arg2);
    }

    protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> type, World world, BlockPos pos) {
        this(type, world);
        this.attachmentPos = pos;
    }

    @Override
    protected void initDataTracker() {
    }

    protected void setFacing(Direction facing) {
        Validate.notNull(facing);
        Validate.isTrue(facing.getAxis().isHorizontal());
        this.facing = facing;
        this.setYaw(this.facing.getHorizontal() * 90);
        this.prevYaw = this.getYaw();
        this.updateAttachmentPosition();
    }

    protected void updateAttachmentPosition() {
        if (this.facing == null) {
            return;
        }
        double d = (double)this.attachmentPos.getX() + 0.5;
        double e = (double)this.attachmentPos.getY() + 0.5;
        double f = (double)this.attachmentPos.getZ() + 0.5;
        double g = 0.46875;
        double h = this.method_6893(this.getWidthPixels());
        double i = this.method_6893(this.getHeightPixels());
        d -= (double)this.facing.getOffsetX() * 0.46875;
        f -= (double)this.facing.getOffsetZ() * 0.46875;
        Direction lv = this.facing.rotateYCounterclockwise();
        this.setPos(d += h * (double)lv.getOffsetX(), e += i, f += h * (double)lv.getOffsetZ());
        double j = this.getWidthPixels();
        double k = this.getHeightPixels();
        double l = this.getWidthPixels();
        if (this.facing.getAxis() == Direction.Axis.Z) {
            l = 1.0;
        } else {
            j = 1.0;
        }
        this.setBoundingBox(new Box(d - (j /= 32.0), e - (k /= 32.0), f - (l /= 32.0), d + j, e + k, f + l));
    }

    private double method_6893(int i) {
        return i % 32 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void tick() {
        if (!this.world.isClient) {
            this.attemptTickInVoid();
            if (this.obstructionCheckCounter++ == 100) {
                this.obstructionCheckCounter = 0;
                if (!this.isRemoved() && !this.canStayAttached()) {
                    this.discard();
                    this.onBreak(null);
                }
            }
        }
    }

    public boolean canStayAttached() {
        if (!this.world.isSpaceEmpty(this)) {
            return false;
        }
        int i = Math.max(1, this.getWidthPixels() / 16);
        int j = Math.max(1, this.getHeightPixels() / 16);
        BlockPos lv = this.attachmentPos.offset(this.facing.getOpposite());
        Direction lv2 = this.facing.rotateYCounterclockwise();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (int k = 0; k < i; ++k) {
            for (int l = 0; l < j; ++l) {
                int m = (i - 1) / -2;
                int n = (j - 1) / -2;
                lv3.set(lv).move(lv2, k + m).move(Direction.UP, l + n);
                BlockState lv4 = this.world.getBlockState(lv3);
                if (lv4.getMaterial().isSolid() || AbstractRedstoneGateBlock.isRedstoneGate(lv4)) continue;
                return false;
            }
        }
        return this.world.getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty();
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)attacker;
            if (!this.world.canPlayerModifyAt(lv, this.attachmentPos)) {
                return true;
            }
            return this.damage(this.getDamageSources().playerAttack(lv), 0.0f);
        }
        return false;
    }

    @Override
    public Direction getHorizontalFacing() {
        return this.facing;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.isRemoved() && !this.world.isClient) {
            this.kill();
            this.scheduleVelocityUpdate();
            this.onBreak(source.getAttacker());
        }
        return true;
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        if (!this.world.isClient && !this.isRemoved() && movement.lengthSquared() > 0.0) {
            this.kill();
            this.onBreak(null);
        }
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        if (!this.world.isClient && !this.isRemoved() && deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 0.0) {
            this.kill();
            this.onBreak(null);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        BlockPos lv = this.getDecorationBlockPos();
        nbt.putInt("TileX", lv.getX());
        nbt.putInt("TileY", lv.getY());
        nbt.putInt("TileZ", lv.getZ());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        BlockPos lv = new BlockPos(nbt.getInt("TileX"), nbt.getInt("TileY"), nbt.getInt("TileZ"));
        if (!lv.isWithinDistance(this.getBlockPos(), 16.0)) {
            LOGGER.error("Hanging entity at invalid position: {}", (Object)lv);
            return;
        }
        this.attachmentPos = lv;
    }

    public abstract int getWidthPixels();

    public abstract int getHeightPixels();

    public abstract void onBreak(@Nullable Entity var1);

    public abstract void onPlace();

    @Override
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        ItemEntity lv = new ItemEntity(this.world, this.getX() + (double)((float)this.facing.getOffsetX() * 0.15f), this.getY() + (double)yOffset, this.getZ() + (double)((float)this.facing.getOffsetZ() * 0.15f), stack);
        lv.setToDefaultPickupDelay();
        this.world.spawnEntity(lv);
        return lv;
    }

    @Override
    protected boolean shouldSetPositionOnLoad() {
        return false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.attachmentPos = BlockPos.ofFloored(x, y, z);
        this.updateAttachmentPosition();
        this.velocityDirty = true;
    }

    public BlockPos getDecorationBlockPos() {
        return this.attachmentPos;
    }

    @Override
    public float applyRotation(BlockRotation rotation) {
        if (this.facing.getAxis() != Direction.Axis.Y) {
            switch (rotation) {
                case CLOCKWISE_180: {
                    this.facing = this.facing.getOpposite();
                    break;
                }
                case COUNTERCLOCKWISE_90: {
                    this.facing = this.facing.rotateYCounterclockwise();
                    break;
                }
                case CLOCKWISE_90: {
                    this.facing = this.facing.rotateYClockwise();
                    break;
                }
            }
        }
        float f = MathHelper.wrapDegrees(this.getYaw());
        switch (rotation) {
            case CLOCKWISE_180: {
                return f + 180.0f;
            }
            case COUNTERCLOCKWISE_90: {
                return f + 90.0f;
            }
            case CLOCKWISE_90: {
                return f + 270.0f;
            }
        }
        return f;
    }

    @Override
    public float applyMirror(BlockMirror mirror) {
        return this.applyRotation(mirror.getRotation(this.facing));
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
    }

    @Override
    public void calculateDimensions() {
    }
}

