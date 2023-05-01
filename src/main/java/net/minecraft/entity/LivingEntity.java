/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.AttackPosOffsettingMount;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.CollisionView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class LivingEntity
extends Entity
implements Attackable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final UUID SPRINTING_SPEED_BOOST_ID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final UUID SOUL_SPEED_BOOST_ID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
    private static final UUID POWDER_SNOW_SLOW_ID = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4ce");
    private static final EntityAttributeModifier SPRINTING_SPEED_BOOST = new EntityAttributeModifier(SPRINTING_SPEED_BOOST_ID, "Sprinting speed boost", (double)0.3f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final int field_30069 = 2;
    public static final int field_30070 = 4;
    public static final int EQUIPMENT_SLOT_ID = 98;
    public static final int field_30072 = 100;
    public static final int GLOWING_FLAG = 6;
    public static final int field_30074 = 100;
    private static final int field_30078 = 40;
    public static final double field_30075 = 0.003;
    public static final double GRAVITY = 0.08;
    public static final int DEATH_TICKS = 20;
    private static final int FALL_FLYING_FLAG = 7;
    private static final int field_30080 = 10;
    private static final int field_30081 = 2;
    public static final int field_30063 = 4;
    private static final double MAX_ENTITY_VIEWING_DISTANCE = 128.0;
    protected static final int USING_ITEM_FLAG = 1;
    protected static final int OFF_HAND_ACTIVE_FLAG = 2;
    protected static final int USING_RIPTIDE_FLAG = 4;
    protected static final TrackedData<Byte> LIVING_FLAGS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Float> HEALTH = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> POTION_SWIRLS_COLOR = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> POTION_SWIRLS_AMBIENT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> STUCK_ARROW_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> STINGER_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<BlockPos>> SLEEPING_POSITION = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    protected static final float field_30067 = 1.74f;
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2f, 0.2f);
    public static final float BABY_SCALE_FACTOR = 0.5f;
    private static final int field_42636 = 50;
    private final AttributeContainer attributes;
    private final DamageTracker damageTracker = new DamageTracker(this);
    private final Map<StatusEffect, StatusEffectInstance> activeStatusEffects = Maps.newHashMap();
    private final DefaultedList<ItemStack> syncedHandStacks = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> syncedArmorStacks = DefaultedList.ofSize(4, ItemStack.EMPTY);
    public boolean handSwinging;
    private boolean noDrag = false;
    public Hand preferredHand;
    public int handSwingTicks;
    public int stuckArrowTimer;
    public int stuckStingerTimer;
    public int hurtTime;
    public int maxHurtTime;
    public int deathTime;
    public float lastHandSwingProgress;
    public float handSwingProgress;
    protected int lastAttackedTicks;
    public final LimbAnimator limbAnimator = new LimbAnimator();
    public final int defaultMaxHealth = 20;
    public final float randomLargeSeed;
    public final float randomSmallSeed;
    public float bodyYaw;
    public float prevBodyYaw;
    public float headYaw;
    public float prevHeadYaw;
    @Nullable
    protected PlayerEntity attackingPlayer;
    protected int playerHitTimer;
    protected boolean dead;
    protected int despawnCounter;
    protected float prevStepBobbingAmount;
    protected float stepBobbingAmount;
    protected float lookDirection;
    protected float prevLookDirection;
    protected float field_6215;
    protected int scoreAmount;
    protected float lastDamageTaken;
    protected boolean jumping;
    public float sidewaysSpeed;
    public float upwardSpeed;
    public float forwardSpeed;
    protected int bodyTrackingIncrements;
    protected double serverX;
    protected double serverY;
    protected double serverZ;
    protected double serverYaw;
    protected double serverPitch;
    protected double serverHeadYaw;
    protected int headTrackingIncrements;
    private boolean effectsChanged = true;
    @Nullable
    private LivingEntity attacker;
    private int lastAttackedTime;
    private LivingEntity attacking;
    private int lastAttackTime;
    private float movementSpeed;
    private int jumpingCooldown;
    private float absorptionAmount;
    protected ItemStack activeItemStack = ItemStack.EMPTY;
    protected int itemUseTimeLeft;
    protected int roll;
    private BlockPos lastBlockPos;
    private Optional<BlockPos> climbingPos = Optional.empty();
    @Nullable
    private DamageSource lastDamageSource;
    private long lastDamageTime;
    protected int riptideTicks;
    private float leaningPitch;
    private float lastLeaningPitch;
    protected Brain<?> brain;
    private boolean experienceDroppingDisabled;

    protected LivingEntity(EntityType<? extends LivingEntity> arg, World arg2) {
        super(arg, arg2);
        this.attributes = new AttributeContainer(DefaultAttributeRegistry.get(arg));
        this.setHealth(this.getMaxHealth());
        this.intersectionChecked = true;
        this.randomSmallSeed = (float)((Math.random() + 1.0) * (double)0.01f);
        this.refreshPosition();
        this.randomLargeSeed = (float)Math.random() * 12398.0f;
        this.setYaw((float)(Math.random() * 6.2831854820251465));
        this.headYaw = this.getYaw();
        this.setStepHeight(0.6f);
        NbtOps lv = NbtOps.INSTANCE;
        this.brain = this.deserializeBrain(new Dynamic<NbtElement>(lv, lv.createMap(ImmutableMap.of(lv.createString("memories"), (NbtElement)lv.emptyMap()))));
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain.Profile<?> createBrainProfile() {
        return Brain.createProfile(ImmutableList.of(), ImmutableList.of());
    }

    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return this.createBrainProfile().deserialize(dynamic);
    }

    @Override
    public void kill() {
        this.damage(this.getDamageSources().outOfWorld(), Float.MAX_VALUE);
    }

    public boolean canTarget(EntityType<?> type) {
        return true;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(LIVING_FLAGS, (byte)0);
        this.dataTracker.startTracking(POTION_SWIRLS_COLOR, 0);
        this.dataTracker.startTracking(POTION_SWIRLS_AMBIENT, false);
        this.dataTracker.startTracking(STUCK_ARROW_COUNT, 0);
        this.dataTracker.startTracking(STINGER_COUNT, 0);
        this.dataTracker.startTracking(HEALTH, Float.valueOf(1.0f));
        this.dataTracker.startTracking(SLEEPING_POSITION, Optional.empty());
    }

    public static DefaultAttributeContainer.Builder createLivingAttributes() {
        return DefaultAttributeContainer.builder().add(EntityAttributes.GENERIC_MAX_HEALTH).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).add(EntityAttributes.GENERIC_MOVEMENT_SPEED).add(EntityAttributes.GENERIC_ARMOR).add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if (!this.isTouchingWater()) {
            this.checkWaterState();
        }
        if (!this.world.isClient && onGround && this.fallDistance > 0.0f) {
            this.removeSoulSpeedBoost();
            this.addSoulSpeedBoostIfNeeded();
        }
        if (!this.world.isClient && this.fallDistance > 3.0f && onGround) {
            float f = MathHelper.ceil(this.fallDistance - 3.0f);
            if (!state.isAir()) {
                double e = Math.min((double)(0.2f + f / 15.0f), 2.5);
                int i = (int)(150.0 * e);
                ((ServerWorld)this.world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), this.getX(), this.getY(), this.getZ(), i, 0.0, 0.0, 0.0, 0.15f);
            }
        }
        super.fall(heightDifference, onGround, state, landedPosition);
    }

    public boolean canBreatheInWater() {
        return this.getGroup() == EntityGroup.UNDEAD;
    }

    public float getLeaningPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastLeaningPitch, this.leaningPitch);
    }

    @Override
    public void baseTick() {
        this.lastHandSwingProgress = this.handSwingProgress;
        if (this.firstUpdate) {
            this.getSleepingPosition().ifPresent(this::setPositionInBed);
        }
        if (this.shouldDisplaySoulSpeedEffects()) {
            this.displaySoulSpeedEffects();
        }
        super.baseTick();
        this.world.getProfiler().push("livingEntityBaseTick");
        if (this.isFireImmune() || this.world.isClient) {
            this.extinguish();
        }
        if (this.isAlive()) {
            BlockPos lv2;
            boolean bl = this instanceof PlayerEntity;
            if (!this.world.isClient) {
                double e;
                double d;
                if (this.isInsideWall()) {
                    this.damage(this.getDamageSources().inWall(), 1.0f);
                } else if (bl && !this.world.getWorldBorder().contains(this.getBoundingBox()) && (d = this.world.getWorldBorder().getDistanceInsideBorder(this) + this.world.getWorldBorder().getSafeZone()) < 0.0 && (e = this.world.getWorldBorder().getDamagePerBlock()) > 0.0) {
                    this.damage(this.getDamageSources().inWall(), Math.max(1, MathHelper.floor(-d * e)));
                }
            }
            if (this.isSubmergedIn(FluidTags.WATER) && !this.world.getBlockState(BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ())).isOf(Blocks.BUBBLE_COLUMN)) {
                boolean bl2;
                boolean bl3 = bl2 = !this.canBreatheInWater() && !StatusEffectUtil.hasWaterBreathing(this) && (!bl || !((PlayerEntity)this).getAbilities().invulnerable);
                if (bl2) {
                    this.setAir(this.getNextAirUnderwater(this.getAir()));
                    if (this.getAir() == -20) {
                        this.setAir(0);
                        Vec3d lv = this.getVelocity();
                        for (int i = 0; i < 8; ++i) {
                            double f = this.random.nextDouble() - this.random.nextDouble();
                            double g = this.random.nextDouble() - this.random.nextDouble();
                            double h = this.random.nextDouble() - this.random.nextDouble();
                            this.world.addParticle(ParticleTypes.BUBBLE, this.getX() + f, this.getY() + g, this.getZ() + h, lv.x, lv.y, lv.z);
                        }
                        this.damage(this.getDamageSources().drown(), 2.0f);
                    }
                }
                if (!this.world.isClient && this.hasVehicle() && this.getVehicle() != null && this.getVehicle().shouldDismountUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAir() < this.getMaxAir()) {
                this.setAir(this.getNextAirOnLand(this.getAir()));
            }
            if (!this.world.isClient && !Objects.equal(this.lastBlockPos, lv2 = this.getBlockPos())) {
                this.lastBlockPos = lv2;
                this.applyMovementEffects(lv2);
            }
        }
        if (this.isAlive() && (this.isWet() || this.inPowderSnow)) {
            this.extinguishWithSound();
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.timeUntilRegen > 0 && !(this instanceof ServerPlayerEntity)) {
            --this.timeUntilRegen;
        }
        if (this.isDead() && this.world.shouldUpdatePostDeath(this)) {
            this.updatePostDeath();
        }
        if (this.playerHitTimer > 0) {
            --this.playerHitTimer;
        } else {
            this.attackingPlayer = null;
        }
        if (this.attacking != null && !this.attacking.isAlive()) {
            this.attacking = null;
        }
        if (this.attacker != null) {
            if (!this.attacker.isAlive()) {
                this.setAttacker(null);
            } else if (this.age - this.lastAttackedTime > 100) {
                this.setAttacker(null);
            }
        }
        this.tickStatusEffects();
        this.prevLookDirection = this.lookDirection;
        this.prevBodyYaw = this.bodyYaw;
        this.prevHeadYaw = this.headYaw;
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
        this.world.getProfiler().pop();
    }

    public boolean shouldDisplaySoulSpeedEffects() {
        return this.age % 5 == 0 && this.getVelocity().x != 0.0 && this.getVelocity().z != 0.0 && !this.isSpectator() && EnchantmentHelper.hasSoulSpeed(this) && this.isOnSoulSpeedBlock();
    }

    protected void displaySoulSpeedEffects() {
        Vec3d lv = this.getVelocity();
        this.world.addParticle(ParticleTypes.SOUL, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getWidth(), this.getY() + 0.1, this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getWidth(), lv.x * -0.2, 0.1, lv.z * -0.2);
        float f = this.random.nextFloat() * 0.4f + this.random.nextFloat() > 0.9f ? 0.6f : 0.0f;
        this.playSound(SoundEvents.PARTICLE_SOUL_ESCAPE, f, 0.6f + this.random.nextFloat() * 0.4f);
    }

    protected boolean isOnSoulSpeedBlock() {
        return this.world.getBlockState(this.getVelocityAffectingPos()).isIn(BlockTags.SOUL_SPEED_BLOCKS);
    }

    @Override
    protected float getVelocityMultiplier() {
        if (this.isOnSoulSpeedBlock() && EnchantmentHelper.getEquipmentLevel(Enchantments.SOUL_SPEED, this) > 0) {
            return 1.0f;
        }
        return super.getVelocityMultiplier();
    }

    protected boolean shouldRemoveSoulSpeedBoost(BlockState landingState) {
        return !landingState.isAir() || this.isFallFlying();
    }

    protected void removeSoulSpeedBoost() {
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (lv == null) {
            return;
        }
        if (lv.getModifier(SOUL_SPEED_BOOST_ID) != null) {
            lv.removeModifier(SOUL_SPEED_BOOST_ID);
        }
    }

    protected void addSoulSpeedBoostIfNeeded() {
        int i;
        if (!this.getLandingBlockState().isAir() && (i = EnchantmentHelper.getEquipmentLevel(Enchantments.SOUL_SPEED, this)) > 0 && this.isOnSoulSpeedBlock()) {
            EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (lv == null) {
                return;
            }
            lv.addTemporaryModifier(new EntityAttributeModifier(SOUL_SPEED_BOOST_ID, "Soul speed boost", (double)(0.03f * (1.0f + (float)i * 0.35f)), EntityAttributeModifier.Operation.ADDITION));
            if (this.getRandom().nextFloat() < 0.04f) {
                ItemStack lv2 = this.getEquippedStack(EquipmentSlot.FEET);
                lv2.damage(1, this, player -> player.sendEquipmentBreakStatus(EquipmentSlot.FEET));
            }
        }
    }

    protected void removePowderSnowSlow() {
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (lv == null) {
            return;
        }
        if (lv.getModifier(POWDER_SNOW_SLOW_ID) != null) {
            lv.removeModifier(POWDER_SNOW_SLOW_ID);
        }
    }

    protected void addPowderSnowSlowIfNeeded() {
        int i;
        if (!this.getLandingBlockState().isAir() && (i = this.getFrozenTicks()) > 0) {
            EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (lv == null) {
                return;
            }
            float f = -0.05f * this.getFreezingScale();
            lv.addTemporaryModifier(new EntityAttributeModifier(POWDER_SNOW_SLOW_ID, "Powder snow slow", (double)f, EntityAttributeModifier.Operation.ADDITION));
        }
    }

    protected void applyMovementEffects(BlockPos pos) {
        int i = EnchantmentHelper.getEquipmentLevel(Enchantments.FROST_WALKER, this);
        if (i > 0) {
            FrostWalkerEnchantment.freezeWater(this, this.world, pos, i);
        }
        if (this.shouldRemoveSoulSpeedBoost(this.getLandingBlockState())) {
            this.removeSoulSpeedBoost();
        }
        this.addSoulSpeedBoostIfNeeded();
    }

    public boolean isBaby() {
        return false;
    }

    public float getScaleFactor() {
        return this.isBaby() ? 0.5f : 1.0f;
    }

    protected boolean shouldSwimInFluids() {
        return true;
    }

    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.world.isClient() && !this.isRemoved()) {
            this.world.sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean shouldDropXp() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot() {
        return !this.isBaby();
    }

    protected int getNextAirUnderwater(int air) {
        int j = EnchantmentHelper.getRespiration(this);
        if (j > 0 && this.random.nextInt(j + 1) > 0) {
            return air;
        }
        return air - 1;
    }

    protected int getNextAirOnLand(int air) {
        return Math.min(air + 4, this.getMaxAir());
    }

    public int getXpToDrop() {
        return 0;
    }

    protected boolean shouldAlwaysDropXp() {
        return false;
    }

    public Random getRandom() {
        return this.random;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return this.attacker;
    }

    @Override
    public LivingEntity getLastAttacker() {
        return this.getAttacker();
    }

    public int getLastAttackedTime() {
        return this.lastAttackedTime;
    }

    public void setAttacking(@Nullable PlayerEntity attacking) {
        this.attackingPlayer = attacking;
        this.playerHitTimer = this.age;
    }

    public void setAttacker(@Nullable LivingEntity attacker) {
        this.attacker = attacker;
        this.lastAttackedTime = this.age;
    }

    @Nullable
    public LivingEntity getAttacking() {
        return this.attacking;
    }

    public int getLastAttackTime() {
        return this.lastAttackTime;
    }

    public void onAttacking(Entity target) {
        this.attacking = target instanceof LivingEntity ? (LivingEntity)target : null;
        this.lastAttackTime = this.age;
    }

    public int getDespawnCounter() {
        return this.despawnCounter;
    }

    public void setDespawnCounter(int despawnCounter) {
        this.despawnCounter = despawnCounter;
    }

    public boolean hasNoDrag() {
        return this.noDrag;
    }

    public void setNoDrag(boolean noDrag) {
        this.noDrag = noDrag;
    }

    protected boolean isArmorSlot(EquipmentSlot slot) {
        return true;
    }

    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        boolean bl;
        boolean bl2 = bl = newStack.isEmpty() && oldStack.isEmpty();
        if (bl || ItemStack.canCombine(oldStack, newStack) || this.firstUpdate) {
            return;
        }
        Equipment lv = Equipment.fromStack(newStack);
        if (lv != null && !this.isSpectator() && lv.getSlotType() == slot) {
            if (!this.world.isClient() && !this.isSilent()) {
                this.world.playSound(null, this.getX(), this.getY(), this.getZ(), lv.getEquipSound(), this.getSoundCategory(), 1.0f, 1.0f);
            }
            if (this.isArmorSlot(slot)) {
                this.emitGameEvent(GameEvent.EQUIP);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        this.brain.forgetAll();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("Health", this.getHealth());
        nbt.putShort("HurtTime", (short)this.hurtTime);
        nbt.putInt("HurtByTimestamp", this.lastAttackedTime);
        nbt.putShort("DeathTime", (short)this.deathTime);
        nbt.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        nbt.put("Attributes", this.getAttributes().toNbt());
        if (!this.activeStatusEffects.isEmpty()) {
            NbtList lv = new NbtList();
            for (StatusEffectInstance lv2 : this.activeStatusEffects.values()) {
                lv.add(lv2.writeNbt(new NbtCompound()));
            }
            nbt.put("ActiveEffects", lv);
        }
        nbt.putBoolean("FallFlying", this.isFallFlying());
        this.getSleepingPosition().ifPresent(pos -> {
            nbt.putInt("SleepingX", pos.getX());
            nbt.putInt("SleepingY", pos.getY());
            nbt.putInt("SleepingZ", pos.getZ());
        });
        DataResult<NbtElement> dataResult = this.brain.encode(NbtOps.INSTANCE);
        dataResult.resultOrPartial(LOGGER::error).ifPresent(brain -> nbt.put("Brain", (NbtElement)brain));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.setAbsorptionAmount(nbt.getFloat("AbsorptionAmount"));
        if (nbt.contains("Attributes", NbtElement.LIST_TYPE) && this.world != null && !this.world.isClient) {
            this.getAttributes().readNbt(nbt.getList("Attributes", NbtElement.COMPOUND_TYPE));
        }
        if (nbt.contains("ActiveEffects", NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList("ActiveEffects", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv.size(); ++i) {
                NbtCompound lv2 = lv.getCompound(i);
                StatusEffectInstance lv3 = StatusEffectInstance.fromNbt(lv2);
                if (lv3 == null) continue;
                this.activeStatusEffects.put(lv3.getEffectType(), lv3);
            }
        }
        if (nbt.contains("Health", NbtElement.NUMBER_TYPE)) {
            this.setHealth(nbt.getFloat("Health"));
        }
        this.hurtTime = nbt.getShort("HurtTime");
        this.deathTime = nbt.getShort("DeathTime");
        this.lastAttackedTime = nbt.getInt("HurtByTimestamp");
        if (nbt.contains("Team", NbtElement.STRING_TYPE)) {
            boolean bl;
            String string = nbt.getString("Team");
            Team lv4 = this.world.getScoreboard().getTeam(string);
            boolean bl2 = bl = lv4 != null && this.world.getScoreboard().addPlayerToTeam(this.getUuidAsString(), lv4);
            if (!bl) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", (Object)string);
            }
        }
        if (nbt.getBoolean("FallFlying")) {
            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
        }
        if (nbt.contains("SleepingX", NbtElement.NUMBER_TYPE) && nbt.contains("SleepingY", NbtElement.NUMBER_TYPE) && nbt.contains("SleepingZ", NbtElement.NUMBER_TYPE)) {
            BlockPos lv5 = new BlockPos(nbt.getInt("SleepingX"), nbt.getInt("SleepingY"), nbt.getInt("SleepingZ"));
            this.setSleepingPosition(lv5);
            this.dataTracker.set(POSE, EntityPose.SLEEPING);
            if (!this.firstUpdate) {
                this.setPositionInBed(lv5);
            }
        }
        if (nbt.contains("Brain", NbtElement.COMPOUND_TYPE)) {
            this.brain = this.deserializeBrain(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt.get("Brain")));
        }
    }

    protected void tickStatusEffects() {
        Iterator<StatusEffect> iterator = this.activeStatusEffects.keySet().iterator();
        try {
            while (iterator.hasNext()) {
                StatusEffect lv = iterator.next();
                StatusEffectInstance lv2 = this.activeStatusEffects.get(lv);
                if (!lv2.update(this, () -> this.onStatusEffectUpgraded(lv2, true, null))) {
                    if (this.world.isClient) continue;
                    iterator.remove();
                    this.onStatusEffectRemoved(lv2);
                    continue;
                }
                if (lv2.getDuration() % 600 != 0) continue;
                this.onStatusEffectUpgraded(lv2, false, null);
            }
        }
        catch (ConcurrentModificationException lv) {
            // empty catch block
        }
        if (this.effectsChanged) {
            if (!this.world.isClient) {
                this.updatePotionVisibility();
                this.updateGlowing();
            }
            this.effectsChanged = false;
        }
        int i = this.dataTracker.get(POTION_SWIRLS_COLOR);
        boolean bl = this.dataTracker.get(POTION_SWIRLS_AMBIENT);
        if (i > 0) {
            boolean bl2 = this.isInvisible() ? this.random.nextInt(15) == 0 : this.random.nextBoolean();
            if (bl) {
                bl2 &= this.random.nextInt(5) == 0;
            }
            if (bl2 && i > 0) {
                double d = (double)(i >> 16 & 0xFF) / 255.0;
                double e = (double)(i >> 8 & 0xFF) / 255.0;
                double f = (double)(i >> 0 & 0xFF) / 255.0;
                this.world.addParticle(bl ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), d, e, f);
            }
        }
    }

    protected void updatePotionVisibility() {
        if (this.activeStatusEffects.isEmpty()) {
            this.clearPotionSwirls();
            this.setInvisible(false);
        } else {
            Collection<StatusEffectInstance> collection = this.activeStatusEffects.values();
            this.dataTracker.set(POTION_SWIRLS_AMBIENT, LivingEntity.containsOnlyAmbientEffects(collection));
            this.dataTracker.set(POTION_SWIRLS_COLOR, PotionUtil.getColor(collection));
            this.setInvisible(this.hasStatusEffect(StatusEffects.INVISIBILITY));
        }
    }

    private void updateGlowing() {
        boolean bl = this.isGlowing();
        if (this.getFlag(Entity.GLOWING_FLAG_INDEX) != bl) {
            this.setFlag(Entity.GLOWING_FLAG_INDEX, bl);
        }
    }

    public double getAttackDistanceScalingFactor(@Nullable Entity entity) {
        double d = 1.0;
        if (this.isSneaky()) {
            d *= 0.8;
        }
        if (this.isInvisible()) {
            float f = this.getArmorVisibility();
            if (f < 0.1f) {
                f = 0.1f;
            }
            d *= 0.7 * (double)f;
        }
        if (entity != null) {
            ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
            EntityType<?> lv2 = entity.getType();
            if (lv2 == EntityType.SKELETON && lv.isOf(Items.SKELETON_SKULL) || lv2 == EntityType.ZOMBIE && lv.isOf(Items.ZOMBIE_HEAD) || lv2 == EntityType.PIGLIN && lv.isOf(Items.PIGLIN_HEAD) || lv2 == EntityType.PIGLIN_BRUTE && lv.isOf(Items.PIGLIN_HEAD) || lv2 == EntityType.CREEPER && lv.isOf(Items.CREEPER_HEAD)) {
                d *= 0.5;
            }
        }
        return d;
    }

    public boolean canTarget(LivingEntity target) {
        if (target instanceof PlayerEntity && this.world.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return target.canTakeDamage();
    }

    public boolean isTarget(LivingEntity entity, TargetPredicate predicate) {
        return predicate.test(this, entity);
    }

    public boolean canTakeDamage() {
        return !this.isInvulnerable() && this.isPartOfGame();
    }

    public boolean isPartOfGame() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean containsOnlyAmbientEffects(Collection<StatusEffectInstance> effects) {
        for (StatusEffectInstance lv : effects) {
            if (!lv.shouldShowParticles() || lv.isAmbient()) continue;
            return false;
        }
        return true;
    }

    protected void clearPotionSwirls() {
        this.dataTracker.set(POTION_SWIRLS_AMBIENT, false);
        this.dataTracker.set(POTION_SWIRLS_COLOR, 0);
    }

    public boolean clearStatusEffects() {
        if (this.world.isClient) {
            return false;
        }
        Iterator<StatusEffectInstance> iterator = this.activeStatusEffects.values().iterator();
        boolean bl = false;
        while (iterator.hasNext()) {
            this.onStatusEffectRemoved(iterator.next());
            iterator.remove();
            bl = true;
        }
        return bl;
    }

    public Collection<StatusEffectInstance> getStatusEffects() {
        return this.activeStatusEffects.values();
    }

    public Map<StatusEffect, StatusEffectInstance> getActiveStatusEffects() {
        return this.activeStatusEffects;
    }

    public boolean hasStatusEffect(StatusEffect effect) {
        return this.activeStatusEffects.containsKey(effect);
    }

    @Nullable
    public StatusEffectInstance getStatusEffect(StatusEffect effect) {
        return this.activeStatusEffects.get(effect);
    }

    public final boolean addStatusEffect(StatusEffectInstance effect) {
        return this.addStatusEffect(effect, null);
    }

    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return false;
        }
        StatusEffectInstance lv = this.activeStatusEffects.get(effect.getEffectType());
        if (lv == null) {
            this.activeStatusEffects.put(effect.getEffectType(), effect);
            this.onStatusEffectApplied(effect, source);
            return true;
        }
        if (lv.upgrade(effect)) {
            this.onStatusEffectUpgraded(lv, true, source);
            return true;
        }
        return false;
    }

    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        StatusEffect lv;
        return this.getGroup() != EntityGroup.UNDEAD || (lv = effect.getEffectType()) != StatusEffects.REGENERATION && lv != StatusEffects.POISON;
    }

    public void setStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return;
        }
        StatusEffectInstance lv = this.activeStatusEffects.put(effect.getEffectType(), effect);
        if (lv == null) {
            this.onStatusEffectApplied(effect, source);
        } else {
            this.onStatusEffectUpgraded(effect, true, source);
        }
    }

    public boolean isUndead() {
        return this.getGroup() == EntityGroup.UNDEAD;
    }

    @Nullable
    public StatusEffectInstance removeStatusEffectInternal(@Nullable StatusEffect type) {
        return this.activeStatusEffects.remove(type);
    }

    public boolean removeStatusEffect(StatusEffect type) {
        StatusEffectInstance lv = this.removeStatusEffectInternal(type);
        if (lv != null) {
            this.onStatusEffectRemoved(lv);
            return true;
        }
        return false;
    }

    protected void onStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source) {
        this.effectsChanged = true;
        if (!this.world.isClient) {
            effect.getEffectType().onApplied(this, this.getAttributes(), effect.getAmplifier());
        }
    }

    protected void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source) {
        this.effectsChanged = true;
        if (reapplyEffect && !this.world.isClient) {
            StatusEffect lv = effect.getEffectType();
            lv.onRemoved(this, this.getAttributes(), effect.getAmplifier());
            lv.onApplied(this, this.getAttributes(), effect.getAmplifier());
        }
    }

    protected void onStatusEffectRemoved(StatusEffectInstance effect) {
        this.effectsChanged = true;
        if (!this.world.isClient) {
            effect.getEffectType().onRemoved(this, this.getAttributes(), effect.getAmplifier());
        }
    }

    public void heal(float amount) {
        float g = this.getHealth();
        if (g > 0.0f) {
            this.setHealth(g + amount);
        }
    }

    public float getHealth() {
        return this.dataTracker.get(HEALTH).floatValue();
    }

    public void setHealth(float health) {
        this.dataTracker.set(HEALTH, Float.valueOf(MathHelper.clamp(health, 0.0f, this.getMaxHealth())));
    }

    public boolean isDead() {
        return this.getHealth() <= 0.0f;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean bl3;
        Entity lv3;
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.world.isClient) {
            return false;
        }
        if (this.isDead()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.IS_FIRE) && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (this.isSleeping() && !this.world.isClient) {
            this.wakeUp();
        }
        this.despawnCounter = 0;
        float g = amount;
        boolean bl = false;
        float h = 0.0f;
        if (amount > 0.0f && this.blockedByShield(source)) {
            Entity lv;
            this.damageShield(amount);
            h = amount;
            amount = 0.0f;
            if (!source.isIn(DamageTypeTags.IS_PROJECTILE) && (lv = source.getSource()) instanceof LivingEntity) {
                LivingEntity lv2 = (LivingEntity)lv;
                this.takeShieldHit(lv2);
            }
            bl = true;
        }
        if (source.isIn(DamageTypeTags.IS_FREEZING) && this.getType().isIn(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            amount *= 5.0f;
        }
        this.limbAnimator.setSpeed(1.5f);
        boolean bl2 = true;
        if ((float)this.timeUntilRegen > 10.0f && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (amount <= this.lastDamageTaken) {
                return false;
            }
            this.applyDamage(source, amount - this.lastDamageTaken);
            this.lastDamageTaken = amount;
            bl2 = false;
        } else {
            this.lastDamageTaken = amount;
            this.timeUntilRegen = 20;
            this.applyDamage(source, amount);
            this.hurtTime = this.maxHurtTime = 10;
        }
        if (source.isIn(DamageTypeTags.DAMAGES_HELMET) && !this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            this.damageHelmet(source, amount);
            amount *= 0.75f;
        }
        if ((lv3 = source.getAttacker()) != null) {
            WolfEntity lv6;
            if (lv3 instanceof LivingEntity) {
                LivingEntity lv4 = (LivingEntity)lv3;
                if (!source.isIn(DamageTypeTags.NO_ANGER)) {
                    this.setAttacker(lv4);
                }
            }
            if (lv3 instanceof PlayerEntity) {
                PlayerEntity lv5 = (PlayerEntity)lv3;
                this.playerHitTimer = 100;
                this.attackingPlayer = lv5;
            } else if (lv3 instanceof WolfEntity && (lv6 = (WolfEntity)lv3).isTamed()) {
                PlayerEntity lv7;
                this.playerHitTimer = 100;
                LivingEntity livingEntity = lv6.getOwner();
                this.attackingPlayer = livingEntity instanceof PlayerEntity ? (lv7 = (PlayerEntity)livingEntity) : null;
            }
        }
        if (bl2) {
            if (bl) {
                this.world.sendEntityStatus(this, EntityStatuses.BLOCK_WITH_SHIELD);
            } else {
                this.world.sendEntityDamage(this, source);
            }
            if (!(source.isIn(DamageTypeTags.NO_IMPACT) || bl && !(amount > 0.0f))) {
                this.scheduleVelocityUpdate();
            }
            if (lv3 != null && !source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                double d = lv3.getX() - this.getX();
                double e = lv3.getZ() - this.getZ();
                while (d * d + e * e < 1.0E-4) {
                    d = (Math.random() - Math.random()) * 0.01;
                    e = (Math.random() - Math.random()) * 0.01;
                }
                this.takeKnockback(0.4f, d, e);
                if (!bl) {
                    this.tiltScreen(d, e);
                }
            }
        }
        if (this.isDead()) {
            if (!this.tryUseTotem(source)) {
                SoundEvent lv8 = this.getDeathSound();
                if (bl2 && lv8 != null) {
                    this.playSound(lv8, this.getSoundVolume(), this.getSoundPitch());
                }
                this.onDeath(source);
            }
        } else if (bl2) {
            this.playHurtSound(source);
        }
        boolean bl4 = bl3 = !bl || amount > 0.0f;
        if (bl3) {
            this.lastDamageSource = source;
            this.lastDamageTime = this.world.getTime();
        }
        if (this instanceof ServerPlayerEntity) {
            Criteria.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity)this, source, g, amount, bl);
            if (h > 0.0f && h < 3.4028235E37f) {
                ((ServerPlayerEntity)this).increaseStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(h * 10.0f));
            }
        }
        if (lv3 instanceof ServerPlayerEntity) {
            Criteria.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity)lv3, this, source, g, amount, bl);
        }
        return bl3;
    }

    protected void takeShieldHit(LivingEntity attacker) {
        attacker.knockback(this);
    }

    protected void knockback(LivingEntity target) {
        target.takeKnockback(0.5, target.getX() - this.getX(), target.getZ() - this.getZ());
    }

    private boolean tryUseTotem(DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        ItemStack lv = null;
        for (Hand lv2 : Hand.values()) {
            ItemStack lv3 = this.getStackInHand(lv2);
            if (!lv3.isOf(Items.TOTEM_OF_UNDYING)) continue;
            lv = lv3.copy();
            lv3.decrement(1);
            break;
        }
        if (lv != null) {
            if (this instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv4 = (ServerPlayerEntity)this;
                lv4.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
                Criteria.USED_TOTEM.trigger(lv4, lv);
            }
            this.setHealth(1.0f);
            this.clearStatusEffects();
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
            this.world.sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);
        }
        return lv != null;
    }

    @Nullable
    public DamageSource getRecentDamageSource() {
        if (this.world.getTime() - this.lastDamageTime > 40L) {
            this.lastDamageSource = null;
        }
        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource source) {
        SoundEvent lv = this.getHurtSound(source);
        if (lv != null) {
            this.playSound(lv, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    public boolean blockedByShield(DamageSource source) {
        Vec3d lv3;
        PersistentProjectileEntity lv2;
        Entity lv = source.getSource();
        boolean bl = false;
        if (lv instanceof PersistentProjectileEntity && (lv2 = (PersistentProjectileEntity)lv).getPierceLevel() > 0) {
            bl = true;
        }
        if (!source.isIn(DamageTypeTags.BYPASSES_SHIELD) && this.isBlocking() && !bl && (lv3 = source.getPosition()) != null) {
            Vec3d lv4 = this.getRotationVec(1.0f);
            Vec3d lv5 = lv3.relativize(this.getPos()).normalize();
            lv5 = new Vec3d(lv5.x, 0.0, lv5.z);
            if (lv5.dotProduct(lv4) < 0.0) {
                return true;
            }
        }
        return false;
    }

    private void playEquipmentBreakEffects(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (!this.isSilent()) {
                this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_BREAK, this.getSoundCategory(), 0.8f, 0.8f + this.world.random.nextFloat() * 0.4f, false);
            }
            this.spawnItemParticles(stack, 5);
        }
    }

    public void onDeath(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity lv = damageSource.getAttacker();
        LivingEntity lv2 = this.getPrimeAdversary();
        if (this.scoreAmount >= 0 && lv2 != null) {
            lv2.updateKilledAdvancementCriterion(this, this.scoreAmount, damageSource);
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        if (!this.world.isClient && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getDamageTracker().getDeathMessage().getString());
        }
        this.dead = true;
        this.getDamageTracker().update();
        if (this.world instanceof ServerWorld) {
            if (lv == null || lv.onKilledOther((ServerWorld)this.world, this)) {
                this.emitGameEvent(GameEvent.ENTITY_DIE);
                this.drop(damageSource);
                this.onKilledBy(lv2);
            }
            this.world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
        }
        this.setPose(EntityPose.DYING);
    }

    protected void onKilledBy(@Nullable LivingEntity adversary) {
        if (this.world.isClient) {
            return;
        }
        boolean bl = false;
        if (adversary instanceof WitherEntity) {
            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                BlockPos lv = this.getBlockPos();
                BlockState lv2 = Blocks.WITHER_ROSE.getDefaultState();
                if (this.world.getBlockState(lv).isAir() && lv2.canPlaceAt(this.world, lv)) {
                    this.world.setBlockState(lv, lv2, Block.NOTIFY_ALL);
                    bl = true;
                }
            }
            if (!bl) {
                ItemEntity lv3 = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                this.world.spawnEntity(lv3);
            }
        }
    }

    protected void drop(DamageSource source) {
        boolean bl;
        Entity lv = source.getAttacker();
        int i = lv instanceof PlayerEntity ? EnchantmentHelper.getLooting((LivingEntity)lv) : 0;
        boolean bl2 = bl = this.playerHitTimer > 0;
        if (this.shouldDropLoot() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.dropLoot(source, bl);
            this.dropEquipment(source, i, bl);
        }
        this.dropInventory();
        this.dropXp();
    }

    protected void dropInventory() {
    }

    protected void dropXp() {
        if (this.world instanceof ServerWorld && !this.isExperienceDroppingDisabled() && (this.shouldAlwaysDropXp() || this.playerHitTimer > 0 && this.shouldDropXp() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))) {
            ExperienceOrbEntity.spawn((ServerWorld)this.world, this.getPos(), this.getXpToDrop());
        }
    }

    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
    }

    public Identifier getLootTable() {
        return this.getType().getLootTableId();
    }

    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        Identifier lv = this.getLootTable();
        LootTable lv2 = this.world.getServer().getLootManager().getTable(lv);
        LootContext.Builder lv3 = this.getLootContextBuilder(causedByPlayer, source);
        lv2.generateLoot(lv3.build(LootContextTypes.ENTITY), this::dropStack);
    }

    protected LootContext.Builder getLootContextBuilder(boolean causedByPlayer, DamageSource source) {
        LootContext.Builder lv = new LootContext.Builder((ServerWorld)this.world).random(this.random).parameter(LootContextParameters.THIS_ENTITY, this).parameter(LootContextParameters.ORIGIN, this.getPos()).parameter(LootContextParameters.DAMAGE_SOURCE, source).optionalParameter(LootContextParameters.KILLER_ENTITY, source.getAttacker()).optionalParameter(LootContextParameters.DIRECT_KILLER_ENTITY, source.getSource());
        if (causedByPlayer && this.attackingPlayer != null) {
            lv = lv.parameter(LootContextParameters.LAST_DAMAGE_PLAYER, this.attackingPlayer).luck(this.attackingPlayer.getLuck());
        }
        return lv;
    }

    public void takeKnockback(double strength, double x, double z) {
        if ((strength *= 1.0 - this.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        this.velocityDirty = true;
        Vec3d lv = this.getVelocity();
        Vec3d lv2 = new Vec3d(x, 0.0, z).normalize().multiply(strength);
        this.setVelocity(lv.x / 2.0 - lv2.x, this.onGround ? Math.min(0.4, lv.y / 2.0 + strength) : lv.y, lv.z / 2.0 - lv2.z);
    }

    public void tiltScreen(double deltaX, double deltaZ) {
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_DEATH;
    }

    private SoundEvent getFallSound(int distance) {
        return distance > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void disableExperienceDropping() {
        this.experienceDroppingDisabled = true;
    }

    public boolean isExperienceDroppingDisabled() {
        return this.experienceDroppingDisabled;
    }

    protected Vec3d getAttackPos() {
        Entity entity = this.getVehicle();
        if (entity instanceof AttackPosOffsettingMount) {
            AttackPosOffsettingMount lv = (AttackPosOffsettingMount)((Object)entity);
            return this.getPos().add(0.0, lv.getPassengerAttackYOffset(), 0.0);
        }
        return this.getPos();
    }

    public float getDamageTiltYaw() {
        return 0.0f;
    }

    public FallSounds getFallSounds() {
        return new FallSounds(SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundEvents.ENTITY_GENERIC_BIG_FALL);
    }

    protected SoundEvent getDrinkSound(ItemStack stack) {
        return stack.getDrinkSound();
    }

    public SoundEvent getEatSound(ItemStack stack) {
        return stack.getEatSound();
    }

    @Override
    public void setOnGround(boolean onGround) {
        super.setOnGround(onGround);
        if (onGround) {
            this.climbingPos = Optional.empty();
        }
    }

    public Optional<BlockPos> getClimbingPos() {
        return this.climbingPos;
    }

    public boolean isClimbing() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos lv = this.getBlockPos();
        BlockState lv2 = this.getBlockStateAtPos();
        if (lv2.isIn(BlockTags.CLIMBABLE)) {
            this.climbingPos = Optional.of(lv);
            return true;
        }
        if (lv2.getBlock() instanceof TrapdoorBlock && this.canEnterTrapdoor(lv, lv2)) {
            this.climbingPos = Optional.of(lv);
            return true;
        }
        return false;
    }

    private boolean canEnterTrapdoor(BlockPos pos, BlockState state) {
        BlockState lv;
        return state.get(TrapdoorBlock.OPEN) != false && (lv = this.world.getBlockState(pos.down())).isOf(Blocks.LADDER) && lv.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING);
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0f;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        boolean bl = super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
        int i = this.computeFallDamage(fallDistance, damageMultiplier);
        if (i > 0) {
            this.playSound(this.getFallSound(i), 1.0f, 1.0f);
            this.playBlockFallSound();
            this.damage(damageSource, i);
            return true;
        }
        return bl;
    }

    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        if (this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        StatusEffectInstance lv = this.getStatusEffect(StatusEffects.JUMP_BOOST);
        float h = lv == null ? 0.0f : (float)(lv.getAmplifier() + 1);
        return MathHelper.ceil((fallDistance - 3.0f - h) * damageMultiplier);
    }

    protected void playBlockFallSound() {
        int k;
        int j;
        if (this.isSilent()) {
            return;
        }
        int i = MathHelper.floor(this.getX());
        BlockState lv = this.world.getBlockState(new BlockPos(i, j = MathHelper.floor(this.getY() - (double)0.2f), k = MathHelper.floor(this.getZ())));
        if (!lv.isAir()) {
            BlockSoundGroup lv2 = lv.getSoundGroup();
            this.playSound(lv2.getFallSound(), lv2.getVolume() * 0.5f, lv2.getPitch() * 0.75f);
        }
    }

    @Override
    public void animateDamage(float yaw) {
        this.hurtTime = this.maxHurtTime = 10;
    }

    public int getArmor() {
        return MathHelper.floor(this.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
    }

    protected void damageArmor(DamageSource source, float amount) {
    }

    protected void damageHelmet(DamageSource source, float amount) {
    }

    protected void damageShield(float amount) {
    }

    protected float applyArmorToDamage(DamageSource source, float amount) {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            this.damageArmor(source, amount);
            amount = DamageUtil.getDamageLeft(amount, this.getArmor(), (float)this.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
        }
        return amount;
    }

    protected float modifyAppliedDamage(DamageSource source, float amount) {
        int i;
        int j;
        float g;
        float h;
        float k;
        if (source.isIn(DamageTypeTags.BYPASSES_EFFECTS)) {
            return amount;
        }
        if (this.hasStatusEffect(StatusEffects.RESISTANCE) && !source.isIn(DamageTypeTags.BYPASSES_RESISTANCE) && (k = (h = amount) - (amount = Math.max((g = amount * (float)(j = 25 - (i = (this.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5))) / 25.0f, 0.0f))) > 0.0f && k < 3.4028235E37f) {
            if (this instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)this).increaseStat(Stats.DAMAGE_RESISTED, Math.round(k * 10.0f));
            } else if (source.getAttacker() instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(k * 10.0f));
            }
        }
        if (amount <= 0.0f) {
            return 0.0f;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return amount;
        }
        i = EnchantmentHelper.getProtectionAmount(this.getArmorItems(), source);
        if (i > 0) {
            amount = DamageUtil.getInflictedDamage(amount, i);
        }
        return amount;
    }

    protected void applyDamage(DamageSource source, float amount) {
        Entity entity;
        if (this.isInvulnerableTo(source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float g = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (g - amount));
        float h = g - amount;
        if (h > 0.0f && h < 3.4028235E37f && (entity = source.getAttacker()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            lv.increaseStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(h * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        float i = this.getHealth();
        this.getDamageTracker().onDamage(source, i, amount);
        this.setHealth(i - amount);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - amount);
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public DamageTracker getDamageTracker() {
        return this.damageTracker;
    }

    @Nullable
    public LivingEntity getPrimeAdversary() {
        if (this.damageTracker.getBiggestAttacker() != null) {
            return this.damageTracker.getBiggestAttacker();
        }
        if (this.attackingPlayer != null) {
            return this.attackingPlayer;
        }
        if (this.attacker != null) {
            return this.attacker;
        }
        return null;
    }

    public final float getMaxHealth() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
    }

    public final int getStuckArrowCount() {
        return this.dataTracker.get(STUCK_ARROW_COUNT);
    }

    public final void setStuckArrowCount(int stuckArrowCount) {
        this.dataTracker.set(STUCK_ARROW_COUNT, stuckArrowCount);
    }

    public final int getStingerCount() {
        return this.dataTracker.get(STINGER_COUNT);
    }

    public final void setStingerCount(int stingerCount) {
        this.dataTracker.set(STINGER_COUNT, stingerCount);
    }

    private int getHandSwingDuration() {
        if (StatusEffectUtil.hasHaste(this)) {
            return 6 - (1 + StatusEffectUtil.getHasteAmplifier(this));
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            return 6 + (1 + this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2;
        }
        return 6;
    }

    public void swingHand(Hand hand) {
        this.swingHand(hand, false);
    }

    public void swingHand(Hand hand, boolean fromServerPlayer) {
        if (!this.handSwinging || this.handSwingTicks >= this.getHandSwingDuration() / 2 || this.handSwingTicks < 0) {
            this.handSwingTicks = -1;
            this.handSwinging = true;
            this.preferredHand = hand;
            if (this.world instanceof ServerWorld) {
                EntityAnimationS2CPacket lv = new EntityAnimationS2CPacket(this, hand == Hand.MAIN_HAND ? EntityAnimationS2CPacket.SWING_MAIN_HAND : EntityAnimationS2CPacket.SWING_OFF_HAND);
                ServerChunkManager lv2 = ((ServerWorld)this.world).getChunkManager();
                if (fromServerPlayer) {
                    lv2.sendToNearbyPlayers(this, lv);
                } else {
                    lv2.sendToOtherNearbyPlayers(this, lv);
                }
            }
        }
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        this.limbAnimator.setSpeed(1.5f);
        this.timeUntilRegen = 20;
        this.hurtTime = this.maxHurtTime = 10;
        SoundEvent lv = this.getHurtSound(damageSource);
        if (lv != null) {
            this.playSound(lv, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
        this.damage(this.getDamageSources().generic(), 0.0f);
        this.lastDamageSource = damageSource;
        this.lastDamageTime = this.world.getTime();
    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case 3: {
                SoundEvent lv = this.getDeathSound();
                if (lv != null) {
                    this.playSound(lv, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                }
                if (this instanceof PlayerEntity) break;
                this.setHealth(0.0f);
                this.onDeath(this.getDamageSources().generic());
                break;
            }
            case 30: {
                this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8f, 0.8f + this.world.random.nextFloat() * 0.4f);
                break;
            }
            case 29: {
                this.playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1.0f, 0.8f + this.world.random.nextFloat() * 0.4f);
                break;
            }
            case 46: {
                int i = 128;
                for (int j = 0; j < 128; ++j) {
                    double d = (double)j / 127.0;
                    float f = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float g = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float h = (this.random.nextFloat() - 0.5f) * 0.2f;
                    double e = MathHelper.lerp(d, this.prevX, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    double k = MathHelper.lerp(d, this.prevY, this.getY()) + this.random.nextDouble() * (double)this.getHeight();
                    double l = MathHelper.lerp(d, this.prevZ, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    this.world.addParticle(ParticleTypes.PORTAL, e, k, l, f, g, h);
                }
                break;
            }
            case 47: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.MAINHAND));
                break;
            }
            case 48: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.OFFHAND));
                break;
            }
            case 49: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.HEAD));
                break;
            }
            case 50: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.CHEST));
                break;
            }
            case 51: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.LEGS));
                break;
            }
            case 52: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.FEET));
                break;
            }
            case 54: {
                HoneyBlock.addRichParticles(this);
                break;
            }
            case 55: {
                this.swapHandStacks();
                break;
            }
            case 60: {
                this.addDeathParticles();
                break;
            }
            default: {
                super.handleStatus(status);
            }
        }
    }

    private void addDeathParticles() {
        for (int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.world.addParticle(ParticleTypes.POOF, this.getParticleX(1.0), this.getRandomBodyY(), this.getParticleZ(1.0), d, e, f);
        }
    }

    private void swapHandStacks() {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.OFFHAND);
        this.equipStack(EquipmentSlot.OFFHAND, this.getEquippedStack(EquipmentSlot.MAINHAND));
        this.equipStack(EquipmentSlot.MAINHAND, lv);
    }

    @Override
    protected void tickInVoid() {
        this.damage(this.getDamageSources().outOfWorld(), 4.0f);
    }

    protected void tickHandSwing() {
        int i = this.getHandSwingDuration();
        if (this.handSwinging) {
            ++this.handSwingTicks;
            if (this.handSwingTicks >= i) {
                this.handSwingTicks = 0;
                this.handSwinging = false;
            }
        } else {
            this.handSwingTicks = 0;
        }
        this.handSwingProgress = (float)this.handSwingTicks / (float)i;
    }

    @Nullable
    public EntityAttributeInstance getAttributeInstance(EntityAttribute attribute) {
        return this.getAttributes().getCustomInstance(attribute);
    }

    public double getAttributeValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributeValue(attribute.value());
    }

    public double getAttributeValue(EntityAttribute attribute) {
        return this.getAttributes().getValue(attribute);
    }

    public double getAttributeBaseValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributeBaseValue(attribute.value());
    }

    public double getAttributeBaseValue(EntityAttribute attribute) {
        return this.getAttributes().getBaseValue(attribute);
    }

    public AttributeContainer getAttributes() {
        return this.attributes;
    }

    public EntityGroup getGroup() {
        return EntityGroup.DEFAULT;
    }

    public ItemStack getMainHandStack() {
        return this.getEquippedStack(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffHandStack() {
        return this.getEquippedStack(EquipmentSlot.OFFHAND);
    }

    public boolean isHolding(Item item) {
        return this.isHolding((ItemStack stack) -> stack.isOf(item));
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandStack()) || predicate.test(this.getOffHandStack());
    }

    public ItemStack getStackInHand(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return this.getEquippedStack(EquipmentSlot.MAINHAND);
        }
        if (hand == Hand.OFF_HAND) {
            return this.getEquippedStack(EquipmentSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + hand);
    }

    public void setStackInHand(Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND) {
            this.equipStack(EquipmentSlot.MAINHAND, stack);
        } else if (hand == Hand.OFF_HAND) {
            this.equipStack(EquipmentSlot.OFFHAND, stack);
        } else {
            throw new IllegalArgumentException("Invalid hand " + hand);
        }
    }

    public boolean hasStackEquipped(EquipmentSlot slot) {
        return !this.getEquippedStack(slot).isEmpty();
    }

    @Override
    public abstract Iterable<ItemStack> getArmorItems();

    public abstract ItemStack getEquippedStack(EquipmentSlot var1);

    @Override
    public abstract void equipStack(EquipmentSlot var1, ItemStack var2);

    protected void processEquippedStack(ItemStack stack) {
        NbtCompound lv = stack.getNbt();
        if (lv != null) {
            stack.getItem().postProcessNbt(lv);
        }
    }

    public float getArmorVisibility() {
        Iterable<ItemStack> iterable = this.getArmorItems();
        int i = 0;
        int j = 0;
        for (ItemStack lv : iterable) {
            if (!lv.isEmpty()) {
                ++j;
            }
            ++i;
        }
        return i > 0 ? (float)j / (float)i : 0.0f;
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (lv.getModifier(SPRINTING_SPEED_BOOST_ID) != null) {
            lv.removeModifier(SPRINTING_SPEED_BOOST);
        }
        if (sprinting) {
            lv.addTemporaryModifier(SPRINTING_SPEED_BOOST);
        }
    }

    protected float getSoundVolume() {
        return 1.0f;
    }

    public float getSoundPitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isImmobile() {
        return this.isDead();
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (!this.isSleeping()) {
            super.pushAwayFrom(entity);
        }
    }

    private void onDismounted(Entity vehicle) {
        Vec3d lv;
        if (this.isRemoved()) {
            lv = this.getPos();
        } else if (vehicle.isRemoved() || this.world.getBlockState(vehicle.getBlockPos()).isIn(BlockTags.PORTALS)) {
            double d = Math.max(this.getY(), vehicle.getY());
            lv = new Vec3d(this.getX(), d, this.getZ());
        } else {
            lv = vehicle.updatePassengerForDismount(this);
        }
        this.requestTeleportAndDismount(lv.x, lv.y, lv.z);
    }

    @Override
    public boolean shouldRenderName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpVelocity() {
        return 0.42f * this.getJumpVelocityMultiplier();
    }

    public double getJumpBoostVelocityModifier() {
        return this.hasStatusEffect(StatusEffects.JUMP_BOOST) ? (double)(0.1f * (float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1)) : 0.0;
    }

    protected void jump() {
        double d = (double)this.getJumpVelocity() + this.getJumpBoostVelocityModifier();
        Vec3d lv = this.getVelocity();
        this.setVelocity(lv.x, d, lv.z);
        if (this.isSprinting()) {
            float f = this.getYaw() * ((float)Math.PI / 180);
            this.setVelocity(this.getVelocity().add(-MathHelper.sin(f) * 0.2f, 0.0, MathHelper.cos(f) * 0.2f));
        }
        this.velocityDirty = true;
    }

    protected void knockDownwards() {
        this.setVelocity(this.getVelocity().add(0.0, -0.04f, 0.0));
    }

    protected void swimUpward(TagKey<Fluid> fluid) {
        this.setVelocity(this.getVelocity().add(0.0, 0.04f, 0.0));
    }

    protected float getBaseMovementSpeedMultiplier() {
        return 0.8f;
    }

    public boolean canWalkOnFluid(FluidState state) {
        return false;
    }

    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement()) {
            boolean bl;
            double d = 0.08;
            boolean bl2 = bl = this.getVelocity().y <= 0.0;
            if (bl && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                d = 0.01;
                this.onLanding();
            }
            FluidState lv = this.world.getFluidState(this.getBlockPos());
            if (this.isTouchingWater() && this.shouldSwimInFluids() && !this.canWalkOnFluid(lv)) {
                double e = this.getY();
                float f = this.isSprinting() ? 0.9f : this.getBaseMovementSpeedMultiplier();
                float g = 0.02f;
                float h = EnchantmentHelper.getDepthStrider(this);
                if (h > 3.0f) {
                    h = 3.0f;
                }
                if (!this.onGround) {
                    h *= 0.5f;
                }
                if (h > 0.0f) {
                    f += (0.54600006f - f) * h / 3.0f;
                    g += (this.getMovementSpeed() - g) * h / 3.0f;
                }
                if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                    f = 0.96f;
                }
                this.updateVelocity(g, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                Vec3d lv2 = this.getVelocity();
                if (this.horizontalCollision && this.isClimbing()) {
                    lv2 = new Vec3d(lv2.x, 0.2, lv2.z);
                }
                this.setVelocity(lv2.multiply(f, 0.8f, f));
                Vec3d lv3 = this.applyFluidMovingSpeed(d, bl, this.getVelocity());
                this.setVelocity(lv3);
                if (this.horizontalCollision && this.doesNotCollide(lv3.x, lv3.y + (double)0.6f - this.getY() + e, lv3.z)) {
                    this.setVelocity(lv3.x, 0.3f, lv3.z);
                }
            } else if (this.isInLava() && this.shouldSwimInFluids() && !this.canWalkOnFluid(lv)) {
                Vec3d lv4;
                double e = this.getY();
                this.updateVelocity(0.02f, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
                    this.setVelocity(this.getVelocity().multiply(0.5, 0.8f, 0.5));
                    lv4 = this.applyFluidMovingSpeed(d, bl, this.getVelocity());
                    this.setVelocity(lv4);
                } else {
                    this.setVelocity(this.getVelocity().multiply(0.5));
                }
                if (!this.hasNoGravity()) {
                    this.setVelocity(this.getVelocity().add(0.0, -d / 4.0, 0.0));
                }
                lv4 = this.getVelocity();
                if (this.horizontalCollision && this.doesNotCollide(lv4.x, lv4.y + (double)0.6f - this.getY() + e, lv4.z)) {
                    this.setVelocity(lv4.x, 0.3f, lv4.z);
                }
            } else if (this.isFallFlying()) {
                double n;
                float o;
                double m;
                this.limitFallDistance();
                Vec3d lv5 = this.getVelocity();
                Vec3d lv6 = this.getRotationVector();
                float f = this.getPitch() * ((float)Math.PI / 180);
                double i = Math.sqrt(lv6.x * lv6.x + lv6.z * lv6.z);
                double j = lv5.horizontalLength();
                double k = lv6.length();
                double l = Math.cos(f);
                l = l * l * Math.min(1.0, k / 0.4);
                lv5 = this.getVelocity().add(0.0, d * (-1.0 + l * 0.75), 0.0);
                if (lv5.y < 0.0 && i > 0.0) {
                    m = lv5.y * -0.1 * l;
                    lv5 = lv5.add(lv6.x * m / i, m, lv6.z * m / i);
                }
                if (f < 0.0f && i > 0.0) {
                    m = j * (double)(-MathHelper.sin(f)) * 0.04;
                    lv5 = lv5.add(-lv6.x * m / i, m * 3.2, -lv6.z * m / i);
                }
                if (i > 0.0) {
                    lv5 = lv5.add((lv6.x / i * j - lv5.x) * 0.1, 0.0, (lv6.z / i * j - lv5.z) * 0.1);
                }
                this.setVelocity(lv5.multiply(0.99f, 0.98f, 0.99f));
                this.move(MovementType.SELF, this.getVelocity());
                if (this.horizontalCollision && !this.world.isClient && (o = (float)((n = j - (m = this.getVelocity().horizontalLength())) * 10.0 - 3.0)) > 0.0f) {
                    this.playSound(this.getFallSound((int)o), 1.0f, 1.0f);
                    this.damage(this.getDamageSources().flyIntoWall(), o);
                }
                if (this.onGround && !this.world.isClient) {
                    this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
                }
            } else {
                BlockPos lv7 = this.getVelocityAffectingPos();
                float p = this.world.getBlockState(lv7).getBlock().getSlipperiness();
                float f = this.onGround ? p * 0.91f : 0.91f;
                Vec3d lv8 = this.applyMovementInput(movementInput, p);
                double q = lv8.y;
                if (this.hasStatusEffect(StatusEffects.LEVITATION)) {
                    q += (0.05 * (double)(this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - lv8.y) * 0.2;
                    this.onLanding();
                } else if (!this.world.isClient || this.world.isChunkLoaded(lv7)) {
                    if (!this.hasNoGravity()) {
                        q -= d;
                    }
                } else {
                    q = this.getY() > (double)this.world.getBottomY() ? -0.1 : 0.0;
                }
                if (this.hasNoDrag()) {
                    this.setVelocity(lv8.x, q, lv8.z);
                } else {
                    this.setVelocity(lv8.x * (double)f, q * (double)0.98f, lv8.z * (double)f);
                }
            }
        }
        this.updateLimbs(this instanceof Flutterer);
    }

    private void travelControlled(LivingEntity controllingPassenger, Vec3d movementInput) {
        Vec3d lv = this.getControlledMovementInput(controllingPassenger, movementInput);
        this.tickControlled(controllingPassenger, lv);
        if (this.isLogicalSideForUpdatingMovement()) {
            this.setMovementSpeed(this.getSaddledSpeed(controllingPassenger));
            this.travel(lv);
        } else {
            this.updateLimbs(false);
            this.setVelocity(Vec3d.ZERO);
            this.tryCheckBlockCollision();
        }
    }

    protected void tickControlled(LivingEntity controllingPassenger, Vec3d movementInput) {
    }

    protected Vec3d getControlledMovementInput(LivingEntity controllingPassenger, Vec3d movementInput) {
        return movementInput;
    }

    protected float getSaddledSpeed(LivingEntity controllingPassenger) {
        return this.getMovementSpeed();
    }

    public void updateLimbs(boolean flutter) {
        float f = (float)MathHelper.magnitude(this.getX() - this.prevX, flutter ? this.getY() - this.prevY : 0.0, this.getZ() - this.prevZ);
        this.updateLimbs(f);
    }

    protected void updateLimbs(float posDelta) {
        float g = Math.min(posDelta * 4.0f, 1.0f);
        this.limbAnimator.updateLimbs(g, 0.4f);
    }

    public Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput);
        this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d lv = this.getVelocity();
        if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
            lv = new Vec3d(lv.x, 0.2, lv.z);
        }
        return lv;
    }

    public Vec3d applyFluidMovingSpeed(double gravity, boolean falling, Vec3d motion) {
        if (!this.hasNoGravity() && !this.isSprinting()) {
            double e = falling && Math.abs(motion.y - 0.005) >= 0.003 && Math.abs(motion.y - gravity / 16.0) < 0.003 ? -0.003 : motion.y - gravity / 16.0;
            return new Vec3d(motion.x, e, motion.z);
        }
        return motion;
    }

    private Vec3d applyClimbingSpeed(Vec3d motion) {
        if (this.isClimbing()) {
            this.onLanding();
            float f = 0.15f;
            double d = MathHelper.clamp(motion.x, (double)-0.15f, (double)0.15f);
            double e = MathHelper.clamp(motion.z, (double)-0.15f, (double)0.15f);
            double g = Math.max(motion.y, (double)-0.15f);
            if (g < 0.0 && !this.getBlockStateAtPos().isOf(Blocks.SCAFFOLDING) && this.isHoldingOntoLadder() && this instanceof PlayerEntity) {
                g = 0.0;
            }
            motion = new Vec3d(d, g, e);
        }
        return motion;
    }

    private float getMovementSpeed(float slipperiness) {
        if (this.onGround) {
            return this.getMovementSpeed() * (0.21600002f / (slipperiness * slipperiness * slipperiness));
        }
        return this.getOffGroundSpeed();
    }

    protected float getOffGroundSpeed() {
        return this.getControllingPassenger() instanceof PlayerEntity ? this.getMovementSpeed() * 0.1f : 0.02f;
    }

    public float getMovementSpeed() {
        return this.movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public boolean tryAttack(Entity target) {
        this.onAttacking(target);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickActiveItemStack();
        this.updateLeaningPitch();
        if (!this.world.isClient) {
            int j;
            int i = this.getStuckArrowCount();
            if (i > 0) {
                if (this.stuckArrowTimer <= 0) {
                    this.stuckArrowTimer = 20 * (30 - i);
                }
                --this.stuckArrowTimer;
                if (this.stuckArrowTimer <= 0) {
                    this.setStuckArrowCount(i - 1);
                }
            }
            if ((j = this.getStingerCount()) > 0) {
                if (this.stuckStingerTimer <= 0) {
                    this.stuckStingerTimer = 20 * (30 - j);
                }
                --this.stuckStingerTimer;
                if (this.stuckStingerTimer <= 0) {
                    this.setStingerCount(j - 1);
                }
            }
            this.sendEquipmentChanges();
            if (this.age % 20 == 0) {
                this.getDamageTracker().update();
            }
            if (this.isSleeping() && !this.isSleepingInBed()) {
                this.wakeUp();
            }
        }
        if (!this.isRemoved()) {
            this.tickMovement();
        }
        double d = this.getX() - this.prevX;
        double e = this.getZ() - this.prevZ;
        float f = (float)(d * d + e * e);
        float g = this.bodyYaw;
        float h = 0.0f;
        this.prevStepBobbingAmount = this.stepBobbingAmount;
        float k = 0.0f;
        if (f > 0.0025000002f) {
            k = 1.0f;
            h = (float)Math.sqrt(f) * 3.0f;
            float l = (float)MathHelper.atan2(e, d) * 57.295776f - 90.0f;
            float m = MathHelper.abs(MathHelper.wrapDegrees(this.getYaw()) - l);
            g = 95.0f < m && m < 265.0f ? l - 180.0f : l;
        }
        if (this.handSwingProgress > 0.0f) {
            g = this.getYaw();
        }
        if (!this.onGround) {
            k = 0.0f;
        }
        this.stepBobbingAmount += (k - this.stepBobbingAmount) * 0.3f;
        this.world.getProfiler().push("headTurn");
        h = this.turnHead(g, h);
        this.world.getProfiler().pop();
        this.world.getProfiler().push("rangeChecks");
        while (this.getYaw() - this.prevYaw < -180.0f) {
            this.prevYaw -= 360.0f;
        }
        while (this.getYaw() - this.prevYaw >= 180.0f) {
            this.prevYaw += 360.0f;
        }
        while (this.bodyYaw - this.prevBodyYaw < -180.0f) {
            this.prevBodyYaw -= 360.0f;
        }
        while (this.bodyYaw - this.prevBodyYaw >= 180.0f) {
            this.prevBodyYaw += 360.0f;
        }
        while (this.getPitch() - this.prevPitch < -180.0f) {
            this.prevPitch -= 360.0f;
        }
        while (this.getPitch() - this.prevPitch >= 180.0f) {
            this.prevPitch += 360.0f;
        }
        while (this.headYaw - this.prevHeadYaw < -180.0f) {
            this.prevHeadYaw -= 360.0f;
        }
        while (this.headYaw - this.prevHeadYaw >= 180.0f) {
            this.prevHeadYaw += 360.0f;
        }
        this.world.getProfiler().pop();
        this.lookDirection += h;
        this.roll = this.isFallFlying() ? ++this.roll : 0;
        if (this.isSleeping()) {
            this.setPitch(0.0f);
        }
    }

    private void sendEquipmentChanges() {
        Map<EquipmentSlot, ItemStack> map = this.getEquipmentChanges();
        if (map != null) {
            this.checkHandStackSwap(map);
            if (!map.isEmpty()) {
                this.sendEquipmentChanges(map);
            }
        }
    }

    @Nullable
    private Map<EquipmentSlot, ItemStack> getEquipmentChanges() {
        EnumMap<EquipmentSlot, ItemStack> map = null;
        block4: for (EquipmentSlot lv : EquipmentSlot.values()) {
            ItemStack lv2;
            switch (lv.getType()) {
                case HAND: {
                    lv2 = this.getSyncedHandStack(lv);
                    break;
                }
                case ARMOR: {
                    lv2 = this.getSyncedArmorStack(lv);
                    break;
                }
                default: {
                    continue block4;
                }
            }
            ItemStack lv3 = this.getEquippedStack(lv);
            if (!this.areItemsDifferent(lv2, lv3)) continue;
            if (map == null) {
                map = Maps.newEnumMap(EquipmentSlot.class);
            }
            map.put(lv, lv3);
            if (!lv2.isEmpty()) {
                this.getAttributes().removeModifiers(lv2.getAttributeModifiers(lv));
            }
            if (lv3.isEmpty()) continue;
            this.getAttributes().addTemporaryModifiers(lv3.getAttributeModifiers(lv));
        }
        return map;
    }

    public boolean areItemsDifferent(ItemStack stack, ItemStack stack2) {
        return !ItemStack.areEqual(stack2, stack);
    }

    private void checkHandStackSwap(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ItemStack lv = equipmentChanges.get((Object)EquipmentSlot.MAINHAND);
        ItemStack lv2 = equipmentChanges.get((Object)EquipmentSlot.OFFHAND);
        if (lv != null && lv2 != null && ItemStack.areEqual(lv, this.getSyncedHandStack(EquipmentSlot.OFFHAND)) && ItemStack.areEqual(lv2, this.getSyncedHandStack(EquipmentSlot.MAINHAND))) {
            ((ServerWorld)this.world).getChunkManager().sendToOtherNearbyPlayers(this, new EntityStatusS2CPacket(this, EntityStatuses.SWAP_HANDS));
            equipmentChanges.remove((Object)EquipmentSlot.MAINHAND);
            equipmentChanges.remove((Object)EquipmentSlot.OFFHAND);
            this.setSyncedHandStack(EquipmentSlot.MAINHAND, lv.copy());
            this.setSyncedHandStack(EquipmentSlot.OFFHAND, lv2.copy());
        }
    }

    private void sendEquipmentChanges(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ArrayList<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(equipmentChanges.size());
        equipmentChanges.forEach((slot, stack) -> {
            ItemStack lv = stack.copy();
            list.add(Pair.of(slot, lv));
            switch (slot.getType()) {
                case HAND: {
                    this.setSyncedHandStack((EquipmentSlot)((Object)slot), lv);
                    break;
                }
                case ARMOR: {
                    this.setSyncedArmorStack((EquipmentSlot)((Object)slot), lv);
                }
            }
        });
        ((ServerWorld)this.world).getChunkManager().sendToOtherNearbyPlayers(this, new EntityEquipmentUpdateS2CPacket(this.getId(), list));
    }

    private ItemStack getSyncedArmorStack(EquipmentSlot slot) {
        return this.syncedArmorStacks.get(slot.getEntitySlotId());
    }

    private void setSyncedArmorStack(EquipmentSlot slot, ItemStack armor) {
        this.syncedArmorStacks.set(slot.getEntitySlotId(), armor);
    }

    private ItemStack getSyncedHandStack(EquipmentSlot slot) {
        return this.syncedHandStacks.get(slot.getEntitySlotId());
    }

    private void setSyncedHandStack(EquipmentSlot slot, ItemStack stack) {
        this.syncedHandStacks.set(slot.getEntitySlotId(), stack);
    }

    protected float turnHead(float bodyRotation, float headRotation) {
        boolean bl;
        float h = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
        this.bodyYaw += h * 0.3f;
        float i = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
        if (Math.abs(i) > 50.0f) {
            this.bodyYaw += i - (float)(MathHelper.sign(i) * 50);
        }
        boolean bl2 = bl = i < -90.0f || i >= 90.0f;
        if (bl) {
            headRotation *= -1.0f;
        }
        return headRotation;
    }

    public void tickMovement() {
        if (this.jumpingCooldown > 0) {
            --this.jumpingCooldown;
        }
        if (this.isLogicalSideForUpdatingMovement()) {
            this.bodyTrackingIncrements = 0;
            this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
        }
        if (this.bodyTrackingIncrements > 0) {
            double d = this.getX() + (this.serverX - this.getX()) / (double)this.bodyTrackingIncrements;
            double e = this.getY() + (this.serverY - this.getY()) / (double)this.bodyTrackingIncrements;
            double f = this.getZ() + (this.serverZ - this.getZ()) / (double)this.bodyTrackingIncrements;
            double g = MathHelper.wrapDegrees(this.serverYaw - (double)this.getYaw());
            this.setYaw(this.getYaw() + (float)g / (float)this.bodyTrackingIncrements);
            this.setPitch(this.getPitch() + (float)(this.serverPitch - (double)this.getPitch()) / (float)this.bodyTrackingIncrements);
            --this.bodyTrackingIncrements;
            this.setPosition(d, e, f);
            this.setRotation(this.getYaw(), this.getPitch());
        } else if (!this.canMoveVoluntarily()) {
            this.setVelocity(this.getVelocity().multiply(0.98));
        }
        if (this.headTrackingIncrements > 0) {
            this.headYaw += (float)MathHelper.wrapDegrees(this.serverHeadYaw - (double)this.headYaw) / (float)this.headTrackingIncrements;
            --this.headTrackingIncrements;
        }
        Vec3d lv = this.getVelocity();
        double h = lv.x;
        double i = lv.y;
        double j = lv.z;
        if (Math.abs(lv.x) < 0.003) {
            h = 0.0;
        }
        if (Math.abs(lv.y) < 0.003) {
            i = 0.0;
        }
        if (Math.abs(lv.z) < 0.003) {
            j = 0.0;
        }
        this.setVelocity(h, i, j);
        this.world.getProfiler().push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.sidewaysSpeed = 0.0f;
            this.forwardSpeed = 0.0f;
        } else if (this.canMoveVoluntarily()) {
            this.world.getProfiler().push("newAi");
            this.tickNewAi();
            this.world.getProfiler().pop();
        }
        this.world.getProfiler().pop();
        this.world.getProfiler().push("jump");
        if (this.jumping && this.shouldSwimInFluids()) {
            double k = this.isInLava() ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER);
            boolean bl = this.isTouchingWater() && k > 0.0;
            double l = this.getSwimHeight();
            if (bl && (!this.onGround || k > l)) {
                this.swimUpward(FluidTags.WATER);
            } else if (this.isInLava() && (!this.onGround || k > l)) {
                this.swimUpward(FluidTags.LAVA);
            } else if ((this.onGround || bl && k <= l) && this.jumpingCooldown == 0) {
                this.jump();
                this.jumpingCooldown = 10;
            }
        } else {
            this.jumpingCooldown = 0;
        }
        this.world.getProfiler().pop();
        this.world.getProfiler().push("travel");
        this.sidewaysSpeed *= 0.98f;
        this.forwardSpeed *= 0.98f;
        this.tickFallFlying();
        Box lv2 = this.getBoundingBox();
        LivingEntity lv3 = this.getControllingPassenger();
        Vec3d lv4 = new Vec3d(this.sidewaysSpeed, this.upwardSpeed, this.forwardSpeed);
        if (lv3 != null && this.isAlive()) {
            this.travelControlled(lv3, lv4);
        } else {
            this.travel(lv4);
        }
        this.world.getProfiler().pop();
        this.world.getProfiler().push("freezing");
        if (!this.world.isClient && !this.isDead()) {
            int m = this.getFrozenTicks();
            if (this.inPowderSnow && this.canFreeze()) {
                this.setFrozenTicks(Math.min(this.getMinFreezeDamageTicks(), m + 1));
            } else {
                this.setFrozenTicks(Math.max(0, m - 2));
            }
        }
        this.removePowderSnowSlow();
        this.addPowderSnowSlowIfNeeded();
        if (!this.world.isClient && this.age % 40 == 0 && this.isFrozen() && this.canFreeze()) {
            this.damage(this.getDamageSources().freeze(), 1.0f);
        }
        this.world.getProfiler().pop();
        this.world.getProfiler().push("push");
        if (this.riptideTicks > 0) {
            --this.riptideTicks;
            this.tickRiptide(lv2, this.getBoundingBox());
        }
        this.tickCramming();
        this.world.getProfiler().pop();
        if (!this.world.isClient && this.hurtByWater() && this.isWet()) {
            this.damage(this.getDamageSources().drown(), 1.0f);
        }
    }

    public boolean hurtByWater() {
        return false;
    }

    private void tickFallFlying() {
        boolean bl = this.getFlag(Entity.FALL_FLYING_FLAG_INDEX);
        if (bl && !this.onGround && !this.hasVehicle() && !this.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack lv = this.getEquippedStack(EquipmentSlot.CHEST);
            if (lv.isOf(Items.ELYTRA) && ElytraItem.isUsable(lv)) {
                bl = true;
                int i = this.roll + 1;
                if (!this.world.isClient && i % 10 == 0) {
                    int j = i / 10;
                    if (j % 2 == 0) {
                        lv.damage(1, this, player -> player.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
                    }
                    this.emitGameEvent(GameEvent.ELYTRA_GLIDE);
                }
            } else {
                bl = false;
            }
        } else {
            bl = false;
        }
        if (!this.world.isClient) {
            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, bl);
        }
    }

    protected void tickNewAi() {
    }

    protected void tickCramming() {
        if (this.world.isClient()) {
            this.world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), this.getBoundingBox(), EntityPredicates.canBePushedBy(this)).forEach(this::pushAway);
            return;
        }
        List<Entity> list = this.world.getOtherEntities(this, this.getBoundingBox(), EntityPredicates.canBePushedBy(this));
        if (!list.isEmpty()) {
            int j;
            int i = this.world.getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                j = 0;
                for (int k = 0; k < list.size(); ++k) {
                    if (list.get(k).hasVehicle()) continue;
                    ++j;
                }
                if (j > i - 1) {
                    this.damage(this.getDamageSources().cramming(), 6.0f);
                }
            }
            for (j = 0; j < list.size(); ++j) {
                Entity lv = list.get(j);
                this.pushAway(lv);
            }
        }
    }

    protected void tickRiptide(Box a, Box b) {
        Box lv = a.union(b);
        List<Entity> list = this.world.getOtherEntities(this, lv);
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); ++i) {
                Entity lv2 = list.get(i);
                if (!(lv2 instanceof LivingEntity)) continue;
                this.attackLivingEntity((LivingEntity)lv2);
                this.riptideTicks = 0;
                this.setVelocity(this.getVelocity().multiply(-0.2));
                break;
            }
        } else if (this.horizontalCollision) {
            this.riptideTicks = 0;
        }
        if (!this.world.isClient && this.riptideTicks <= 0) {
            this.setLivingFlag(USING_RIPTIDE_FLAG, false);
        }
    }

    protected void pushAway(Entity entity) {
        entity.pushAwayFrom(this);
    }

    protected void attackLivingEntity(LivingEntity target) {
    }

    public boolean isUsingRiptide() {
        return (this.dataTracker.get(LIVING_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity lv = this.getVehicle();
        super.stopRiding();
        if (lv != null && lv != this.getVehicle() && !this.world.isClient) {
            this.onDismounted(lv);
        }
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.prevStepBobbingAmount = this.stepBobbingAmount;
        this.stepBobbingAmount = 0.0f;
        this.onLanding();
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        this.serverX = x;
        this.serverY = y;
        this.serverZ = z;
        this.serverYaw = yaw;
        this.serverPitch = pitch;
        this.bodyTrackingIncrements = interpolationSteps;
    }

    @Override
    public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
        this.serverHeadYaw = yaw;
        this.headTrackingIncrements = interpolationSteps;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void triggerItemPickedUpByEntityCriteria(ItemEntity item) {
        Entity lv = item.getOwner();
        if (lv instanceof ServerPlayerEntity) {
            Criteria.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayerEntity)lv, item.getStack(), this);
        }
    }

    public void sendPickup(Entity item, int count) {
        if (!item.isRemoved() && !this.world.isClient && (item instanceof ItemEntity || item instanceof PersistentProjectileEntity || item instanceof ExperienceOrbEntity)) {
            ((ServerWorld)this.world).getChunkManager().sendToOtherNearbyPlayers(item, new ItemPickupAnimationS2CPacket(item.getId(), this.getId(), count));
        }
    }

    public boolean canSee(Entity entity) {
        if (entity.world != this.world) {
            return false;
        }
        Vec3d lv = new Vec3d(this.getX(), this.getEyeY(), this.getZ());
        Vec3d lv2 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        if (lv2.distanceTo(lv) > 128.0) {
            return false;
        }
        return this.world.raycast(new RaycastContext(lv, lv2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getYaw(float tickDelta) {
        if (tickDelta == 1.0f) {
            return this.headYaw;
        }
        return MathHelper.lerp(tickDelta, this.prevHeadYaw, this.headYaw);
    }

    public float getHandSwingProgress(float tickDelta) {
        float g = this.handSwingProgress - this.lastHandSwingProgress;
        if (g < 0.0f) {
            g += 1.0f;
        }
        return this.lastHandSwingProgress + g * tickDelta;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.isClimbing();
    }

    @Override
    public float getHeadYaw() {
        return this.headYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.bodyYaw = bodyYaw;
    }

    @Override
    protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    public static Vec3d positionInPortal(Vec3d pos) {
        return new Vec3d(pos.x, pos.y, 0.0);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0f) {
            amount = 0.0f;
        }
        this.absorptionAmount = amount;
    }

    public void enterCombat() {
    }

    public void endCombat() {
    }

    protected void markEffectsDirty() {
        this.effectsChanged = true;
    }

    public abstract Arm getMainArm();

    public boolean isUsingItem() {
        return (this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
    }

    public Hand getActiveHand() {
        return (this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    private void tickActiveItemStack() {
        if (this.isUsingItem()) {
            if (ItemStack.areItemsEqual(this.getStackInHand(this.getActiveHand()), this.activeItemStack)) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                this.tickItemStackUsage(this.activeItemStack);
            } else {
                this.clearActiveItem();
            }
        }
    }

    protected void tickItemStackUsage(ItemStack stack) {
        stack.usageTick(this.world, this, this.getItemUseTimeLeft());
        if (this.shouldSpawnConsumptionEffects()) {
            this.spawnConsumptionEffects(stack, 5);
        }
        if (--this.itemUseTimeLeft == 0 && !this.world.isClient && !stack.isUsedOnRelease()) {
            this.consumeItem();
        }
    }

    private boolean shouldSpawnConsumptionEffects() {
        int i = this.getItemUseTimeLeft();
        FoodComponent lv = this.activeItemStack.getItem().getFoodComponent();
        boolean bl = lv != null && lv.isSnack();
        return (bl |= i <= this.activeItemStack.getMaxUseTime() - 7) && i % 4 == 0;
    }

    private void updateLeaningPitch() {
        this.lastLeaningPitch = this.leaningPitch;
        this.leaningPitch = this.isInSwimmingPose() ? Math.min(1.0f, this.leaningPitch + 0.09f) : Math.max(0.0f, this.leaningPitch - 0.09f);
    }

    protected void setLivingFlag(int mask, boolean value) {
        int j = this.dataTracker.get(LIVING_FLAGS).byteValue();
        j = value ? (j |= mask) : (j &= ~mask);
        this.dataTracker.set(LIVING_FLAGS, (byte)j);
    }

    public void setCurrentHand(Hand hand) {
        ItemStack lv = this.getStackInHand(hand);
        if (lv.isEmpty() || this.isUsingItem()) {
            return;
        }
        this.activeItemStack = lv;
        this.itemUseTimeLeft = lv.getMaxUseTime();
        if (!this.world.isClient) {
            this.setLivingFlag(USING_ITEM_FLAG, true);
            this.setLivingFlag(OFF_HAND_ACTIVE_FLAG, hand == Hand.OFF_HAND);
            this.emitGameEvent(GameEvent.ITEM_INTERACT_START);
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (SLEEPING_POSITION.equals(data)) {
            if (this.world.isClient) {
                this.getSleepingPosition().ifPresent(this::setPositionInBed);
            }
        } else if (LIVING_FLAGS.equals(data) && this.world.isClient) {
            if (this.isUsingItem() && this.activeItemStack.isEmpty()) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                if (!this.activeItemStack.isEmpty()) {
                    this.itemUseTimeLeft = this.activeItemStack.getMaxUseTime();
                }
            } else if (!this.isUsingItem() && !this.activeItemStack.isEmpty()) {
                this.activeItemStack = ItemStack.EMPTY;
                this.itemUseTimeLeft = 0;
            }
        }
    }

    @Override
    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        super.lookAt(anchorPoint, target);
        this.prevHeadYaw = this.headYaw;
        this.prevBodyYaw = this.bodyYaw = this.headYaw;
    }

    protected void spawnConsumptionEffects(ItemStack stack, int particleCount) {
        if (stack.isEmpty() || !this.isUsingItem()) {
            return;
        }
        if (stack.getUseAction() == UseAction.DRINK) {
            this.playSound(this.getDrinkSound(stack), 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
        }
        if (stack.getUseAction() == UseAction.EAT) {
            this.spawnItemParticles(stack, particleCount);
            this.playSound(this.getEatSound(stack), 0.5f + 0.5f * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    private void spawnItemParticles(ItemStack stack, int count) {
        for (int j = 0; j < count; ++j) {
            Vec3d lv = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            lv = lv.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            lv = lv.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3d lv2 = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
            lv2 = lv2.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            lv2 = lv2.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            lv2 = lv2.add(this.getX(), this.getEyeY(), this.getZ());
            this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), lv2.x, lv2.y, lv2.z, lv.x, lv.y + 0.05, lv.z);
        }
    }

    protected void consumeItem() {
        if (this.world.isClient && !this.isUsingItem()) {
            return;
        }
        Hand lv = this.getActiveHand();
        if (!this.activeItemStack.equals(this.getStackInHand(lv))) {
            this.stopUsingItem();
            return;
        }
        if (!this.activeItemStack.isEmpty() && this.isUsingItem()) {
            this.spawnConsumptionEffects(this.activeItemStack, 16);
            ItemStack lv2 = this.activeItemStack.finishUsing(this.world, this);
            if (lv2 != this.activeItemStack) {
                this.setStackInHand(lv, lv2);
            }
            this.clearActiveItem();
        }
    }

    public ItemStack getActiveItem() {
        return this.activeItemStack;
    }

    public int getItemUseTimeLeft() {
        return this.itemUseTimeLeft;
    }

    public int getItemUseTime() {
        if (this.isUsingItem()) {
            return this.activeItemStack.getMaxUseTime() - this.getItemUseTimeLeft();
        }
        return 0;
    }

    public void stopUsingItem() {
        if (!this.activeItemStack.isEmpty()) {
            this.activeItemStack.onStoppedUsing(this.world, this, this.getItemUseTimeLeft());
            if (this.activeItemStack.isUsedOnRelease()) {
                this.tickActiveItemStack();
            }
        }
        this.clearActiveItem();
    }

    public void clearActiveItem() {
        if (!this.world.isClient) {
            boolean bl = this.isUsingItem();
            this.setLivingFlag(USING_ITEM_FLAG, false);
            if (bl) {
                this.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        this.activeItemStack = ItemStack.EMPTY;
        this.itemUseTimeLeft = 0;
    }

    public boolean isBlocking() {
        if (!this.isUsingItem() || this.activeItemStack.isEmpty()) {
            return false;
        }
        Item lv = this.activeItemStack.getItem();
        if (lv.getUseAction(this.activeItemStack) != UseAction.BLOCK) {
            return false;
        }
        return lv.getMaxUseTime(this.activeItemStack) - this.itemUseTimeLeft >= 5;
    }

    public boolean isHoldingOntoLadder() {
        return this.isSneaking();
    }

    public boolean isFallFlying() {
        return this.getFlag(Entity.FALL_FLYING_FLAG_INDEX);
    }

    @Override
    public boolean isInSwimmingPose() {
        return super.isInSwimmingPose() || !this.isFallFlying() && this.isInPose(EntityPose.FALL_FLYING);
    }

    public int getRoll() {
        return this.roll;
    }

    public boolean teleport(double x, double y, double z, boolean particleEffects) {
        double g = this.getX();
        double h = this.getY();
        double i = this.getZ();
        double j = y;
        boolean bl2 = false;
        World lv2 = this.world;
        BlockPos lv = BlockPos.ofFloored(x, j, z);
        if (lv2.isChunkLoaded(lv)) {
            boolean bl3 = false;
            while (!bl3 && lv.getY() > lv2.getBottomY()) {
                BlockPos lv3 = lv.down();
                BlockState lv4 = lv2.getBlockState(lv3);
                if (lv4.getMaterial().blocksMovement()) {
                    bl3 = true;
                    continue;
                }
                j -= 1.0;
                lv = lv3;
            }
            if (bl3) {
                this.requestTeleport(x, j, z);
                if (lv2.isSpaceEmpty(this) && !lv2.containsFluid(this.getBoundingBox())) {
                    bl2 = true;
                }
            }
        }
        if (!bl2) {
            this.requestTeleport(g, h, i);
            return false;
        }
        if (particleEffects) {
            lv2.sendEntityStatus(this, EntityStatuses.ADD_PORTAL_PARTICLES);
        }
        if (this instanceof PathAwareEntity) {
            ((PathAwareEntity)this).getNavigation().stop();
        }
        return true;
    }

    public boolean isAffectedBySplashPotions() {
        return true;
    }

    public boolean isMobOrPlayer() {
        return true;
    }

    public void setNearbySongPlaying(BlockPos songPosition, boolean playing) {
    }

    public boolean canEquip(ItemStack stack) {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return pose == EntityPose.SLEEPING ? SLEEPING_DIMENSIONS : super.getDimensions(pose).scaled(this.getScaleFactor());
    }

    public ImmutableList<EntityPose> getPoses() {
        return ImmutableList.of(EntityPose.STANDING);
    }

    public Box getBoundingBox(EntityPose pose) {
        EntityDimensions lv = this.getDimensions(pose);
        return new Box(-lv.width / 2.0f, 0.0, -lv.width / 2.0f, lv.width / 2.0f, lv.height, lv.width / 2.0f);
    }

    @Override
    public boolean canUsePortals() {
        return super.canUsePortals() && !this.isSleeping();
    }

    public Optional<BlockPos> getSleepingPosition() {
        return this.dataTracker.get(SLEEPING_POSITION);
    }

    public void setSleepingPosition(BlockPos pos) {
        this.dataTracker.set(SLEEPING_POSITION, Optional.of(pos));
    }

    public void clearSleepingPosition() {
        this.dataTracker.set(SLEEPING_POSITION, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPosition().isPresent();
    }

    public void sleep(BlockPos pos) {
        BlockState lv;
        if (this.hasVehicle()) {
            this.stopRiding();
        }
        if ((lv = this.world.getBlockState(pos)).getBlock() instanceof BedBlock) {
            this.world.setBlockState(pos, (BlockState)lv.with(BedBlock.OCCUPIED, true), Block.NOTIFY_ALL);
        }
        this.setPose(EntityPose.SLEEPING);
        this.setPositionInBed(pos);
        this.setSleepingPosition(pos);
        this.setVelocity(Vec3d.ZERO);
        this.velocityDirty = true;
    }

    private void setPositionInBed(BlockPos pos) {
        this.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 0.6875, (double)pos.getZ() + 0.5);
    }

    private boolean isSleepingInBed() {
        return this.getSleepingPosition().map(pos -> this.world.getBlockState((BlockPos)pos).getBlock() instanceof BedBlock).orElse(false);
    }

    public void wakeUp() {
        this.getSleepingPosition().filter(this.world::isChunkLoaded).ifPresent(pos -> {
            BlockState lv = this.world.getBlockState((BlockPos)pos);
            if (lv.getBlock() instanceof BedBlock) {
                Direction lv2 = lv.get(BedBlock.FACING);
                this.world.setBlockState((BlockPos)pos, (BlockState)lv.with(BedBlock.OCCUPIED, false), Block.NOTIFY_ALL);
                Vec3d lv3 = BedBlock.findWakeUpPosition(this.getType(), (CollisionView)this.world, pos, lv2, this.getYaw()).orElseGet(() -> {
                    BlockPos lv = pos.up();
                    return new Vec3d((double)lv.getX() + 0.5, (double)lv.getY() + 0.1, (double)lv.getZ() + 0.5);
                });
                Vec3d lv4 = Vec3d.ofBottomCenter(pos).subtract(lv3).normalize();
                float f = (float)MathHelper.wrapDegrees(MathHelper.atan2(lv4.z, lv4.x) * 57.2957763671875 - 90.0);
                this.setPosition(lv3.x, lv3.y, lv3.z);
                this.setYaw(f);
                this.setPitch(0.0f);
            }
        });
        Vec3d lv = this.getPos();
        this.setPose(EntityPose.STANDING);
        this.setPosition(lv.x, lv.y, lv.z);
        this.clearSleepingPosition();
    }

    @Nullable
    public Direction getSleepingDirection() {
        BlockPos lv = this.getSleepingPosition().orElse(null);
        return lv != null ? BedBlock.getDirection(this.world, lv) : null;
    }

    @Override
    public boolean isInsideWall() {
        return !this.isSleeping() && super.isInsideWall();
    }

    @Override
    protected final float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return pose == EntityPose.SLEEPING ? 0.2f : this.getActiveEyeHeight(pose, dimensions);
    }

    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return super.getEyeHeight(pose, dimensions);
    }

    public ItemStack getProjectileType(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    public ItemStack eatFood(World world, ItemStack stack) {
        if (stack.isFood()) {
            world.playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatSound(stack), SoundCategory.NEUTRAL, 1.0f, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f);
            this.applyFoodEffects(stack, world, this);
            if (!(this instanceof PlayerEntity) || !((PlayerEntity)this).getAbilities().creativeMode) {
                stack.decrement(1);
            }
            this.emitGameEvent(GameEvent.EAT);
        }
        return stack;
    }

    private void applyFoodEffects(ItemStack stack, World world, LivingEntity targetEntity) {
        Item lv = stack.getItem();
        if (lv.isFood()) {
            List<Pair<StatusEffectInstance, Float>> list = lv.getFoodComponent().getStatusEffects();
            for (Pair<StatusEffectInstance, Float> pair : list) {
                if (world.isClient || pair.getFirst() == null || !(world.random.nextFloat() < pair.getSecond().floatValue())) continue;
                targetEntity.addStatusEffect(new StatusEffectInstance(pair.getFirst()));
            }
        }
    }

    private static byte getEquipmentBreakStatus(EquipmentSlot slot) {
        switch (slot) {
            case MAINHAND: {
                return 47;
            }
            case OFFHAND: {
                return 48;
            }
            case HEAD: {
                return 49;
            }
            case CHEST: {
                return 50;
            }
            case FEET: {
                return 52;
            }
            case LEGS: {
                return 51;
            }
        }
        return 47;
    }

    public void sendEquipmentBreakStatus(EquipmentSlot slot) {
        this.world.sendEntityStatus(this, LivingEntity.getEquipmentBreakStatus(slot));
    }

    public void sendToolBreakStatus(Hand hand) {
        this.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    @Override
    public Box getVisibilityBoundingBox() {
        if (this.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.DRAGON_HEAD)) {
            float f = 0.5f;
            return this.getBoundingBox().expand(0.5, 0.5, 0.5);
        }
        return super.getVisibilityBoundingBox();
    }

    public static EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        Equipment lv = Equipment.fromStack(stack);
        if (lv != null) {
            return lv.getSlotType();
        }
        return EquipmentSlot.MAINHAND;
    }

    private static StackReference getStackReference(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            return StackReference.of(entity, slot);
        }
        return StackReference.of(entity, slot, stack -> stack.isEmpty() || MobEntity.getPreferredEquipmentSlot(stack) == slot);
    }

    @Nullable
    private static EquipmentSlot getEquipmentSlot(int slotId) {
        if (slotId == 100 + EquipmentSlot.HEAD.getEntitySlotId()) {
            return EquipmentSlot.HEAD;
        }
        if (slotId == 100 + EquipmentSlot.CHEST.getEntitySlotId()) {
            return EquipmentSlot.CHEST;
        }
        if (slotId == 100 + EquipmentSlot.LEGS.getEntitySlotId()) {
            return EquipmentSlot.LEGS;
        }
        if (slotId == 100 + EquipmentSlot.FEET.getEntitySlotId()) {
            return EquipmentSlot.FEET;
        }
        if (slotId == 98) {
            return EquipmentSlot.MAINHAND;
        }
        if (slotId == 99) {
            return EquipmentSlot.OFFHAND;
        }
        return null;
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        EquipmentSlot lv = LivingEntity.getEquipmentSlot(mappedIndex);
        if (lv != null) {
            return LivingEntity.getStackReference(this, lv);
        }
        return super.getStackReference(mappedIndex);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        }
        boolean bl = !this.getEquippedStack(EquipmentSlot.HEAD).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getEquippedStack(EquipmentSlot.CHEST).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getEquippedStack(EquipmentSlot.LEGS).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getEquippedStack(EquipmentSlot.FEET).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES);
        return bl && super.canFreeze();
    }

    @Override
    public boolean isGlowing() {
        return !this.world.isClient() && this.hasStatusEffect(StatusEffects.GLOWING) || super.isGlowing();
    }

    @Override
    public float getBodyYaw() {
        return this.bodyYaw;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        float g = packet.getYaw();
        float h = packet.getPitch();
        this.updateTrackedPosition(d, e, f);
        this.bodyYaw = packet.getHeadYaw();
        this.headYaw = packet.getHeadYaw();
        this.prevBodyYaw = this.bodyYaw;
        this.prevHeadYaw = this.headYaw;
        this.setId(packet.getId());
        this.setUuid(packet.getUuid());
        this.updatePositionAndAngles(d, e, f, g, h);
        this.setVelocity(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
    }

    public boolean disablesShield() {
        return this.getMainHandStack().getItem() instanceof AxeItem;
    }

    @Override
    public float getStepHeight() {
        float f = super.getStepHeight();
        return this.getControllingPassenger() instanceof PlayerEntity ? Math.max(f, 1.0f) : f;
    }

    public record FallSounds(SoundEvent small, SoundEvent big) {
    }
}

