/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.LandingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FallingBlockEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private BlockState block = Blocks.SAND.getDefaultState();
    public int timeFalling;
    public boolean dropItem = true;
    private boolean destroyedOnLanding;
    private boolean hurtEntities;
    private int fallHurtMax = 40;
    private float fallHurtAmount;
    @Nullable
    public NbtCompound blockEntityData;
    protected static final TrackedData<BlockPos> BLOCK_POS = DataTracker.registerData(FallingBlockEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> arg, World arg2) {
        super(arg, arg2);
    }

    private FallingBlockEntity(World world, double x, double y, double z, BlockState block) {
        this((EntityType<? extends FallingBlockEntity>)EntityType.FALLING_BLOCK, world);
        this.block = block;
        this.intersectionChecked = true;
        this.setPosition(x, y, z);
        this.setVelocity(Vec3d.ZERO);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.setFallingBlockPos(this.getBlockPos());
    }

    public static FallingBlockEntity spawnFromBlock(World world, BlockPos pos, BlockState state) {
        FallingBlockEntity lv = new FallingBlockEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, state.contains(Properties.WATERLOGGED) ? (BlockState)state.with(Properties.WATERLOGGED, false) : state);
        world.setBlockState(pos, state.getFluidState().getBlockState(), Block.NOTIFY_ALL);
        world.spawnEntity(lv);
        return lv;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setFallingBlockPos(BlockPos pos) {
        this.dataTracker.set(BLOCK_POS, pos);
    }

    public BlockPos getFallingBlockPos() {
        return this.dataTracker.get(BLOCK_POS);
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(BLOCK_POS, BlockPos.ORIGIN);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        if (this.block.isAir()) {
            this.discard();
            return;
        }
        Block lv = this.block.getBlock();
        ++this.timeFalling;
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }
        this.move(MovementType.SELF, this.getVelocity());
        if (!this.world.isClient) {
            BlockHitResult lv3;
            BlockPos lv2 = this.getBlockPos();
            boolean bl = this.block.getBlock() instanceof ConcretePowderBlock;
            boolean bl2 = bl && this.world.getFluidState(lv2).isIn(FluidTags.WATER);
            double d = this.getVelocity().lengthSquared();
            if (bl && d > 1.0 && (lv3 = this.world.raycast(new RaycastContext(new Vec3d(this.prevX, this.prevY, this.prevZ), this.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, this))).getType() != HitResult.Type.MISS && this.world.getFluidState(lv3.getBlockPos()).isIn(FluidTags.WATER)) {
                lv2 = lv3.getBlockPos();
                bl2 = true;
            }
            if (this.onGround || bl2) {
                BlockState lv4 = this.world.getBlockState(lv2);
                this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
                if (!lv4.isOf(Blocks.MOVING_PISTON)) {
                    if (!this.destroyedOnLanding) {
                        boolean bl5;
                        boolean bl3 = lv4.canReplace(new AutomaticItemPlacementContext(this.world, lv2, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                        boolean bl4 = FallingBlock.canFallThrough(this.world.getBlockState(lv2.down())) && (!bl || !bl2);
                        boolean bl6 = bl5 = this.block.canPlaceAt(this.world, lv2) && !bl4;
                        if (bl3 && bl5) {
                            if (this.block.contains(Properties.WATERLOGGED) && this.world.getFluidState(lv2).getFluid() == Fluids.WATER) {
                                this.block = (BlockState)this.block.with(Properties.WATERLOGGED, true);
                            }
                            if (this.world.setBlockState(lv2, this.block, Block.NOTIFY_ALL)) {
                                BlockEntity lv5;
                                ((ServerWorld)this.world).getChunkManager().threadedAnvilChunkStorage.sendToOtherNearbyPlayers(this, new BlockUpdateS2CPacket(lv2, this.world.getBlockState(lv2)));
                                this.discard();
                                if (lv instanceof LandingBlock) {
                                    ((LandingBlock)((Object)lv)).onLanding(this.world, lv2, this.block, lv4, this);
                                }
                                if (this.blockEntityData != null && this.block.hasBlockEntity() && (lv5 = this.world.getBlockEntity(lv2)) != null) {
                                    NbtCompound lv6 = lv5.createNbt();
                                    for (String string : this.blockEntityData.getKeys()) {
                                        lv6.put(string, this.blockEntityData.get(string).copy());
                                    }
                                    try {
                                        lv5.readNbt(lv6);
                                    }
                                    catch (Exception exception) {
                                        LOGGER.error("Failed to load block entity from falling block", exception);
                                    }
                                    lv5.markDirty();
                                }
                            } else if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                this.discard();
                                this.onDestroyedOnLanding(lv, lv2);
                                this.dropItem(lv);
                            }
                        } else {
                            this.discard();
                            if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                this.onDestroyedOnLanding(lv, lv2);
                                this.dropItem(lv);
                            }
                        }
                    } else {
                        this.discard();
                        this.onDestroyedOnLanding(lv, lv2);
                    }
                }
            } else if (!(this.world.isClient || (this.timeFalling <= 100 || lv2.getY() > this.world.getBottomY() && lv2.getY() <= this.world.getTopY()) && this.timeFalling <= 600)) {
                if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                    this.dropItem(lv);
                }
                this.discard();
            }
        }
        this.setVelocity(this.getVelocity().multiply(0.98));
    }

    public void onDestroyedOnLanding(Block block, BlockPos pos) {
        if (block instanceof LandingBlock) {
            ((LandingBlock)((Object)block)).onDestroyedOnLanding(this.world, pos, this);
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        DamageSource lv2;
        Predicate<Entity> predicate;
        if (!this.hurtEntities) {
            return false;
        }
        int i = MathHelper.ceil(fallDistance - 1.0f);
        if (i < 0) {
            return false;
        }
        if (this.block.getBlock() instanceof LandingBlock) {
            LandingBlock lv = (LandingBlock)((Object)this.block.getBlock());
            predicate = lv.getEntityPredicate();
            lv2 = lv.getDamageSource(this);
        } else {
            predicate = EntityPredicates.EXCEPT_SPECTATOR;
            lv2 = this.getDamageSources().fallingBlock(this);
        }
        float h = Math.min(MathHelper.floor((float)i * this.fallHurtAmount), this.fallHurtMax);
        this.world.getOtherEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.damage(lv2, h));
        boolean bl = this.block.isIn(BlockTags.ANVIL);
        if (bl && h > 0.0f && this.random.nextFloat() < 0.05f + (float)i * 0.05f) {
            BlockState lv3 = AnvilBlock.getLandingState(this.block);
            if (lv3 == null) {
                this.destroyedOnLanding = true;
            } else {
                this.block = lv3;
            }
        }
        return false;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put("BlockState", NbtHelper.fromBlockState(this.block));
        nbt.putInt("Time", this.timeFalling);
        nbt.putBoolean("DropItem", this.dropItem);
        nbt.putBoolean("HurtEntities", this.hurtEntities);
        nbt.putFloat("FallHurtAmount", this.fallHurtAmount);
        nbt.putInt("FallHurtMax", this.fallHurtMax);
        if (this.blockEntityData != null) {
            nbt.put("TileEntityData", this.blockEntityData);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.block = NbtHelper.toBlockState(this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("BlockState"));
        this.timeFalling = nbt.getInt("Time");
        if (nbt.contains("HurtEntities", NbtElement.NUMBER_TYPE)) {
            this.hurtEntities = nbt.getBoolean("HurtEntities");
            this.fallHurtAmount = nbt.getFloat("FallHurtAmount");
            this.fallHurtMax = nbt.getInt("FallHurtMax");
        } else if (this.block.isIn(BlockTags.ANVIL)) {
            this.hurtEntities = true;
        }
        if (nbt.contains("DropItem", NbtElement.NUMBER_TYPE)) {
            this.dropItem = nbt.getBoolean("DropItem");
        }
        if (nbt.contains("TileEntityData", NbtElement.COMPOUND_TYPE)) {
            this.blockEntityData = nbt.getCompound("TileEntityData");
        }
        if (this.block.isAir()) {
            this.block = Blocks.SAND.getDefaultState();
        }
    }

    public void setHurtEntities(float fallHurtAmount, int fallHurtMax) {
        this.hurtEntities = true;
        this.fallHurtAmount = fallHurtAmount;
        this.fallHurtMax = fallHurtMax;
    }

    public void setDestroyedOnLanding() {
        this.destroyedOnLanding = true;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    @Override
    public void populateCrashReport(CrashReportSection section) {
        super.populateCrashReport(section);
        section.add("Immitating BlockState", this.block.toString());
    }

    public BlockState getBlockState() {
        return this.block;
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("entity.minecraft.falling_block_type", this.block.getBlock().getName());
    }

    @Override
    public boolean entityDataRequiresOperator() {
        return true;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, Block.getRawIdFromState(this.getBlockState()));
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.block = Block.getStateFromRawId(packet.getEntityData());
        this.intersectionChecked = true;
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.setPosition(d, e, f);
        this.setFallingBlockPos(this.getBlockPos());
    }
}

