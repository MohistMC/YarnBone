/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.CollisionView;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class PlayerEntity
extends LivingEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int field_30643 = 16;
    public static final int field_30644 = 20;
    public static final int field_30645 = 100;
    public static final int field_30646 = 10;
    public static final int field_30647 = 200;
    public static final float field_30648 = 1.5f;
    public static final float field_30649 = 0.6f;
    public static final float field_30650 = 0.6f;
    public static final float DEFAULT_EYE_HEIGHT = 1.62f;
    public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.changing(0.6f, 1.8f);
    private static final Map<EntityPose, EntityDimensions> POSE_DIMENSIONS = ImmutableMap.builder().put(EntityPose.STANDING, STANDING_DIMENSIONS).put(EntityPose.SLEEPING, SLEEPING_DIMENSIONS).put(EntityPose.FALL_FLYING, EntityDimensions.changing(0.6f, 0.6f)).put(EntityPose.SWIMMING, EntityDimensions.changing(0.6f, 0.6f)).put(EntityPose.SPIN_ATTACK, EntityDimensions.changing(0.6f, 0.6f)).put(EntityPose.CROUCHING, EntityDimensions.changing(0.6f, 1.5f)).put(EntityPose.DYING, EntityDimensions.fixed(0.2f, 0.2f)).build();
    private static final int field_30652 = 25;
    private static final TrackedData<Float> ABSORPTION_AMOUNT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> SCORE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Byte> PLAYER_MODEL_PARTS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Byte> MAIN_ARM = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<NbtCompound> LEFT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    protected static final TrackedData<NbtCompound> RIGHT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    private long shoulderEntityAddedTime;
    private final PlayerInventory inventory = new PlayerInventory(this);
    protected EnderChestInventory enderChestInventory = new EnderChestInventory();
    public final PlayerScreenHandler playerScreenHandler;
    public ScreenHandler currentScreenHandler;
    protected HungerManager hungerManager = new HungerManager();
    protected int abilityResyncCountdown;
    public float prevStrideDistance;
    public float strideDistance;
    public int experiencePickUpDelay;
    public double prevCapeX;
    public double prevCapeY;
    public double prevCapeZ;
    public double capeX;
    public double capeY;
    public double capeZ;
    private int sleepTimer;
    protected boolean isSubmergedInWater;
    private final PlayerAbilities abilities = new PlayerAbilities();
    public int experienceLevel;
    public int totalExperience;
    public float experienceProgress;
    protected int enchantmentTableSeed;
    protected final float field_7509 = 0.02f;
    private int lastPlayedLevelUpSoundTime;
    private final GameProfile gameProfile;
    private boolean reducedDebugInfo;
    private ItemStack selectedItem = ItemStack.EMPTY;
    private final ItemCooldownManager itemCooldownManager = this.createCooldownManager();
    private Optional<GlobalPos> lastDeathPos = Optional.empty();
    @Nullable
    public FishingBobberEntity fishHook;
    protected float damageTiltYaw;

    public PlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super((EntityType<? extends LivingEntity>)EntityType.PLAYER, world);
        this.setUuid(Uuids.getUuidFromProfile(gameProfile));
        this.gameProfile = gameProfile;
        this.playerScreenHandler = new PlayerScreenHandler(this.inventory, !world.isClient, this);
        this.currentScreenHandler = this.playerScreenHandler;
        this.refreshPositionAndAngles((double)pos.getX() + 0.5, pos.getY() + 1, (double)pos.getZ() + 0.5, yaw, 0.0f);
        this.field_6215 = 180.0f;
    }

    public boolean isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode) {
        if (!gameMode.isBlockBreakingRestricted()) {
            return false;
        }
        if (gameMode == GameMode.SPECTATOR) {
            return true;
        }
        if (this.canModifyBlocks()) {
            return false;
        }
        ItemStack lv = this.getMainHandStack();
        return lv.isEmpty() || !lv.canDestroy(world.getRegistryManager().get(RegistryKeys.BLOCK), new CachedBlockPosition(world, pos, false));
    }

    public static DefaultAttributeContainer.Builder createPlayerAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f).add(EntityAttributes.GENERIC_ATTACK_SPEED).add(EntityAttributes.GENERIC_LUCK);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ABSORPTION_AMOUNT, Float.valueOf(0.0f));
        this.dataTracker.startTracking(SCORE, 0);
        this.dataTracker.startTracking(PLAYER_MODEL_PARTS, (byte)0);
        this.dataTracker.startTracking(MAIN_ARM, (byte)1);
        this.dataTracker.startTracking(LEFT_SHOULDER_ENTITY, new NbtCompound());
        this.dataTracker.startTracking(RIGHT_SHOULDER_ENTITY, new NbtCompound());
    }

    @Override
    public void tick() {
        this.noClip = this.isSpectator();
        if (this.isSpectator()) {
            this.onGround = false;
        }
        if (this.experiencePickUpDelay > 0) {
            --this.experiencePickUpDelay;
        }
        if (this.isSleeping()) {
            ++this.sleepTimer;
            if (this.sleepTimer > 100) {
                this.sleepTimer = 100;
            }
            if (!this.world.isClient && this.world.isDay()) {
                this.wakeUp(false, true);
            }
        } else if (this.sleepTimer > 0) {
            ++this.sleepTimer;
            if (this.sleepTimer >= 110) {
                this.sleepTimer = 0;
            }
        }
        this.updateWaterSubmersionState();
        super.tick();
        if (!this.world.isClient && this.currentScreenHandler != null && !this.currentScreenHandler.canUse(this)) {
            this.closeHandledScreen();
            this.currentScreenHandler = this.playerScreenHandler;
        }
        this.updateCapeAngles();
        if (!this.world.isClient) {
            this.hungerManager.update(this);
            this.incrementStat(Stats.PLAY_TIME);
            this.incrementStat(Stats.TOTAL_WORLD_TIME);
            if (this.isAlive()) {
                this.incrementStat(Stats.TIME_SINCE_DEATH);
            }
            if (this.isSneaky()) {
                this.incrementStat(Stats.SNEAK_TIME);
            }
            if (!this.isSleeping()) {
                this.incrementStat(Stats.TIME_SINCE_REST);
            }
        }
        int i = 29999999;
        double d = MathHelper.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
        double e = MathHelper.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
        if (d != this.getX() || e != this.getZ()) {
            this.setPosition(d, this.getY(), e);
        }
        ++this.lastAttackedTicks;
        ItemStack lv = this.getMainHandStack();
        if (!ItemStack.areEqual(this.selectedItem, lv)) {
            if (!ItemStack.areItemsEqual(this.selectedItem, lv)) {
                this.resetLastAttackedTicks();
            }
            this.selectedItem = lv.copy();
        }
        this.updateTurtleHelmet();
        this.itemCooldownManager.update();
        this.updatePose();
    }

    public boolean shouldCancelInteraction() {
        return this.isSneaking();
    }

    protected boolean shouldDismount() {
        return this.isSneaking();
    }

    protected boolean clipAtLedge() {
        return this.isSneaking();
    }

    protected boolean updateWaterSubmersionState() {
        this.isSubmergedInWater = this.isSubmergedIn(FluidTags.WATER);
        return this.isSubmergedInWater;
    }

    private void updateTurtleHelmet() {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
        if (lv.isOf(Items.TURTLE_HELMET) && !this.isSubmergedIn(FluidTags.WATER)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 200, 0, false, false, true));
        }
    }

    protected ItemCooldownManager createCooldownManager() {
        return new ItemCooldownManager();
    }

    private void updateCapeAngles() {
        this.prevCapeX = this.capeX;
        this.prevCapeY = this.capeY;
        this.prevCapeZ = this.capeZ;
        double d = this.getX() - this.capeX;
        double e = this.getY() - this.capeY;
        double f = this.getZ() - this.capeZ;
        double g = 10.0;
        if (d > 10.0) {
            this.prevCapeX = this.capeX = this.getX();
        }
        if (f > 10.0) {
            this.prevCapeZ = this.capeZ = this.getZ();
        }
        if (e > 10.0) {
            this.prevCapeY = this.capeY = this.getY();
        }
        if (d < -10.0) {
            this.prevCapeX = this.capeX = this.getX();
        }
        if (f < -10.0) {
            this.prevCapeZ = this.capeZ = this.getZ();
        }
        if (e < -10.0) {
            this.prevCapeY = this.capeY = this.getY();
        }
        this.capeX += d * 0.25;
        this.capeZ += f * 0.25;
        this.capeY += e * 0.25;
    }

    protected void updatePose() {
        if (!this.wouldPoseNotCollide(EntityPose.SWIMMING)) {
            return;
        }
        EntityPose lv = this.isFallFlying() ? EntityPose.FALL_FLYING : (this.isSleeping() ? EntityPose.SLEEPING : (this.isSwimming() ? EntityPose.SWIMMING : (this.isUsingRiptide() ? EntityPose.SPIN_ATTACK : (this.isSneaking() && !this.abilities.flying ? EntityPose.CROUCHING : EntityPose.STANDING))));
        EntityPose lv2 = this.isSpectator() || this.hasVehicle() || this.wouldPoseNotCollide(lv) ? lv : (this.wouldPoseNotCollide(EntityPose.CROUCHING) ? EntityPose.CROUCHING : EntityPose.SWIMMING);
        this.setPose(lv2);
    }

    @Override
    public int getMaxNetherPortalTime() {
        return this.abilities.invulnerable ? 1 : 80;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_PLAYER_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_PLAYER_SPLASH;
    }

    @Override
    protected SoundEvent getHighSpeedSplashSound() {
        return SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
    }

    @Override
    public int getDefaultPortalCooldown() {
        return 10;
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        this.world.playSound(this, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
    }

    public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }

    @Override
    protected int getBurningDuration() {
        return 20;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.CONSUME_ITEM) {
            this.consumeItem();
        } else if (status == EntityStatuses.USE_FULL_DEBUG_INFO) {
            this.reducedDebugInfo = false;
        } else if (status == EntityStatuses.USE_REDUCED_DEBUG_INFO) {
            this.reducedDebugInfo = true;
        } else if (status == EntityStatuses.ADD_CLOUD_PARTICLES) {
            this.spawnParticles(ParticleTypes.CLOUD);
        } else {
            super.handleStatus(status);
        }
    }

    private void spawnParticles(ParticleEffect parameters) {
        for (int i = 0; i < 5; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.world.addParticle(parameters, this.getParticleX(1.0), this.getRandomBodyY() + 1.0, this.getParticleZ(1.0), d, e, f);
        }
    }

    protected void closeHandledScreen() {
        this.currentScreenHandler = this.playerScreenHandler;
    }

    protected void onHandledScreenClosed() {
    }

    @Override
    public void tickRiding() {
        if (!this.world.isClient && this.shouldDismount() && this.hasVehicle()) {
            this.stopRiding();
            this.setSneaking(false);
            return;
        }
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.tickRiding();
        this.prevStrideDistance = this.strideDistance;
        this.strideDistance = 0.0f;
        this.increaseRidingMotionStats(this.getX() - d, this.getY() - e, this.getZ() - f);
    }

    @Override
    protected void tickNewAi() {
        super.tickNewAi();
        this.tickHandSwing();
        this.headYaw = this.getYaw();
    }

    @Override
    public void tickMovement() {
        if (this.abilityResyncCountdown > 0) {
            --this.abilityResyncCountdown;
        }
        if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
            if (this.getHealth() < this.getMaxHealth() && this.age % 20 == 0) {
                this.heal(1.0f);
            }
            if (this.hungerManager.isNotFull() && this.age % 10 == 0) {
                this.hungerManager.setFoodLevel(this.hungerManager.getFoodLevel() + 1);
            }
        }
        this.inventory.updateItems();
        this.prevStrideDistance = this.strideDistance;
        super.tickMovement();
        this.setMovementSpeed((float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        float f = !this.onGround || this.isDead() || this.isSwimming() ? 0.0f : Math.min(0.1f, (float)this.getVelocity().horizontalLength());
        this.strideDistance += (f - this.strideDistance) * 0.4f;
        if (this.getHealth() > 0.0f && !this.isSpectator()) {
            Box lv = this.hasVehicle() && !this.getVehicle().isRemoved() ? this.getBoundingBox().union(this.getVehicle().getBoundingBox()).expand(1.0, 0.0, 1.0) : this.getBoundingBox().expand(1.0, 0.5, 1.0);
            List<Entity> list = this.world.getOtherEntities(this, lv);
            ArrayList<Entity> list2 = Lists.newArrayList();
            for (int i = 0; i < list.size(); ++i) {
                Entity lv2 = list.get(i);
                if (lv2.getType() == EntityType.EXPERIENCE_ORB) {
                    list2.add(lv2);
                    continue;
                }
                if (lv2.isRemoved()) continue;
                this.collideWithEntity(lv2);
            }
            if (!list2.isEmpty()) {
                this.collideWithEntity((Entity)Util.getRandom(list2, this.random));
            }
        }
        this.updateShoulderEntity(this.getShoulderEntityLeft());
        this.updateShoulderEntity(this.getShoulderEntityRight());
        if (!this.world.isClient && (this.fallDistance > 0.5f || this.isTouchingWater()) || this.abilities.flying || this.isSleeping() || this.inPowderSnow) {
            this.dropShoulderEntities();
        }
    }

    private void updateShoulderEntity(@Nullable NbtCompound entityNbt) {
        if (!(entityNbt == null || entityNbt.contains("Silent") && entityNbt.getBoolean("Silent") || this.world.random.nextInt(200) != 0)) {
            String string = entityNbt.getString("id");
            EntityType.get(string).filter(entityType -> entityType == EntityType.PARROT).ifPresent(arg -> {
                if (!ParrotEntity.imitateNearbyMob(this.world, this)) {
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), ParrotEntity.getRandomSound(this.world, this.world.random), this.getSoundCategory(), 1.0f, ParrotEntity.getSoundPitch(this.world.random));
                }
            });
        }
    }

    private void collideWithEntity(Entity entity) {
        entity.onPlayerCollision(this);
    }

    public int getScore() {
        return this.dataTracker.get(SCORE);
    }

    public void setScore(int score) {
        this.dataTracker.set(SCORE, score);
    }

    public void addScore(int score) {
        int j = this.getScore();
        this.dataTracker.set(SCORE, j + score);
    }

    public void useRiptide(int riptideTicks) {
        this.riptideTicks = riptideTicks;
        if (!this.world.isClient) {
            this.dropShoulderEntities();
            this.setLivingFlag(LivingEntity.USING_RIPTIDE_FLAG, true);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        this.refreshPosition();
        if (!this.isSpectator()) {
            this.drop(damageSource);
        }
        if (damageSource != null) {
            this.setVelocity(-MathHelper.cos((this.getDamageTiltYaw() + this.getYaw()) * ((float)Math.PI / 180)) * 0.1f, 0.1f, -MathHelper.sin((this.getDamageTiltYaw() + this.getYaw()) * ((float)Math.PI / 180)) * 0.1f);
        } else {
            this.setVelocity(0.0, 0.1, 0.0);
        }
        this.incrementStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        this.extinguish();
        this.setOnFire(false);
        this.setLastDeathPos(Optional.of(GlobalPos.create(this.world.getRegistryKey(), this.getBlockPos())));
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            this.vanishCursedItems();
            this.inventory.dropAll();
        }
    }

    protected void vanishCursedItems() {
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack lv = this.inventory.getStack(i);
            if (lv.isEmpty() || !EnchantmentHelper.hasVanishingCurse(lv)) continue;
            this.inventory.removeStack(i);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return source.getType().effects().getSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack, boolean retainOwnership) {
        return this.dropItem(stack, false, retainOwnership);
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.world.isClient) {
            this.swingHand(Hand.MAIN_HAND);
        }
        double d = this.getEyeY() - (double)0.3f;
        ItemEntity lv = new ItemEntity(this.world, this.getX(), d, this.getZ(), stack);
        lv.setPickupDelay(40);
        if (retainOwnership) {
            lv.setThrower(this.getUuid());
        }
        if (throwRandomly) {
            float f = this.random.nextFloat() * 0.5f;
            float g = this.random.nextFloat() * ((float)Math.PI * 2);
            lv.setVelocity(-MathHelper.sin(g) * f, 0.2f, MathHelper.cos(g) * f);
        } else {
            float f = 0.3f;
            float g = MathHelper.sin(this.getPitch() * ((float)Math.PI / 180));
            float h = MathHelper.cos(this.getPitch() * ((float)Math.PI / 180));
            float i = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float j = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            float k = this.random.nextFloat() * ((float)Math.PI * 2);
            float l = 0.02f * this.random.nextFloat();
            lv.setVelocity((double)(-i * h * 0.3f) + Math.cos(k) * (double)l, -g * 0.3f + 0.1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, (double)(j * h * 0.3f) + Math.sin(k) * (double)l);
        }
        return lv;
    }

    public float getBlockBreakingSpeed(BlockState block) {
        float f = this.inventory.getBlockBreakingSpeed(block);
        if (f > 1.0f) {
            int i = EnchantmentHelper.getEfficiency(this);
            ItemStack lv = this.getMainHandStack();
            if (i > 0 && !lv.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(this)) {
            f *= 1.0f + (float)(StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2f;
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            f *= (switch (this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
            f /= 5.0f;
        }
        if (!this.onGround) {
            f /= 5.0f;
        }
        return f;
    }

    public boolean canHarvest(BlockState state) {
        return !state.isToolRequired() || this.inventory.getMainHandStack().isSuitableFor(state);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setUuid(Uuids.getUuidFromProfile(this.gameProfile));
        NbtList lv = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
        this.inventory.readNbt(lv);
        this.inventory.selectedSlot = nbt.getInt("SelectedItemSlot");
        this.sleepTimer = nbt.getShort("SleepTimer");
        this.experienceProgress = nbt.getFloat("XpP");
        this.experienceLevel = nbt.getInt("XpLevel");
        this.totalExperience = nbt.getInt("XpTotal");
        this.enchantmentTableSeed = nbt.getInt("XpSeed");
        if (this.enchantmentTableSeed == 0) {
            this.enchantmentTableSeed = this.random.nextInt();
        }
        this.setScore(nbt.getInt("Score"));
        this.hungerManager.readNbt(nbt);
        this.abilities.readNbt(nbt);
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(this.abilities.getWalkSpeed());
        if (nbt.contains("EnderItems", NbtElement.LIST_TYPE)) {
            this.enderChestInventory.readNbtList(nbt.getList("EnderItems", NbtElement.COMPOUND_TYPE));
        }
        if (nbt.contains("ShoulderEntityLeft", NbtElement.COMPOUND_TYPE)) {
            this.setShoulderEntityLeft(nbt.getCompound("ShoulderEntityLeft"));
        }
        if (nbt.contains("ShoulderEntityRight", NbtElement.COMPOUND_TYPE)) {
            this.setShoulderEntityRight(nbt.getCompound("ShoulderEntityRight"));
        }
        if (nbt.contains("LastDeathLocation", NbtElement.COMPOUND_TYPE)) {
            this.setLastDeathPos(GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("LastDeathLocation")).resultOrPartial(LOGGER::error));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtHelper.putDataVersion(nbt);
        nbt.put("Inventory", this.inventory.writeNbt(new NbtList()));
        nbt.putInt("SelectedItemSlot", this.inventory.selectedSlot);
        nbt.putShort("SleepTimer", (short)this.sleepTimer);
        nbt.putFloat("XpP", this.experienceProgress);
        nbt.putInt("XpLevel", this.experienceLevel);
        nbt.putInt("XpTotal", this.totalExperience);
        nbt.putInt("XpSeed", this.enchantmentTableSeed);
        nbt.putInt("Score", this.getScore());
        this.hungerManager.writeNbt(nbt);
        this.abilities.writeNbt(nbt);
        nbt.put("EnderItems", this.enderChestInventory.toNbtList());
        if (!this.getShoulderEntityLeft().isEmpty()) {
            nbt.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
        }
        if (!this.getShoulderEntityRight().isEmpty()) {
            nbt.put("ShoulderEntityRight", this.getShoulderEntityRight());
        }
        this.getLastDeathPos().flatMap(arg -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, (GlobalPos)arg).resultOrPartial(LOGGER::error)).ifPresent(arg2 -> nbt.put("LastDeathLocation", (NbtElement)arg2));
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (super.isInvulnerableTo(damageSource)) {
            return true;
        }
        if (damageSource.isIn(DamageTypeTags.IS_DROWNING)) {
            return !this.world.getGameRules().getBoolean(GameRules.DROWNING_DAMAGE);
        }
        if (damageSource.isIn(DamageTypeTags.IS_FALL)) {
            return !this.world.getGameRules().getBoolean(GameRules.FALL_DAMAGE);
        }
        if (damageSource.isIn(DamageTypeTags.IS_FIRE)) {
            return !this.world.getGameRules().getBoolean(GameRules.FIRE_DAMAGE);
        }
        if (damageSource.isIn(DamageTypeTags.IS_FREEZING)) {
            return !this.world.getGameRules().getBoolean(GameRules.FREEZE_DAMAGE);
        }
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.abilities.invulnerable && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        this.despawnCounter = 0;
        if (this.isDead()) {
            return false;
        }
        if (!this.world.isClient) {
            this.dropShoulderEntities();
        }
        if (source.isScaledWithDifficulty()) {
            if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                amount = 0.0f;
            }
            if (this.world.getDifficulty() == Difficulty.EASY) {
                amount = Math.min(amount / 2.0f + 1.0f, amount);
            }
            if (this.world.getDifficulty() == Difficulty.HARD) {
                amount = amount * 3.0f / 2.0f;
            }
        }
        if (amount == 0.0f) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    protected void takeShieldHit(LivingEntity attacker) {
        super.takeShieldHit(attacker);
        if (attacker.disablesShield()) {
            this.disableShield(true);
        }
    }

    @Override
    public boolean canTakeDamage() {
        return !this.getAbilities().invulnerable && super.canTakeDamage();
    }

    public boolean shouldDamagePlayer(PlayerEntity player) {
        AbstractTeam lv = this.getScoreboardTeam();
        AbstractTeam lv2 = player.getScoreboardTeam();
        if (lv == null) {
            return true;
        }
        if (!lv.isEqual(lv2)) {
            return true;
        }
        return lv.isFriendlyFireAllowed();
    }

    @Override
    protected void damageArmor(DamageSource source, float amount) {
        this.inventory.damageArmor(source, amount, PlayerInventory.ARMOR_SLOTS);
    }

    @Override
    protected void damageHelmet(DamageSource source, float amount) {
        this.inventory.damageArmor(source, amount, PlayerInventory.HELMET_SLOTS);
    }

    @Override
    protected void damageShield(float amount) {
        if (!this.activeItemStack.isOf(Items.SHIELD)) {
            return;
        }
        if (!this.world.isClient) {
            this.incrementStat(Stats.USED.getOrCreateStat(this.activeItemStack.getItem()));
        }
        if (amount >= 3.0f) {
            int i = 1 + MathHelper.floor(amount);
            Hand lv = this.getActiveHand();
            this.activeItemStack.damage(i, this, player -> player.sendToolBreakStatus(lv));
            if (this.activeItemStack.isEmpty()) {
                if (lv == Hand.MAIN_HAND) {
                    this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    this.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                this.activeItemStack = ItemStack.EMPTY;
                this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8f, 0.8f + this.world.random.nextFloat() * 0.4f);
            }
        }
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float g = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (g - amount));
        float h = g - amount;
        if (h > 0.0f && h < 3.4028235E37f) {
            this.increaseStat(Stats.DAMAGE_ABSORBED, Math.round(h * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        this.addExhaustion(source.getExhaustion());
        float i = this.getHealth();
        this.getDamageTracker().onDamage(source, i, amount);
        this.setHealth(this.getHealth() - amount);
        if (amount < 3.4028235E37f) {
            this.increaseStat(Stats.DAMAGE_TAKEN, Math.round(amount * 10.0f));
        }
    }

    @Override
    protected boolean isOnSoulSpeedBlock() {
        return !this.abilities.flying && super.isOnSoulSpeedBlock();
    }

    public boolean shouldFilterText() {
        return false;
    }

    public void openEditSignScreen(SignBlockEntity sign) {
    }

    public void openCommandBlockMinecartScreen(CommandBlockExecutor commandBlockExecutor) {
    }

    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
    }

    public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
    }

    public void openJigsawScreen(JigsawBlockEntity jigsaw) {
    }

    public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) {
    }

    public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
        return OptionalInt.empty();
    }

    public void sendTradeOffers(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable) {
    }

    public void useBook(ItemStack book, Hand hand) {
    }

    public ActionResult interact(Entity entity, Hand hand) {
        if (this.isSpectator()) {
            if (entity instanceof NamedScreenHandlerFactory) {
                this.openHandledScreen((NamedScreenHandlerFactory)((Object)entity));
            }
            return ActionResult.PASS;
        }
        ItemStack lv = this.getStackInHand(hand);
        ItemStack lv2 = lv.copy();
        ActionResult lv3 = entity.interact(this, hand);
        if (lv3.isAccepted()) {
            if (this.abilities.creativeMode && lv == this.getStackInHand(hand) && lv.getCount() < lv2.getCount()) {
                lv.setCount(lv2.getCount());
            }
            return lv3;
        }
        if (!lv.isEmpty() && entity instanceof LivingEntity) {
            ActionResult lv4;
            if (this.abilities.creativeMode) {
                lv = lv2;
            }
            if ((lv4 = lv.useOnEntity(this, (LivingEntity)entity, hand)).isAccepted()) {
                this.world.emitGameEvent(GameEvent.ENTITY_INTERACT, entity.getPos(), GameEvent.Emitter.of(this));
                if (lv.isEmpty() && !this.abilities.creativeMode) {
                    this.setStackInHand(hand, ItemStack.EMPTY);
                }
                return lv4;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public double getHeightOffset() {
        return -0.35;
    }

    @Override
    public void dismountVehicle() {
        super.dismountVehicle();
        this.ridingCooldown = 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isSleeping();
    }

    @Override
    public boolean shouldSwimInFluids() {
        return !this.abilities.flying;
    }

    @Override
    protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
        if (!this.abilities.flying && movement.y <= 0.0 && (type == MovementType.SELF || type == MovementType.PLAYER) && this.clipAtLedge() && this.method_30263()) {
            double d = movement.x;
            double e = movement.z;
            double f = 0.05;
            while (d != 0.0 && this.world.isSpaceEmpty(this, this.getBoundingBox().offset(d, -this.getStepHeight(), 0.0))) {
                if (d < 0.05 && d >= -0.05) {
                    d = 0.0;
                    continue;
                }
                if (d > 0.0) {
                    d -= 0.05;
                    continue;
                }
                d += 0.05;
            }
            while (e != 0.0 && this.world.isSpaceEmpty(this, this.getBoundingBox().offset(0.0, -this.getStepHeight(), e))) {
                if (e < 0.05 && e >= -0.05) {
                    e = 0.0;
                    continue;
                }
                if (e > 0.0) {
                    e -= 0.05;
                    continue;
                }
                e += 0.05;
            }
            while (d != 0.0 && e != 0.0 && this.world.isSpaceEmpty(this, this.getBoundingBox().offset(d, -this.getStepHeight(), e))) {
                d = d < 0.05 && d >= -0.05 ? 0.0 : (d > 0.0 ? (d -= 0.05) : (d += 0.05));
                if (e < 0.05 && e >= -0.05) {
                    e = 0.0;
                    continue;
                }
                if (e > 0.0) {
                    e -= 0.05;
                    continue;
                }
                e += 0.05;
            }
            movement = new Vec3d(d, movement.y, e);
        }
        return movement;
    }

    private boolean method_30263() {
        return this.onGround || this.fallDistance < this.getStepHeight() && !this.world.isSpaceEmpty(this, this.getBoundingBox().offset(0.0, this.fallDistance - this.getStepHeight(), 0.0));
    }

    public void attack(Entity target) {
        if (!target.isAttackable()) {
            return;
        }
        if (target.handleAttack(this)) {
            return;
        }
        float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float g = target instanceof LivingEntity ? EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup()) : EnchantmentHelper.getAttackDamage(this.getMainHandStack(), EntityGroup.DEFAULT);
        float h = this.getAttackCooldownProgress(0.5f);
        g *= h;
        this.resetLastAttackedTicks();
        if ((f *= 0.2f + h * h * 0.8f) > 0.0f || g > 0.0f) {
            ItemStack lv;
            boolean bl = h > 0.9f;
            boolean bl2 = false;
            int i = 0;
            i += EnchantmentHelper.getKnockback(this);
            if (this.isSprinting() && bl) {
                this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0f, 1.0f);
                ++i;
                bl2 = true;
            }
            boolean bl3 = bl && this.fallDistance > 0.0f && !this.onGround && !this.isClimbing() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && !this.hasVehicle() && target instanceof LivingEntity;
            boolean bl4 = bl3 = bl3 && !this.isSprinting();
            if (bl3) {
                f *= 1.5f;
            }
            f += g;
            boolean bl42 = false;
            double d = this.horizontalSpeed - this.prevHorizontalSpeed;
            if (bl && !bl3 && !bl2 && this.onGround && d < (double)this.getMovementSpeed() && (lv = this.getStackInHand(Hand.MAIN_HAND)).getItem() instanceof SwordItem) {
                bl42 = true;
            }
            float j = 0.0f;
            boolean bl5 = false;
            int k = EnchantmentHelper.getFireAspect(this);
            if (target instanceof LivingEntity) {
                j = ((LivingEntity)target).getHealth();
                if (k > 0 && !target.isOnFire()) {
                    bl5 = true;
                    target.setOnFireFor(1);
                }
            }
            Vec3d lv2 = target.getVelocity();
            boolean bl6 = target.damage(this.getDamageSources().playerAttack(this), f);
            if (bl6) {
                if (i > 0) {
                    if (target instanceof LivingEntity) {
                        ((LivingEntity)target).takeKnockback((float)i * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                    } else {
                        target.addVelocity(-MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)) * (float)i * 0.5f, 0.1, MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)) * (float)i * 0.5f);
                    }
                    this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
                    this.setSprinting(false);
                }
                if (bl42) {
                    float l = 1.0f + EnchantmentHelper.getSweepingMultiplier(this) * f;
                    List<LivingEntity> list = this.world.getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0));
                    for (LivingEntity lv3 : list) {
                        if (lv3 == this || lv3 == target || this.isTeammate(lv3) || lv3 instanceof ArmorStandEntity && ((ArmorStandEntity)lv3).isMarker() || !(this.squaredDistanceTo(lv3) < 9.0)) continue;
                        lv3.takeKnockback(0.4f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                        lv3.damage(this.getDamageSources().playerAttack(this), l);
                    }
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0f, 1.0f);
                    this.spawnSweepAttackParticles();
                }
                if (target instanceof ServerPlayerEntity && target.velocityModified) {
                    ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                    target.velocityModified = false;
                    target.setVelocity(lv2);
                }
                if (bl3) {
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0f, 1.0f);
                    this.addCritParticles(target);
                }
                if (!bl3 && !bl42) {
                    if (bl) {
                        this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0f, 1.0f);
                    } else {
                        this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, this.getSoundCategory(), 1.0f, 1.0f);
                    }
                }
                if (g > 0.0f) {
                    this.addEnchantedHitParticles(target);
                }
                this.onAttacking(target);
                if (target instanceof LivingEntity) {
                    EnchantmentHelper.onUserDamaged((LivingEntity)target, this);
                }
                EnchantmentHelper.onTargetDamaged(this, target);
                ItemStack lv4 = this.getMainHandStack();
                Entity lv5 = target;
                if (target instanceof EnderDragonPart) {
                    lv5 = ((EnderDragonPart)target).owner;
                }
                if (!this.world.isClient && !lv4.isEmpty() && lv5 instanceof LivingEntity) {
                    lv4.postHit((LivingEntity)lv5, this);
                    if (lv4.isEmpty()) {
                        this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
                if (target instanceof LivingEntity) {
                    float m = j - ((LivingEntity)target).getHealth();
                    this.increaseStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0f));
                    if (k > 0) {
                        target.setOnFireFor(k * 4);
                    }
                    if (this.world instanceof ServerWorld && m > 2.0f) {
                        int n = (int)((double)m * 0.5);
                        ((ServerWorld)this.world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
                    }
                }
                this.addExhaustion(0.1f);
            } else {
                this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0f, 1.0f);
                if (bl5) {
                    target.extinguish();
                }
            }
        }
    }

    @Override
    protected void attackLivingEntity(LivingEntity target) {
        this.attack(target);
    }

    public void disableShield(boolean sprinting) {
        float f = 0.25f + (float)EnchantmentHelper.getEfficiency(this) * 0.05f;
        if (sprinting) {
            f += 0.75f;
        }
        if (this.random.nextFloat() < f) {
            this.getItemCooldownManager().set(Items.SHIELD, 100);
            this.clearActiveItem();
            this.world.sendEntityStatus(this, EntityStatuses.BREAK_SHIELD);
        }
    }

    public void addCritParticles(Entity target) {
    }

    public void addEnchantedHitParticles(Entity target) {
    }

    public void spawnSweepAttackParticles() {
        double d = -MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
        double e = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
        if (this.world instanceof ServerWorld) {
            ((ServerWorld)this.world).spawnParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getBodyY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
        }
    }

    public void requestRespawn() {
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        this.playerScreenHandler.onClosed(this);
        if (this.currentScreenHandler != null && this.shouldCloseHandledScreenOnRespawn()) {
            this.onHandledScreenClosed();
        }
    }

    public boolean isMainPlayer() {
        return false;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public PlayerInventory getInventory() {
        return this.inventory;
    }

    public PlayerAbilities getAbilities() {
        return this.abilities;
    }

    public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
    }

    public boolean shouldCloseHandledScreenOnRespawn() {
        return this.currentScreenHandler != this.playerScreenHandler;
    }

    public Either<SleepFailureReason, Unit> trySleep(BlockPos pos) {
        this.sleep(pos);
        this.sleepTimer = 0;
        return Either.right(Unit.INSTANCE);
    }

    public void wakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers) {
        super.wakeUp();
        if (this.world instanceof ServerWorld && updateSleepingPlayers) {
            ((ServerWorld)this.world).updateSleepingPlayers();
        }
        this.sleepTimer = skipSleepTimer ? 0 : 100;
    }

    @Override
    public void wakeUp() {
        this.wakeUp(true, true);
    }

    public static Optional<Vec3d> findRespawnPosition(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive) {
        BlockState lv = world.getBlockState(pos);
        Block lv2 = lv.getBlock();
        if (lv2 instanceof RespawnAnchorBlock && (forced || lv.get(RespawnAnchorBlock.CHARGES) > 0) && RespawnAnchorBlock.isNether(world)) {
            Optional<Vec3d> optional = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, pos);
            if (!forced && !alive && optional.isPresent()) {
                world.setBlockState(pos, (BlockState)lv.with(RespawnAnchorBlock.CHARGES, lv.get(RespawnAnchorBlock.CHARGES) - 1), Block.NOTIFY_ALL);
            }
            return optional;
        }
        if (lv2 instanceof BedBlock && BedBlock.isBedWorking(world)) {
            return BedBlock.findWakeUpPosition(EntityType.PLAYER, (CollisionView)world, pos, lv.get(BedBlock.FACING), angle);
        }
        if (!forced) {
            return Optional.empty();
        }
        boolean bl3 = lv2.canMobSpawnInside();
        boolean bl4 = world.getBlockState(pos.up()).getBlock().canMobSpawnInside();
        if (bl3 && bl4) {
            return Optional.of(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.1, (double)pos.getZ() + 0.5));
        }
        return Optional.empty();
    }

    public boolean canResetTimeBySleeping() {
        return this.isSleeping() && this.sleepTimer >= 100;
    }

    public int getSleepTimer() {
        return this.sleepTimer;
    }

    public void sendMessage(Text message, boolean overlay) {
    }

    public void incrementStat(Identifier stat) {
        this.incrementStat(Stats.CUSTOM.getOrCreateStat(stat));
    }

    public void increaseStat(Identifier stat, int amount) {
        this.increaseStat(Stats.CUSTOM.getOrCreateStat(stat), amount);
    }

    public void incrementStat(Stat<?> stat) {
        this.increaseStat(stat, 1);
    }

    public void increaseStat(Stat<?> stat, int amount) {
    }

    public void resetStat(Stat<?> stat) {
    }

    public int unlockRecipes(Collection<Recipe<?>> recipes) {
        return 0;
    }

    public void unlockRecipes(Identifier[] ids) {
    }

    public int lockRecipes(Collection<Recipe<?>> recipes) {
        return 0;
    }

    @Override
    public void jump() {
        super.jump();
        this.incrementStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.addExhaustion(0.2f);
        } else {
            this.addExhaustion(0.05f);
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        double g;
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        if (this.isSwimming() && !this.hasVehicle()) {
            double h;
            g = this.getRotationVector().y;
            double d2 = h = g < -0.2 ? 0.085 : 0.06;
            if (g <= 0.0 || this.jumping || !this.world.getBlockState(BlockPos.ofFloored(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).getFluidState().isEmpty()) {
                Vec3d lv = this.getVelocity();
                this.setVelocity(lv.add(0.0, (g - lv.y) * h, 0.0));
            }
        }
        if (this.abilities.flying && !this.hasVehicle()) {
            g = this.getVelocity().y;
            super.travel(movementInput);
            Vec3d lv2 = this.getVelocity();
            this.setVelocity(lv2.x, g * 0.6, lv2.z);
            this.onLanding();
            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
        } else {
            super.travel(movementInput);
        }
        this.increaseTravelMotionStats(this.getX() - d, this.getY() - e, this.getZ() - f);
    }

    @Override
    public void updateSwimming() {
        if (this.abilities.flying) {
            this.setSwimming(false);
        } else {
            super.updateSwimming();
        }
    }

    protected boolean doesNotSuffocate(BlockPos pos) {
        return !this.world.getBlockState(pos).shouldSuffocate(this.world, pos);
    }

    @Override
    public float getMovementSpeed() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    }

    public void increaseTravelMotionStats(double dx, double dy, double dz) {
        if (this.hasVehicle()) {
            return;
        }
        if (this.isSwimming()) {
            int i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0f);
            if (i > 0) {
                this.increaseStat(Stats.SWIM_ONE_CM, i);
                this.addExhaustion(0.01f * (float)i * 0.01f);
            }
        } else if (this.isSubmergedIn(FluidTags.WATER)) {
            int i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0f);
            if (i > 0) {
                this.increaseStat(Stats.WALK_UNDER_WATER_ONE_CM, i);
                this.addExhaustion(0.01f * (float)i * 0.01f);
            }
        } else if (this.isTouchingWater()) {
            int i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0f);
            if (i > 0) {
                this.increaseStat(Stats.WALK_ON_WATER_ONE_CM, i);
                this.addExhaustion(0.01f * (float)i * 0.01f);
            }
        } else if (this.isClimbing()) {
            if (dy > 0.0) {
                this.increaseStat(Stats.CLIMB_ONE_CM, (int)Math.round(dy * 100.0));
            }
        } else if (this.onGround) {
            int i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0f);
            if (i > 0) {
                if (this.isSprinting()) {
                    this.increaseStat(Stats.SPRINT_ONE_CM, i);
                    this.addExhaustion(0.1f * (float)i * 0.01f);
                } else if (this.isInSneakingPose()) {
                    this.increaseStat(Stats.CROUCH_ONE_CM, i);
                    this.addExhaustion(0.0f * (float)i * 0.01f);
                } else {
                    this.increaseStat(Stats.WALK_ONE_CM, i);
                    this.addExhaustion(0.0f * (float)i * 0.01f);
                }
            }
        } else if (this.isFallFlying()) {
            int i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0f);
            this.increaseStat(Stats.AVIATE_ONE_CM, i);
        } else {
            int i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0f);
            if (i > 25) {
                this.increaseStat(Stats.FLY_ONE_CM, i);
            }
        }
    }

    private void increaseRidingMotionStats(double dx, double dy, double dz) {
        int i;
        if (this.hasVehicle() && (i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0f)) > 0) {
            Entity lv = this.getVehicle();
            if (lv instanceof AbstractMinecartEntity) {
                this.increaseStat(Stats.MINECART_ONE_CM, i);
            } else if (lv instanceof BoatEntity) {
                this.increaseStat(Stats.BOAT_ONE_CM, i);
            } else if (lv instanceof PigEntity) {
                this.increaseStat(Stats.PIG_ONE_CM, i);
            } else if (lv instanceof AbstractHorseEntity) {
                this.increaseStat(Stats.HORSE_ONE_CM, i);
            } else if (lv instanceof StriderEntity) {
                this.increaseStat(Stats.STRIDER_ONE_CM, i);
            }
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (this.abilities.allowFlying) {
            return false;
        }
        if (fallDistance >= 2.0f) {
            this.increaseStat(Stats.FALL_ONE_CM, (int)Math.round((double)fallDistance * 100.0));
        }
        return super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
    }

    public boolean checkFallFlying() {
        ItemStack lv;
        if (!this.onGround && !this.isFallFlying() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.LEVITATION) && (lv = this.getEquippedStack(EquipmentSlot.CHEST)).isOf(Items.ELYTRA) && ElytraItem.isUsable(lv)) {
            this.startFallFlying();
            return true;
        }
        return false;
    }

    public void startFallFlying() {
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
    }

    public void stopFallFlying() {
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
    }

    @Override
    protected void onSwimmingStart() {
        if (!this.isSpectator()) {
            super.onSwimmingStart();
        }
    }

    @Override
    public LivingEntity.FallSounds getFallSounds() {
        return new LivingEntity.FallSounds(SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundEvents.ENTITY_PLAYER_BIG_FALL);
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other) {
        this.incrementStat(Stats.KILLED.getOrCreateStat(other.getType()));
        return true;
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
        if (!this.abilities.flying) {
            super.slowMovement(state, multiplier);
        }
    }

    public void addExperience(int experience) {
        this.addScore(experience);
        this.experienceProgress += (float)experience / (float)this.getNextLevelExperience();
        this.totalExperience = MathHelper.clamp(this.totalExperience + experience, 0, Integer.MAX_VALUE);
        while (this.experienceProgress < 0.0f) {
            float f = this.experienceProgress * (float)this.getNextLevelExperience();
            if (this.experienceLevel > 0) {
                this.addExperienceLevels(-1);
                this.experienceProgress = 1.0f + f / (float)this.getNextLevelExperience();
                continue;
            }
            this.addExperienceLevels(-1);
            this.experienceProgress = 0.0f;
        }
        while (this.experienceProgress >= 1.0f) {
            this.experienceProgress = (this.experienceProgress - 1.0f) * (float)this.getNextLevelExperience();
            this.addExperienceLevels(1);
            this.experienceProgress /= (float)this.getNextLevelExperience();
        }
    }

    public int getEnchantmentTableSeed() {
        return this.enchantmentTableSeed;
    }

    public void applyEnchantmentCosts(ItemStack enchantedItem, int experienceLevels) {
        this.experienceLevel -= experienceLevels;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        this.enchantmentTableSeed = this.random.nextInt();
    }

    public void addExperienceLevels(int levels) {
        this.experienceLevel += levels;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        if (levels > 0 && this.experienceLevel % 5 == 0 && (float)this.lastPlayedLevelUpSoundTime < (float)this.age - 100.0f) {
            float f = this.experienceLevel > 30 ? 1.0f : (float)this.experienceLevel / 30.0f;
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), f * 0.75f, 1.0f);
            this.lastPlayedLevelUpSoundTime = this.age;
        }
    }

    public int getNextLevelExperience() {
        if (this.experienceLevel >= 30) {
            return 112 + (this.experienceLevel - 30) * 9;
        }
        if (this.experienceLevel >= 15) {
            return 37 + (this.experienceLevel - 15) * 5;
        }
        return 7 + this.experienceLevel * 2;
    }

    public void addExhaustion(float exhaustion) {
        if (this.abilities.invulnerable) {
            return;
        }
        if (!this.world.isClient) {
            this.hungerManager.addExhaustion(exhaustion);
        }
    }

    public Optional<SculkShriekerWarningManager> getSculkShriekerWarningManager() {
        return Optional.empty();
    }

    public HungerManager getHungerManager() {
        return this.hungerManager;
    }

    public boolean canConsume(boolean ignoreHunger) {
        return this.abilities.invulnerable || ignoreHunger || this.hungerManager.isNotFull();
    }

    public boolean canFoodHeal() {
        return this.getHealth() > 0.0f && this.getHealth() < this.getMaxHealth();
    }

    public boolean canModifyBlocks() {
        return this.abilities.allowModifyWorld;
    }

    public boolean canPlaceOn(BlockPos pos, Direction facing, ItemStack stack) {
        if (this.abilities.allowModifyWorld) {
            return true;
        }
        BlockPos lv = pos.offset(facing.getOpposite());
        CachedBlockPosition lv2 = new CachedBlockPosition(this.world, lv, false);
        return stack.canPlaceOn(this.world.getRegistryManager().get(RegistryKeys.BLOCK), lv2);
    }

    @Override
    public int getXpToDrop() {
        if (this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || this.isSpectator()) {
            return 0;
        }
        int i = this.experienceLevel * 7;
        if (i > 100) {
            return 100;
        }
        return i;
    }

    @Override
    protected boolean shouldAlwaysDropXp() {
        return true;
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return !this.abilities.flying && (!this.onGround || !this.isSneaky()) ? Entity.MoveEffect.ALL : Entity.MoveEffect.NONE;
    }

    public void sendAbilitiesUpdate() {
    }

    @Override
    public Text getName() {
        return Text.literal(this.gameProfile.getName());
    }

    public EnderChestInventory getEnderChestInventory() {
        return this.enderChestInventory;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.inventory.getMainHandStack();
        }
        if (slot == EquipmentSlot.OFFHAND) {
            return this.inventory.offHand.get(0);
        }
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            return this.inventory.armor.get(slot.getEntitySlotId());
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean isArmorSlot(EquipmentSlot slot) {
        return slot.getType() == EquipmentSlot.Type.ARMOR;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        if (slot == EquipmentSlot.MAINHAND) {
            this.onEquipStack(slot, this.inventory.main.set(this.inventory.selectedSlot, stack), stack);
        } else if (slot == EquipmentSlot.OFFHAND) {
            this.onEquipStack(slot, this.inventory.offHand.set(0, stack), stack);
        } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            this.onEquipStack(slot, this.inventory.armor.set(slot.getEntitySlotId(), stack), stack);
        }
    }

    public boolean giveItemStack(ItemStack stack) {
        return this.inventory.insertStack(stack);
    }

    @Override
    public Iterable<ItemStack> getHandItems() {
        return Lists.newArrayList(this.getMainHandStack(), this.getOffHandStack());
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.inventory.armor;
    }

    public boolean addShoulderEntity(NbtCompound entityNbt) {
        if (this.hasVehicle() || !this.onGround || this.isTouchingWater() || this.inPowderSnow) {
            return false;
        }
        if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(entityNbt);
            this.shoulderEntityAddedTime = this.world.getTime();
            return true;
        }
        if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(entityNbt);
            this.shoulderEntityAddedTime = this.world.getTime();
            return true;
        }
        return false;
    }

    protected void dropShoulderEntities() {
        if (this.shoulderEntityAddedTime + 20L < this.world.getTime()) {
            this.dropShoulderEntity(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new NbtCompound());
            this.dropShoulderEntity(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new NbtCompound());
        }
    }

    private void dropShoulderEntity(NbtCompound entityNbt) {
        if (!this.world.isClient && !entityNbt.isEmpty()) {
            EntityType.getEntityFromNbt(entityNbt, this.world).ifPresent(entity -> {
                if (entity instanceof TameableEntity) {
                    ((TameableEntity)entity).setOwnerUuid(this.uuid);
                }
                entity.setPosition(this.getX(), this.getY() + (double)0.7f, this.getZ());
                ((ServerWorld)this.world).tryLoadEntity((Entity)entity);
            });
        }
    }

    @Override
    public abstract boolean isSpectator();

    @Override
    public boolean canBeHitByProjectile() {
        return !this.isSpectator() && super.canBeHitByProjectile();
    }

    @Override
    public boolean isSwimming() {
        return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
    }

    public abstract boolean isCreative();

    @Override
    public boolean isPushedByFluids() {
        return !this.abilities.flying;
    }

    public Scoreboard getScoreboard() {
        return this.world.getScoreboard();
    }

    @Override
    public Text getDisplayName() {
        MutableText lv = Team.decorateName(this.getScoreboardTeam(), this.getName());
        return this.addTellClickEvent(lv);
    }

    private MutableText addTellClickEvent(MutableText component) {
        String string = this.getGameProfile().getName();
        return component.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + string + " ")).withHoverEvent(this.getHoverEvent()).withInsertion(string));
    }

    @Override
    public String getEntityName() {
        return this.getGameProfile().getName();
    }

    @Override
    public float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        switch (pose) {
            case SWIMMING: 
            case FALL_FLYING: 
            case SPIN_ATTACK: {
                return 0.4f;
            }
            case CROUCHING: {
                return 1.27f;
            }
        }
        return 1.62f;
    }

    @Override
    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0f) {
            amount = 0.0f;
        }
        this.getDataTracker().set(ABSORPTION_AMOUNT, Float.valueOf(amount));
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getDataTracker().get(ABSORPTION_AMOUNT).floatValue();
    }

    public boolean isPartVisible(PlayerModelPart modelPart) {
        return (this.getDataTracker().get(PLAYER_MODEL_PARTS) & modelPart.getBitFlag()) == modelPart.getBitFlag();
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        if (mappedIndex >= 0 && mappedIndex < this.inventory.main.size()) {
            return StackReference.of(this.inventory, mappedIndex);
        }
        int j = mappedIndex - 200;
        if (j >= 0 && j < this.enderChestInventory.size()) {
            return StackReference.of(this.enderChestInventory, j);
        }
        return super.getStackReference(mappedIndex);
    }

    public boolean hasReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        this.reducedDebugInfo = reducedDebugInfo;
    }

    @Override
    public void setFireTicks(int fireTicks) {
        super.setFireTicks(this.abilities.invulnerable ? Math.min(fireTicks, 1) : fireTicks);
    }

    @Override
    public Arm getMainArm() {
        return this.dataTracker.get(MAIN_ARM) == 0 ? Arm.LEFT : Arm.RIGHT;
    }

    public void setMainArm(Arm arm) {
        this.dataTracker.set(MAIN_ARM, (byte)(arm != Arm.LEFT ? 1 : 0));
    }

    public NbtCompound getShoulderEntityLeft() {
        return this.dataTracker.get(LEFT_SHOULDER_ENTITY);
    }

    protected void setShoulderEntityLeft(NbtCompound entityNbt) {
        this.dataTracker.set(LEFT_SHOULDER_ENTITY, entityNbt);
    }

    public NbtCompound getShoulderEntityRight() {
        return this.dataTracker.get(RIGHT_SHOULDER_ENTITY);
    }

    protected void setShoulderEntityRight(NbtCompound entityNbt) {
        this.dataTracker.set(RIGHT_SHOULDER_ENTITY, entityNbt);
    }

    public float getAttackCooldownProgressPerTick() {
        return (float)(1.0 / this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }

    public float getAttackCooldownProgress(float baseTime) {
        return MathHelper.clamp(((float)this.lastAttackedTicks + baseTime) / this.getAttackCooldownProgressPerTick(), 0.0f, 1.0f);
    }

    public void resetLastAttackedTicks() {
        this.lastAttackedTicks = 0;
    }

    public ItemCooldownManager getItemCooldownManager() {
        return this.itemCooldownManager;
    }

    @Override
    protected float getVelocityMultiplier() {
        return this.abilities.flying || this.isFallFlying() ? 1.0f : super.getVelocityMultiplier();
    }

    public float getLuck() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_LUCK);
    }

    public boolean isCreativeLevelTwoOp() {
        return this.abilities.creativeMode && this.getPermissionLevel() >= 2;
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        EquipmentSlot lv = MobEntity.getPreferredEquipmentSlot(stack);
        return this.getEquippedStack(lv).isEmpty();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return POSE_DIMENSIONS.getOrDefault((Object)pose, STANDING_DIMENSIONS);
    }

    @Override
    public ImmutableList<EntityPose> getPoses() {
        return ImmutableList.of(EntityPose.STANDING, EntityPose.CROUCHING, EntityPose.SWIMMING);
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        if (!(stack.getItem() instanceof RangedWeaponItem)) {
            return ItemStack.EMPTY;
        }
        Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
        ItemStack lv = RangedWeaponItem.getHeldProjectile(this, predicate);
        if (!lv.isEmpty()) {
            return lv;
        }
        predicate = ((RangedWeaponItem)stack.getItem()).getProjectiles();
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack lv2 = this.inventory.getStack(i);
            if (!predicate.test(lv2)) continue;
            return lv2;
        }
        return this.abilities.creativeMode ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack eatFood(World world, ItemStack stack) {
        this.getHungerManager().eat(stack.getItem(), stack);
        this.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
        if (this instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)this, stack);
        }
        return super.eatFood(world, stack);
    }

    @Override
    protected boolean shouldRemoveSoulSpeedBoost(BlockState landingState) {
        return this.abilities.flying || super.shouldRemoveSoulSpeedBoost(landingState);
    }

    @Override
    public Vec3d getLeashPos(float delta) {
        double d = 0.22 * (this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0);
        float g = MathHelper.lerp(delta * 0.5f, this.getPitch(), this.prevPitch) * ((float)Math.PI / 180);
        float h = MathHelper.lerp(delta, this.prevBodyYaw, this.bodyYaw) * ((float)Math.PI / 180);
        if (this.isFallFlying() || this.isUsingRiptide()) {
            float l;
            Vec3d lv = this.getRotationVec(delta);
            Vec3d lv2 = this.getVelocity();
            double e = lv2.horizontalLengthSquared();
            double i = lv.horizontalLengthSquared();
            if (e > 0.0 && i > 0.0) {
                double j = (lv2.x * lv.x + lv2.z * lv.z) / Math.sqrt(e * i);
                double k = lv2.x * lv.z - lv2.z * lv.x;
                l = (float)(Math.signum(k) * Math.acos(j));
            } else {
                l = 0.0f;
            }
            return this.getLerpedPos(delta).add(new Vec3d(d, -0.11, 0.85).rotateZ(-l).rotateX(-g).rotateY(-h));
        }
        if (this.isInSwimmingPose()) {
            return this.getLerpedPos(delta).add(new Vec3d(d, 0.2, -0.15).rotateX(-g).rotateY(-h));
        }
        double m = this.getBoundingBox().getYLength() - 1.0;
        double e = this.isInSneakingPose() ? -0.2 : 0.07;
        return this.getLerpedPos(delta).add(new Vec3d(d, m, e).rotateY(-h));
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    public boolean isUsingSpyglass() {
        return this.isUsingItem() && this.getActiveItem().isOf(Items.SPYGLASS);
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    public Optional<GlobalPos> getLastDeathPos() {
        return this.lastDeathPos;
    }

    public void setLastDeathPos(Optional<GlobalPos> lastDeathPos) {
        this.lastDeathPos = lastDeathPos;
    }

    @Override
    public float getDamageTiltYaw() {
        return this.damageTiltYaw;
    }

    @Override
    public void animateDamage(float yaw) {
        super.animateDamage(yaw);
        this.damageTiltYaw = yaw;
    }

    @Override
    public boolean canSprintAsVehicle() {
        return true;
    }

    @Override
    protected float getOffGroundSpeed() {
        if (this.abilities.flying && !this.hasVehicle()) {
            return this.isSprinting() ? this.abilities.getFlySpeed() * 2.0f : this.abilities.getFlySpeed();
        }
        return this.isSprinting() ? 0.025999999f : 0.02f;
    }

    public static enum SleepFailureReason {
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW(Text.translatable("block.minecraft.bed.no_sleep")),
        TOO_FAR_AWAY(Text.translatable("block.minecraft.bed.too_far_away")),
        OBSTRUCTED(Text.translatable("block.minecraft.bed.obstructed")),
        OTHER_PROBLEM,
        NOT_SAFE(Text.translatable("block.minecraft.bed.not_safe"));

        @Nullable
        private final Text message;

        private SleepFailureReason() {
            this.message = null;
        }

        private SleepFailureReason(Text message) {
            this.message = message;
        }

        @Nullable
        public Text getMessage() {
            return this.message;
        }
    }
}

