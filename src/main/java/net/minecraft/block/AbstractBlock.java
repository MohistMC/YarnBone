/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBlock
implements ToggleableFeature {
    protected static final Direction[] DIRECTIONS = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
    protected final Material material;
    protected final boolean collidable;
    protected final float resistance;
    protected final boolean randomTicks;
    protected final BlockSoundGroup soundGroup;
    protected final float slipperiness;
    protected final float velocityMultiplier;
    protected final float jumpVelocityMultiplier;
    protected final boolean dynamicBounds;
    protected final FeatureSet requiredFeatures;
    protected final Settings settings;
    @Nullable
    protected Identifier lootTableId;

    public AbstractBlock(Settings settings) {
        this.material = settings.material;
        this.collidable = settings.collidable;
        this.lootTableId = settings.lootTableId;
        this.resistance = settings.resistance;
        this.randomTicks = settings.randomTicks;
        this.soundGroup = settings.soundGroup;
        this.slipperiness = settings.slipperiness;
        this.velocityMultiplier = settings.velocityMultiplier;
        this.jumpVelocityMultiplier = settings.jumpVelocityMultiplier;
        this.dynamicBounds = settings.dynamicBounds;
        this.requiredFeatures = settings.requiredFeatures;
        this.settings = settings;
    }

    @Deprecated
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
    }

    @Deprecated
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        switch (type) {
            case LAND: {
                return !state.isFullCube(world, pos);
            }
            case WATER: {
                return world.getFluidState(pos).isIn(FluidTags.WATER);
            }
            case AIR: {
                return !state.isFullCube(world, pos);
            }
        }
        return false;
    }

    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    @Deprecated
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return false;
    }

    @Deprecated
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        DebugInfoSender.sendNeighborUpdate(world, pos);
    }

    @Deprecated
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
    }

    @Deprecated
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.hasBlockEntity() && !state.isOf(newState.getBlock())) {
            world.removeBlockEntity(pos);
        }
    }

    @Deprecated
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    @Deprecated
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        return false;
    }

    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Deprecated
    public boolean hasSidedTransparency(BlockState state) {
        return false;
    }

    @Deprecated
    public boolean emitsRedstonePower(BlockState state) {
        return false;
    }

    @Deprecated
    public PistonBehavior getPistonBehavior(BlockState state) {
        return this.material.getPistonBehavior();
    }

    @Deprecated
    public FluidState getFluidState(BlockState state) {
        return Fluids.EMPTY.getDefaultState();
    }

    @Deprecated
    public boolean hasComparatorOutput(BlockState state) {
        return false;
    }

    public float getMaxHorizontalModelOffset() {
        return 0.25f;
    }

    public float getVerticalModelOffsetMultiplier() {
        return 0.2f;
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.requiredFeatures;
    }

    @Deprecated
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state;
    }

    @Deprecated
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state;
    }

    @Deprecated
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return this.material.isReplaceable() && (context.getStack().isEmpty() || !context.getStack().isOf(this.asItem()));
    }

    @Deprecated
    public boolean canBucketPlace(BlockState state, Fluid fluid) {
        return this.material.isReplaceable() || !this.material.isSolid();
    }

    @Deprecated
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        Identifier lv = this.getLootTableId();
        if (lv == LootTables.EMPTY) {
            return Collections.emptyList();
        }
        LootContext lv2 = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
        ServerWorld lv3 = lv2.getWorld();
        LootTable lv4 = lv3.getServer().getLootManager().getTable(lv);
        return lv4.generateLoot(lv2);
    }

    @Deprecated
    public long getRenderingSeed(BlockState state, BlockPos pos) {
        return MathHelper.hashCode(pos);
    }

    @Deprecated
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return state.getOutlineShape(world, pos);
    }

    @Deprecated
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return this.getCollisionShape(state, world, pos, ShapeContext.absent());
    }

    @Deprecated
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Deprecated
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        if (state.isOpaqueFullCube(world, pos)) {
            return world.getMaxLightLevel();
        }
        return state.isTransparent(world, pos) ? 0 : 1;
    }

    @Nullable
    @Deprecated
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return null;
    }

    @Deprecated
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Deprecated
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return state.isFullCube(world, pos) ? 0.2f : 1.0f;
    }

    @Deprecated
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 0;
    }

    @Deprecated
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Deprecated
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collidable ? state.getOutlineShape(world, pos) : VoxelShapes.empty();
    }

    @Deprecated
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return Block.isShapeFullCube(state.getCollisionShape(world, pos));
    }

    @Deprecated
    public boolean isCullingShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return Block.isShapeFullCube(state.getCullingShape(world, pos));
    }

    @Deprecated
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getCollisionShape(state, world, pos, context);
    }

    @Deprecated
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.scheduledTick(state, world, pos, random);
    }

    @Deprecated
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

    @Deprecated
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float f = state.getHardness(world, pos);
        if (f == -1.0f) {
            return 0.0f;
        }
        int i = player.canHarvest(state) ? 30 : 100;
        return player.getBlockBreakingSpeed(state) / f / (float)i;
    }

    @Deprecated
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
    }

    @Deprecated
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
    }

    @Deprecated
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    @Deprecated
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
    }

    @Deprecated
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public final Identifier getLootTableId() {
        if (this.lootTableId == null) {
            Identifier lv = Registries.BLOCK.getId(this.asBlock());
            this.lootTableId = lv.withPrefixedPath("blocks/");
        }
        return this.lootTableId;
    }

    @Deprecated
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public MapColor getDefaultMapColor() {
        return this.settings.mapColorProvider.apply(this.asBlock().getDefaultState());
    }

    public float getHardness() {
        return this.settings.hardness;
    }

    public static class Settings {
        Material material;
        Function<BlockState, MapColor> mapColorProvider;
        boolean collidable = true;
        BlockSoundGroup soundGroup = BlockSoundGroup.STONE;
        ToIntFunction<BlockState> luminance = state -> 0;
        float resistance;
        float hardness;
        boolean toolRequired;
        boolean randomTicks;
        float slipperiness = 0.6f;
        float velocityMultiplier = 1.0f;
        float jumpVelocityMultiplier = 1.0f;
        Identifier lootTableId;
        boolean opaque = true;
        boolean isAir;
        boolean blockBreakParticles = true;
        TypedContextPredicate<EntityType<?>> allowsSpawningPredicate = (state, world, pos, type) -> state.isSideSolidFullSquare(world, pos, Direction.UP) && state.getLuminance() < 14;
        ContextPredicate solidBlockPredicate = (state, world, pos) -> state.getMaterial().blocksLight() && state.isFullCube(world, pos);
        ContextPredicate suffocationPredicate;
        ContextPredicate blockVisionPredicate = this.suffocationPredicate = (state, world, pos) -> this.material.blocksMovement() && state.isFullCube(world, pos);
        ContextPredicate postProcessPredicate = (state, world, pos) -> false;
        ContextPredicate emissiveLightingPredicate = (state, world, pos) -> false;
        boolean dynamicBounds;
        FeatureSet requiredFeatures = FeatureFlags.VANILLA_FEATURES;
        Optional<Offsetter> offsetter = Optional.empty();

        private Settings(Material material, MapColor mapColorProvider) {
            this(material, (BlockState state) -> mapColorProvider);
        }

        private Settings(Material material, Function<BlockState, MapColor> mapColorProvider) {
            this.material = material;
            this.mapColorProvider = mapColorProvider;
        }

        public static Settings of(Material material) {
            return Settings.of(material, material.getColor());
        }

        public static Settings of(Material material, DyeColor color) {
            return Settings.of(material, color.getMapColor());
        }

        public static Settings of(Material material, MapColor color) {
            return new Settings(material, color);
        }

        public static Settings of(Material material, Function<BlockState, MapColor> mapColor) {
            return new Settings(material, mapColor);
        }

        public static Settings copy(AbstractBlock block) {
            Settings lv = new Settings(block.material, block.settings.mapColorProvider);
            lv.material = block.settings.material;
            lv.hardness = block.settings.hardness;
            lv.resistance = block.settings.resistance;
            lv.collidable = block.settings.collidable;
            lv.randomTicks = block.settings.randomTicks;
            lv.luminance = block.settings.luminance;
            lv.mapColorProvider = block.settings.mapColorProvider;
            lv.soundGroup = block.settings.soundGroup;
            lv.slipperiness = block.settings.slipperiness;
            lv.velocityMultiplier = block.settings.velocityMultiplier;
            lv.dynamicBounds = block.settings.dynamicBounds;
            lv.opaque = block.settings.opaque;
            lv.isAir = block.settings.isAir;
            lv.toolRequired = block.settings.toolRequired;
            lv.offsetter = block.settings.offsetter;
            lv.blockBreakParticles = block.settings.blockBreakParticles;
            lv.requiredFeatures = block.settings.requiredFeatures;
            return lv;
        }

        public Settings noCollision() {
            this.collidable = false;
            this.opaque = false;
            return this;
        }

        public Settings nonOpaque() {
            this.opaque = false;
            return this;
        }

        public Settings slipperiness(float slipperiness) {
            this.slipperiness = slipperiness;
            return this;
        }

        public Settings velocityMultiplier(float velocityMultiplier) {
            this.velocityMultiplier = velocityMultiplier;
            return this;
        }

        public Settings jumpVelocityMultiplier(float jumpVelocityMultiplier) {
            this.jumpVelocityMultiplier = jumpVelocityMultiplier;
            return this;
        }

        public Settings sounds(BlockSoundGroup soundGroup) {
            this.soundGroup = soundGroup;
            return this;
        }

        public Settings luminance(ToIntFunction<BlockState> luminance) {
            this.luminance = luminance;
            return this;
        }

        public Settings strength(float hardness, float resistance) {
            return this.hardness(hardness).resistance(resistance);
        }

        public Settings breakInstantly() {
            return this.strength(0.0f);
        }

        public Settings strength(float strength) {
            this.strength(strength, strength);
            return this;
        }

        public Settings ticksRandomly() {
            this.randomTicks = true;
            return this;
        }

        public Settings dynamicBounds() {
            this.dynamicBounds = true;
            return this;
        }

        public Settings dropsNothing() {
            this.lootTableId = LootTables.EMPTY;
            return this;
        }

        public Settings dropsLike(Block source) {
            this.lootTableId = source.getLootTableId();
            return this;
        }

        public Settings air() {
            this.isAir = true;
            return this;
        }

        public Settings allowsSpawning(TypedContextPredicate<EntityType<?>> predicate) {
            this.allowsSpawningPredicate = predicate;
            return this;
        }

        public Settings solidBlock(ContextPredicate predicate) {
            this.solidBlockPredicate = predicate;
            return this;
        }

        public Settings suffocates(ContextPredicate predicate) {
            this.suffocationPredicate = predicate;
            return this;
        }

        public Settings blockVision(ContextPredicate predicate) {
            this.blockVisionPredicate = predicate;
            return this;
        }

        public Settings postProcess(ContextPredicate predicate) {
            this.postProcessPredicate = predicate;
            return this;
        }

        public Settings emissiveLighting(ContextPredicate predicate) {
            this.emissiveLightingPredicate = predicate;
            return this;
        }

        public Settings requiresTool() {
            this.toolRequired = true;
            return this;
        }

        public Settings mapColor(MapColor color) {
            this.mapColorProvider = state -> color;
            return this;
        }

        public Settings hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Settings resistance(float resistance) {
            this.resistance = Math.max(0.0f, resistance);
            return this;
        }

        public Settings offset(OffsetType offsetType) {
            switch (offsetType) {
                default: {
                    this.offsetter = Optional.empty();
                    break;
                }
                case XYZ: {
                    this.offsetter = Optional.of((state, world, pos) -> {
                        Block lv = state.getBlock();
                        long l = MathHelper.hashCode(pos.getX(), 0, pos.getZ());
                        double d = ((double)((float)(l >> 4 & 0xFL) / 15.0f) - 1.0) * (double)lv.getVerticalModelOffsetMultiplier();
                        float f = lv.getMaxHorizontalModelOffset();
                        double e = MathHelper.clamp(((double)((float)(l & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                        double g = MathHelper.clamp(((double)((float)(l >> 8 & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                        return new Vec3d(e, d, g);
                    });
                    break;
                }
                case XZ: {
                    this.offsetter = Optional.of((state, world, pos) -> {
                        Block lv = state.getBlock();
                        long l = MathHelper.hashCode(pos.getX(), 0, pos.getZ());
                        float f = lv.getMaxHorizontalModelOffset();
                        double d = MathHelper.clamp(((double)((float)(l & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                        double e = MathHelper.clamp(((double)((float)(l >> 8 & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                        return new Vec3d(d, 0.0, e);
                    });
                }
            }
            return this;
        }

        public Settings noBlockBreakParticles() {
            this.blockBreakParticles = false;
            return this;
        }

        public Settings requires(FeatureFlag ... features) {
            this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(features);
            return this;
        }
    }

    public static interface TypedContextPredicate<A> {
        public boolean test(BlockState var1, BlockView var2, BlockPos var3, A var4);
    }

    public static interface Offsetter {
        public Vec3d evaluate(BlockState var1, BlockView var2, BlockPos var3);
    }

    public static interface ContextPredicate {
        public boolean test(BlockState var1, BlockView var2, BlockPos var3);
    }

    public static abstract class AbstractBlockState
    extends State<Block, BlockState> {
        private final int luminance;
        private final boolean hasSidedTransparency;
        private final boolean isAir;
        private final Material material;
        private final MapColor mapColor;
        private final float hardness;
        private final boolean toolRequired;
        private final boolean opaque;
        private final ContextPredicate solidBlockPredicate;
        private final ContextPredicate suffocationPredicate;
        private final ContextPredicate blockVisionPredicate;
        private final ContextPredicate postProcessPredicate;
        private final ContextPredicate emissiveLightingPredicate;
        private final Optional<Offsetter> offsetter;
        private final boolean blockBreakParticles;
        @Nullable
        protected ShapeCache shapeCache;
        private FluidState fluidState = Fluids.EMPTY.getDefaultState();
        private boolean ticksRandomly;

        protected AbstractBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
            super(block, propertyMap, codec);
            Settings lv = block.settings;
            this.luminance = lv.luminance.applyAsInt(this.asBlockState());
            this.hasSidedTransparency = block.hasSidedTransparency(this.asBlockState());
            this.isAir = lv.isAir;
            this.material = lv.material;
            this.mapColor = lv.mapColorProvider.apply(this.asBlockState());
            this.hardness = lv.hardness;
            this.toolRequired = lv.toolRequired;
            this.opaque = lv.opaque;
            this.solidBlockPredicate = lv.solidBlockPredicate;
            this.suffocationPredicate = lv.suffocationPredicate;
            this.blockVisionPredicate = lv.blockVisionPredicate;
            this.postProcessPredicate = lv.postProcessPredicate;
            this.emissiveLightingPredicate = lv.emissiveLightingPredicate;
            this.offsetter = lv.offsetter;
            this.blockBreakParticles = lv.blockBreakParticles;
        }

        public void initShapeCache() {
            this.fluidState = ((Block)this.owner).getFluidState(this.asBlockState());
            this.ticksRandomly = ((Block)this.owner).hasRandomTicks(this.asBlockState());
            if (!this.getBlock().hasDynamicBounds()) {
                this.shapeCache = new ShapeCache(this.asBlockState());
            }
        }

        public Block getBlock() {
            return (Block)this.owner;
        }

        public RegistryEntry<Block> getRegistryEntry() {
            return ((Block)this.owner).getRegistryEntry();
        }

        public Material getMaterial() {
            return this.material;
        }

        public boolean allowsSpawning(BlockView world, BlockPos pos, EntityType<?> type) {
            return this.getBlock().settings.allowsSpawningPredicate.test(this.asBlockState(), world, pos, type);
        }

        public boolean isTransparent(BlockView world, BlockPos pos) {
            if (this.shapeCache != null) {
                return this.shapeCache.transparent;
            }
            return this.getBlock().isTransparent(this.asBlockState(), world, pos);
        }

        public int getOpacity(BlockView world, BlockPos pos) {
            if (this.shapeCache != null) {
                return this.shapeCache.lightSubtracted;
            }
            return this.getBlock().getOpacity(this.asBlockState(), world, pos);
        }

        public VoxelShape getCullingFace(BlockView world, BlockPos pos, Direction direction) {
            if (this.shapeCache != null && this.shapeCache.extrudedFaces != null) {
                return this.shapeCache.extrudedFaces[direction.ordinal()];
            }
            return VoxelShapes.extrudeFace(this.getCullingShape(world, pos), direction);
        }

        public VoxelShape getCullingShape(BlockView world, BlockPos pos) {
            return this.getBlock().getCullingShape(this.asBlockState(), world, pos);
        }

        public boolean exceedsCube() {
            return this.shapeCache == null || this.shapeCache.exceedsCube;
        }

        public boolean hasSidedTransparency() {
            return this.hasSidedTransparency;
        }

        public int getLuminance() {
            return this.luminance;
        }

        public boolean isAir() {
            return this.isAir;
        }

        public MapColor getMapColor(BlockView world, BlockPos pos) {
            return this.mapColor;
        }

        public BlockState rotate(BlockRotation rotation) {
            return this.getBlock().rotate(this.asBlockState(), rotation);
        }

        public BlockState mirror(BlockMirror mirror) {
            return this.getBlock().mirror(this.asBlockState(), mirror);
        }

        public BlockRenderType getRenderType() {
            return this.getBlock().getRenderType(this.asBlockState());
        }

        public boolean hasEmissiveLighting(BlockView world, BlockPos pos) {
            return this.emissiveLightingPredicate.test(this.asBlockState(), world, pos);
        }

        public float getAmbientOcclusionLightLevel(BlockView world, BlockPos pos) {
            return this.getBlock().getAmbientOcclusionLightLevel(this.asBlockState(), world, pos);
        }

        public boolean isSolidBlock(BlockView world, BlockPos pos) {
            return this.solidBlockPredicate.test(this.asBlockState(), world, pos);
        }

        public boolean emitsRedstonePower() {
            return this.getBlock().emitsRedstonePower(this.asBlockState());
        }

        public int getWeakRedstonePower(BlockView world, BlockPos pos, Direction direction) {
            return this.getBlock().getWeakRedstonePower(this.asBlockState(), world, pos, direction);
        }

        public boolean hasComparatorOutput() {
            return this.getBlock().hasComparatorOutput(this.asBlockState());
        }

        public int getComparatorOutput(World world, BlockPos pos) {
            return this.getBlock().getComparatorOutput(this.asBlockState(), world, pos);
        }

        public float getHardness(BlockView world, BlockPos pos) {
            return this.hardness;
        }

        public float calcBlockBreakingDelta(PlayerEntity player, BlockView world, BlockPos pos) {
            return this.getBlock().calcBlockBreakingDelta(this.asBlockState(), player, world, pos);
        }

        public int getStrongRedstonePower(BlockView world, BlockPos pos, Direction direction) {
            return this.getBlock().getStrongRedstonePower(this.asBlockState(), world, pos, direction);
        }

        public PistonBehavior getPistonBehavior() {
            return this.getBlock().getPistonBehavior(this.asBlockState());
        }

        public boolean isOpaqueFullCube(BlockView world, BlockPos pos) {
            if (this.shapeCache != null) {
                return this.shapeCache.fullOpaque;
            }
            BlockState lv = this.asBlockState();
            if (lv.isOpaque()) {
                return Block.isShapeFullCube(lv.getCullingShape(world, pos));
            }
            return false;
        }

        public boolean isOpaque() {
            return this.opaque;
        }

        public boolean isSideInvisible(BlockState state, Direction direction) {
            return this.getBlock().isSideInvisible(this.asBlockState(), state, direction);
        }

        public VoxelShape getOutlineShape(BlockView world, BlockPos pos) {
            return this.getOutlineShape(world, pos, ShapeContext.absent());
        }

        public VoxelShape getOutlineShape(BlockView world, BlockPos pos, ShapeContext context) {
            return this.getBlock().getOutlineShape(this.asBlockState(), world, pos, context);
        }

        public VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
            if (this.shapeCache != null) {
                return this.shapeCache.collisionShape;
            }
            return this.getCollisionShape(world, pos, ShapeContext.absent());
        }

        public VoxelShape getCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
            return this.getBlock().getCollisionShape(this.asBlockState(), world, pos, context);
        }

        public VoxelShape getSidesShape(BlockView world, BlockPos pos) {
            return this.getBlock().getSidesShape(this.asBlockState(), world, pos);
        }

        public VoxelShape getCameraCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
            return this.getBlock().getCameraCollisionShape(this.asBlockState(), world, pos, context);
        }

        public VoxelShape getRaycastShape(BlockView world, BlockPos pos) {
            return this.getBlock().getRaycastShape(this.asBlockState(), world, pos);
        }

        public final boolean hasSolidTopSurface(BlockView world, BlockPos pos, Entity entity) {
            return this.isSolidSurface(world, pos, entity, Direction.UP);
        }

        public final boolean isSolidSurface(BlockView world, BlockPos pos, Entity entity, Direction direction) {
            return Block.isFaceFullSquare(this.getCollisionShape(world, pos, ShapeContext.of(entity)), direction);
        }

        public Vec3d getModelOffset(BlockView world, BlockPos pos) {
            return this.offsetter.map(offsetter -> offsetter.evaluate(this.asBlockState(), world, pos)).orElse(Vec3d.ZERO);
        }

        public boolean hasModelOffset() {
            return !this.offsetter.isEmpty();
        }

        public boolean onSyncedBlockEvent(World world, BlockPos pos, int type, int data) {
            return this.getBlock().onSyncedBlockEvent(this.asBlockState(), world, pos, type, data);
        }

        @Deprecated
        public void neighborUpdate(World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
            this.getBlock().neighborUpdate(this.asBlockState(), world, pos, sourceBlock, sourcePos, notify);
        }

        public final void updateNeighbors(WorldAccess world, BlockPos pos, int flags) {
            this.updateNeighbors(world, pos, flags, 512);
        }

        public final void updateNeighbors(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
            this.getBlock();
            BlockPos.Mutable lv = new BlockPos.Mutable();
            for (Direction lv2 : DIRECTIONS) {
                lv.set((Vec3i)pos, lv2);
                world.replaceWithStateForNeighborUpdate(lv2.getOpposite(), this.asBlockState(), lv, pos, flags, maxUpdateDepth);
            }
        }

        public final void prepare(WorldAccess world, BlockPos pos, int flags) {
            this.prepare(world, pos, flags, 512);
        }

        public void prepare(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
            this.getBlock().prepare(this.asBlockState(), world, pos, flags, maxUpdateDepth);
        }

        public void onBlockAdded(World world, BlockPos pos, BlockState state, boolean notify) {
            this.getBlock().onBlockAdded(this.asBlockState(), world, pos, state, notify);
        }

        public void onStateReplaced(World world, BlockPos pos, BlockState state, boolean moved) {
            this.getBlock().onStateReplaced(this.asBlockState(), world, pos, state, moved);
        }

        public void scheduledTick(ServerWorld world, BlockPos pos, Random random) {
            this.getBlock().scheduledTick(this.asBlockState(), world, pos, random);
        }

        public void randomTick(ServerWorld world, BlockPos pos, Random random) {
            this.getBlock().randomTick(this.asBlockState(), world, pos, random);
        }

        public void onEntityCollision(World world, BlockPos pos, Entity entity) {
            this.getBlock().onEntityCollision(this.asBlockState(), world, pos, entity);
        }

        public void onStacksDropped(ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
            this.getBlock().onStacksDropped(this.asBlockState(), world, pos, tool, dropExperience);
        }

        public List<ItemStack> getDroppedStacks(LootContext.Builder builder) {
            return this.getBlock().getDroppedStacks(this.asBlockState(), builder);
        }

        public ActionResult onUse(World world, PlayerEntity player, Hand hand, BlockHitResult hit) {
            return this.getBlock().onUse(this.asBlockState(), world, hit.getBlockPos(), player, hand, hit);
        }

        public void onBlockBreakStart(World world, BlockPos pos, PlayerEntity player) {
            this.getBlock().onBlockBreakStart(this.asBlockState(), world, pos, player);
        }

        public boolean shouldSuffocate(BlockView world, BlockPos pos) {
            return this.suffocationPredicate.test(this.asBlockState(), world, pos);
        }

        public boolean shouldBlockVision(BlockView world, BlockPos pos) {
            return this.blockVisionPredicate.test(this.asBlockState(), world, pos);
        }

        public BlockState getStateForNeighborUpdate(Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
            return this.getBlock().getStateForNeighborUpdate(this.asBlockState(), direction, neighborState, world, pos, neighborPos);
        }

        public boolean canPathfindThrough(BlockView world, BlockPos pos, NavigationType type) {
            return this.getBlock().canPathfindThrough(this.asBlockState(), world, pos, type);
        }

        public boolean canReplace(ItemPlacementContext context) {
            return this.getBlock().canReplace(this.asBlockState(), context);
        }

        public boolean canBucketPlace(Fluid fluid) {
            return this.getBlock().canBucketPlace(this.asBlockState(), fluid);
        }

        public boolean isReplaceable() {
            return this.getMaterial().isReplaceable();
        }

        public boolean canPlaceAt(WorldView world, BlockPos pos) {
            return this.getBlock().canPlaceAt(this.asBlockState(), world, pos);
        }

        public boolean shouldPostProcess(BlockView world, BlockPos pos) {
            return this.postProcessPredicate.test(this.asBlockState(), world, pos);
        }

        @Nullable
        public NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
            return this.getBlock().createScreenHandlerFactory(this.asBlockState(), world, pos);
        }

        public boolean isIn(TagKey<Block> tag) {
            return this.getBlock().getRegistryEntry().isIn(tag);
        }

        public boolean isIn(TagKey<Block> tag, Predicate<AbstractBlockState> predicate) {
            return this.isIn(tag) && predicate.test(this);
        }

        public boolean isIn(RegistryEntryList<Block> blocks) {
            return blocks.contains(this.getBlock().getRegistryEntry());
        }

        public Stream<TagKey<Block>> streamTags() {
            return this.getBlock().getRegistryEntry().streamTags();
        }

        public boolean hasBlockEntity() {
            return this.getBlock() instanceof BlockEntityProvider;
        }

        @Nullable
        public <T extends BlockEntity> BlockEntityTicker<T> getBlockEntityTicker(World world, BlockEntityType<T> blockEntityType) {
            if (this.getBlock() instanceof BlockEntityProvider) {
                return ((BlockEntityProvider)((Object)this.getBlock())).getTicker(world, this.asBlockState(), blockEntityType);
            }
            return null;
        }

        public boolean isOf(Block block) {
            return this.getBlock() == block;
        }

        public FluidState getFluidState() {
            return this.fluidState;
        }

        public boolean hasRandomTicks() {
            return this.ticksRandomly;
        }

        public long getRenderingSeed(BlockPos pos) {
            return this.getBlock().getRenderingSeed(this.asBlockState(), pos);
        }

        public BlockSoundGroup getSoundGroup() {
            return this.getBlock().getSoundGroup(this.asBlockState());
        }

        public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
            this.getBlock().onProjectileHit(world, state, hit, projectile);
        }

        public boolean isSideSolidFullSquare(BlockView world, BlockPos pos, Direction direction) {
            return this.isSideSolid(world, pos, direction, SideShapeType.FULL);
        }

        public boolean isSideSolid(BlockView world, BlockPos pos, Direction direction, SideShapeType shapeType) {
            if (this.shapeCache != null) {
                return this.shapeCache.isSideSolid(direction, shapeType);
            }
            return shapeType.matches(this.asBlockState(), world, pos, direction);
        }

        public boolean isFullCube(BlockView world, BlockPos pos) {
            if (this.shapeCache != null) {
                return this.shapeCache.isFullCube;
            }
            return this.getBlock().isShapeFullCube(this.asBlockState(), world, pos);
        }

        protected abstract BlockState asBlockState();

        public boolean isToolRequired() {
            return this.toolRequired;
        }

        public boolean hasBlockBreakParticles() {
            return this.blockBreakParticles;
        }

        static final class ShapeCache {
            private static final Direction[] DIRECTIONS = Direction.values();
            private static final int SHAPE_TYPE_LENGTH = SideShapeType.values().length;
            protected final boolean fullOpaque;
            final boolean transparent;
            final int lightSubtracted;
            @Nullable
            final VoxelShape[] extrudedFaces;
            protected final VoxelShape collisionShape;
            protected final boolean exceedsCube;
            private final boolean[] solidSides;
            protected final boolean isFullCube;

            ShapeCache(BlockState state) {
                Block lv = state.getBlock();
                this.fullOpaque = state.isOpaqueFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                this.transparent = lv.isTransparent(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                this.lightSubtracted = lv.getOpacity(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                if (!state.isOpaque()) {
                    this.extrudedFaces = null;
                } else {
                    this.extrudedFaces = new VoxelShape[DIRECTIONS.length];
                    VoxelShape lv2 = lv.getCullingShape(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                    Direction[] directionArray = DIRECTIONS;
                    int n = directionArray.length;
                    for (int i = 0; i < n; ++i) {
                        Direction lv3 = directionArray[i];
                        this.extrudedFaces[lv3.ordinal()] = VoxelShapes.extrudeFace(lv2, lv3);
                    }
                }
                this.collisionShape = lv.getCollisionShape(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent());
                if (!this.collisionShape.isEmpty() && state.hasModelOffset()) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", Registries.BLOCK.getId(lv)));
                }
                this.exceedsCube = Arrays.stream(Direction.Axis.values()).anyMatch(axis -> this.collisionShape.getMin((Direction.Axis)axis) < 0.0 || this.collisionShape.getMax((Direction.Axis)axis) > 1.0);
                this.solidSides = new boolean[DIRECTIONS.length * SHAPE_TYPE_LENGTH];
                for (Direction lv4 : DIRECTIONS) {
                    for (SideShapeType lv5 : SideShapeType.values()) {
                        this.solidSides[ShapeCache.indexSolidSide((Direction)lv4, (SideShapeType)lv5)] = lv5.matches(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN, lv4);
                    }
                }
                this.isFullCube = Block.isShapeFullCube(state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN));
            }

            public boolean isSideSolid(Direction direction, SideShapeType shapeType) {
                return this.solidSides[ShapeCache.indexSolidSide(direction, shapeType)];
            }

            private static int indexSolidSide(Direction direction, SideShapeType shapeType) {
                return direction.ordinal() * SHAPE_TYPE_LENGTH + shapeType.ordinal();
            }
        }
    }

    public static enum OffsetType {
        NONE,
        XZ,
        XYZ;

    }
}

