/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class StriderEntity
extends AnimalEntity
implements ItemSteerable,
Saddleable {
    private static final UUID SUFFOCATING_MODIFIER_ID = UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174");
    private static final EntityAttributeModifier SUFFOCATING_MODIFIER = new EntityAttributeModifier(SUFFOCATING_MODIFIER_ID, "Strider suffocating modifier", (double)-0.34f, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    private static final float COLD_SADDLED_SPEED = 0.35f;
    private static final float DEFAULT_SADDLED_SPEED = 0.55f;
    private static final Ingredient BREEDING_INGREDIENT = Ingredient.ofItems(Items.WARPED_FUNGUS);
    private static final Ingredient ATTRACTING_INGREDIENT = Ingredient.ofItems(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
    private static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> COLD = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SADDLED = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final SaddledComponent saddledComponent;
    @Nullable
    private TemptGoal temptGoal;
    @Nullable
    private EscapeDangerGoal escapeDangerGoal;

    public StriderEntity(EntityType<? extends StriderEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, SADDLED);
        this.intersectionChecked = true;
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0f);
        this.setPathfindingPenalty(PathNodeType.LAVA, 0.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 0.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, 0.0f);
    }

    public static boolean canSpawn(EntityType<StriderEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        BlockPos.Mutable lv = pos.mutableCopy();
        do {
            lv.move(Direction.UP);
        } while (world.getFluidState(lv).isIn(FluidTags.LAVA));
        return world.getBlockState(lv).isAir();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (BOOST_TIME.equals(data) && this.world.isClient) {
            this.saddledComponent.boost();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BOOST_TIME, 0);
        this.dataTracker.startTracking(COLD, false);
        this.dataTracker.startTracking(SADDLED, false);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.saddledComponent.writeNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.saddledComponent.readNbt(nbt);
    }

    @Override
    public boolean isSaddled() {
        return this.saddledComponent.isSaddled();
    }

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void saddle(@Nullable SoundCategory sound) {
        this.saddledComponent.setSaddled(true);
        if (sound != null) {
            this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_STRIDER_SADDLE, sound, 0.5f, 1.0f);
        }
    }

    @Override
    protected void initGoals() {
        this.escapeDangerGoal = new EscapeDangerGoal(this, 1.65);
        this.goalSelector.add(1, this.escapeDangerGoal);
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
        this.temptGoal = new TemptGoal(this, 1.4, ATTRACTING_INGREDIENT, false);
        this.goalSelector.add(3, this.temptGoal);
        this.goalSelector.add(4, new GoBackToLavaGoal(this, 1.0));
        this.goalSelector.add(5, new FollowParentGoal(this, 1.0));
        this.goalSelector.add(7, new WanderAroundGoal(this, 1.0, 60));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, StriderEntity.class, 8.0f));
    }

    public void setCold(boolean cold) {
        this.dataTracker.set(COLD, cold);
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (lv != null) {
            lv.removeModifier(SUFFOCATING_MODIFIER_ID);
            if (cold) {
                lv.addTemporaryModifier(SUFFOCATING_MODIFIER);
            }
        }
    }

    public boolean isCold() {
        return this.dataTracker.get(COLD);
    }

    @Override
    public boolean canWalkOnFluid(FluidState state) {
        return state.isIn(FluidTags.LAVA);
    }

    @Override
    public double getMountedHeightOffset() {
        float f = Math.min(0.25f, this.limbAnimator.getSpeed());
        float g = this.limbAnimator.getPos();
        return (double)this.getHeight() - 0.19 + (double)(0.12f * MathHelper.cos(g * 1.5f) * 2.0f * f);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        PlayerEntity lv;
        Entity entity = this.getFirstPassenger();
        if (entity instanceof PlayerEntity && ((lv = (PlayerEntity)entity).getMainHandStack().isOf(Items.WARPED_FUNGUS_ON_A_STICK) || lv.getOffHandStack().isOf(Items.WARPED_FUNGUS_ON_A_STICK))) {
            return lv;
        }
        return null;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Vec3d[] lvs = new Vec3d[]{StriderEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), passenger.getYaw()), StriderEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), passenger.getYaw() - 22.5f), StriderEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), passenger.getYaw() + 22.5f), StriderEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), passenger.getYaw() - 45.0f), StriderEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), passenger.getYaw() + 45.0f)};
        LinkedHashSet<BlockPos> set = Sets.newLinkedHashSet();
        double d = this.getBoundingBox().maxY;
        double e = this.getBoundingBox().minY - 0.5;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (Vec3d lv2 : lvs) {
            lv.set(this.getX() + lv2.x, d, this.getZ() + lv2.z);
            for (double f = d; f > e; f -= 1.0) {
                set.add(lv.toImmutable());
                lv.move(Direction.DOWN);
            }
        }
        for (BlockPos lv3 : set) {
            double g;
            if (this.world.getFluidState(lv3).isIn(FluidTags.LAVA) || !Dismounting.canDismountInBlock(g = this.world.getDismountHeight(lv3))) continue;
            Vec3d lv4 = Vec3d.ofCenter(lv3, g);
            for (EntityPose lv5 : passenger.getPoses()) {
                Box lv6 = passenger.getBoundingBox(lv5);
                if (!Dismounting.canPlaceEntityAt(this.world, passenger, lv6.offset(lv4))) continue;
                passenger.setPose(lv5);
                return lv4;
            }
        }
        return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Override
    protected void tickControlled(LivingEntity controllingPassenger, Vec3d movementInput) {
        this.setRotation(controllingPassenger.getYaw(), controllingPassenger.getPitch() * 0.5f);
        this.bodyYaw = this.headYaw = this.getYaw();
        this.prevYaw = this.headYaw;
        this.saddledComponent.tickBoost();
        super.tickControlled(controllingPassenger, movementInput);
    }

    @Override
    protected Vec3d getControlledMovementInput(LivingEntity controllingPassenger, Vec3d movementInput) {
        return new Vec3d(0.0, 0.0, 1.0);
    }

    @Override
    protected float getSaddledSpeed(LivingEntity controllingPassenger) {
        return (float)(this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (double)(this.isCold() ? 0.35f : 0.55f) * (double)this.saddledComponent.getMovementSpeedMultiplier());
    }

    @Override
    protected float calculateNextStepSoundDistance() {
        return this.distanceTraveled + 0.6f;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.isInLava() ? SoundEvents.ENTITY_STRIDER_STEP_LAVA : SoundEvents.ENTITY_STRIDER_STEP, 1.0f, 1.0f);
    }

    @Override
    public boolean consumeOnAStickItem() {
        return this.saddledComponent.boost(this.getRandom());
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        this.checkBlockCollision();
        if (this.isInLava()) {
            this.onLanding();
            return;
        }
        super.fall(heightDifference, onGround, state, landedPosition);
    }

    @Override
    public void tick() {
        if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
            this.playSound(SoundEvents.ENTITY_STRIDER_HAPPY, 1.0f, this.getSoundPitch());
        } else if (this.isEscapingDanger() && this.random.nextInt(60) == 0) {
            this.playSound(SoundEvents.ENTITY_STRIDER_RETREAT, 1.0f, this.getSoundPitch());
        }
        if (!this.isAiDisabled()) {
            StriderEntity lv3;
            BlockState lv = this.world.getBlockState(this.getBlockPos());
            BlockState lv2 = this.getLandingBlockState();
            boolean bl = lv.isIn(BlockTags.STRIDER_WARM_BLOCKS) || lv2.isIn(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
            Entity entity = this.getVehicle();
            boolean bl2 = entity instanceof StriderEntity && (lv3 = (StriderEntity)entity).isCold();
            this.setCold(!bl || bl2);
        }
        super.tick();
        this.updateFloating();
        this.checkBlockCollision();
    }

    private boolean isEscapingDanger() {
        return this.escapeDangerGoal != null && this.escapeDangerGoal.isActive();
    }

    private boolean isBeingTempted() {
        return this.temptGoal != null && this.temptGoal.isActive();
    }

    @Override
    protected boolean movesIndependently() {
        return true;
    }

    private void updateFloating() {
        if (this.isInLava()) {
            ShapeContext lv = ShapeContext.of(this);
            if (!lv.isAbove(FluidBlock.COLLISION_SHAPE, this.getBlockPos(), true) || this.world.getFluidState(this.getBlockPos().up()).isIn(FluidTags.LAVA)) {
                this.setVelocity(this.getVelocity().multiply(0.5).add(0.0, 0.05, 0.0));
            } else {
                this.onGround = true;
            }
        }
    }

    public static DefaultAttributeContainer.Builder createStriderAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.175f).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isEscapingDanger() || this.isBeingTempted()) {
            return null;
        }
        return SoundEvents.ENTITY_STRIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_STRIDER_DEATH;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return !this.hasPassengers() && !this.isSubmergedIn(FluidTags.LAVA);
    }

    @Override
    public boolean hurtByWater() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new Navigation(this, world);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (world.getBlockState(pos).getFluidState().isIn(FluidTags.LAVA)) {
            return 10.0f;
        }
        return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0f;
    }

    @Override
    @Nullable
    public StriderEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        return EntityType.STRIDER.create(arg);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return BREEDING_INGREDIENT.test(stack);
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (this.isSaddled()) {
            this.dropItem(Items.SADDLE);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        boolean bl = this.isBreedingItem(player.getStackInHand(hand));
        if (!bl && this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
            if (!this.world.isClient) {
                player.startRiding(this);
            }
            return ActionResult.success(this.world.isClient);
        }
        ActionResult lv = super.interactMob(player, hand);
        if (!lv.isAccepted()) {
            ItemStack lv2 = player.getStackInHand(hand);
            if (lv2.isOf(Items.SADDLE)) {
                return lv2.useOnEntity(player, this, hand);
            }
            return ActionResult.PASS;
        }
        if (bl && !this.isSilent()) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_STRIDER_EAT, this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return lv;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (this.isBaby()) {
            return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        }
        Random lv = world.getRandom();
        if (lv.nextInt(30) == 0) {
            MobEntity lv2 = EntityType.ZOMBIFIED_PIGLIN.create(world.toServerWorld());
            if (lv2 != null) {
                entityData = this.initializeRider(world, difficulty, lv2, new ZombieEntity.ZombieData(ZombieEntity.shouldBeBaby(lv), false));
                lv2.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                this.saddle(null);
            }
        } else if (lv.nextInt(10) == 0) {
            PassiveEntity lv3 = EntityType.STRIDER.create(world.toServerWorld());
            if (lv3 != null) {
                lv3.setBreedingAge(-24000);
                entityData = this.initializeRider(world, difficulty, lv3, null);
            }
        } else {
            entityData = new PassiveEntity.PassiveData(0.5f);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    private EntityData initializeRider(ServerWorldAccess world, LocalDifficulty difficulty, MobEntity rider, @Nullable EntityData entityData) {
        rider.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0f);
        rider.initialize(world, difficulty, SpawnReason.JOCKEY, entityData, null);
        rider.startRiding(this, true);
        return new PassiveEntity.PassiveData(0.0f);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    static class GoBackToLavaGoal
    extends MoveToTargetPosGoal {
        private final StriderEntity strider;

        GoBackToLavaGoal(StriderEntity strider, double speed) {
            super(strider, speed, 8, 2);
            this.strider = strider;
        }

        @Override
        public BlockPos getTargetPos() {
            return this.targetPos;
        }

        @Override
        public boolean shouldContinue() {
            return !this.strider.isInLava() && this.isTargetPos(this.strider.world, this.targetPos);
        }

        @Override
        public boolean canStart() {
            return !this.strider.isInLava() && super.canStart();
        }

        @Override
        public boolean shouldResetPath() {
            return this.tryingTime % 20 == 0;
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            return world.getBlockState(pos).isOf(Blocks.LAVA) && world.getBlockState(pos.up()).canPathfindThrough(world, pos, NavigationType.LAND);
        }
    }

    static class Navigation
    extends MobNavigation {
        Navigation(StriderEntity entity, World world) {
            super(entity, world);
        }

        @Override
        protected PathNodeNavigator createPathNodeNavigator(int range) {
            this.nodeMaker = new LandPathNodeMaker();
            this.nodeMaker.setCanEnterOpenDoors(true);
            return new PathNodeNavigator(this.nodeMaker, range);
        }

        @Override
        protected boolean canWalkOnPath(PathNodeType pathType) {
            if (pathType == PathNodeType.LAVA || pathType == PathNodeType.DAMAGE_FIRE || pathType == PathNodeType.DANGER_FIRE) {
                return true;
            }
            return super.canWalkOnPath(pathType);
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            return this.world.getBlockState(pos).isOf(Blocks.LAVA) || super.isValidPosition(pos);
        }
    }
}

