/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LandingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.Thickness;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PointedDripstoneBlock
extends Block
implements LandingBlock,
Waterloggable {
    public static final DirectionProperty VERTICAL_DIRECTION = Properties.VERTICAL_DIRECTION;
    public static final EnumProperty<Thickness> THICKNESS = Properties.THICKNESS;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final int field_31205 = 11;
    private static final int field_31207 = 2;
    private static final float field_31208 = 0.02f;
    private static final float field_31209 = 0.12f;
    private static final int field_31210 = 11;
    private static final float WATER_DRIP_CHANCE = 0.17578125f;
    private static final float LAVA_DRIP_CHANCE = 0.05859375f;
    private static final double field_31213 = 0.6;
    private static final float field_31214 = 1.0f;
    private static final int field_31215 = 40;
    private static final int field_31200 = 6;
    private static final float field_31201 = 2.0f;
    private static final int field_31202 = 2;
    private static final float field_33566 = 5.0f;
    private static final float field_33567 = 0.011377778f;
    private static final int MAX_STALACTITE_GROWTH = 7;
    private static final int STALACTITE_FLOOR_SEARCH_RANGE = 10;
    private static final float field_31203 = 0.6875f;
    private static final VoxelShape TIP_MERGE_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape UP_TIP_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape DOWN_TIP_SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape BASE_SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final float field_31204 = 0.125f;
    private static final VoxelShape DRIP_COLLISION_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public PointedDripstoneBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(VERTICAL_DIRECTION, Direction.UP)).with(THICKNESS, Thickness.TIP)).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION, THICKNESS, WATERLOGGED);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return PointedDripstoneBlock.canPlaceAtWithDirection(world, pos, state.get(VERTICAL_DIRECTION));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return state;
        }
        Direction lv = state.get(VERTICAL_DIRECTION);
        if (lv == Direction.DOWN && world.getBlockTickScheduler().isQueued(pos, this)) {
            return state;
        }
        if (direction == lv.getOpposite() && !this.canPlaceAt(state, world, pos)) {
            if (lv == Direction.DOWN) {
                world.scheduleBlockTick(pos, this, 2);
            } else {
                world.scheduleBlockTick(pos, this, 1);
            }
            return state;
        }
        boolean bl = state.get(THICKNESS) == Thickness.TIP_MERGE;
        Thickness lv2 = PointedDripstoneBlock.getThickness(world, pos, lv, bl);
        return (BlockState)state.with(THICKNESS, lv2);
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos lv = hit.getBlockPos();
        if (!world.isClient && projectile.canModifyAt(world, lv) && projectile instanceof TridentEntity && projectile.getVelocity().length() > 0.6) {
            world.breakBlock(lv, true);
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.get(VERTICAL_DIRECTION) == Direction.UP && state.get(THICKNESS) == Thickness.TIP) {
            entity.handleFallDamage(fallDistance + 2.0f, 2.0f, world.getDamageSources().stalagmite());
        } else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!PointedDripstoneBlock.canDrip(state)) {
            return;
        }
        float f = random.nextFloat();
        if (f > 0.12f) {
            return;
        }
        PointedDripstoneBlock.getFluid(world, pos, state).filter(fluid -> f < 0.02f || PointedDripstoneBlock.isFluidLiquid(fluid.fluid)).ifPresent(fluid -> PointedDripstoneBlock.createParticle(world, pos, state, fluid.fluid));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (PointedDripstoneBlock.isPointingUp(state) && !this.canPlaceAt(state, world, pos)) {
            world.breakBlock(pos, true);
        } else {
            PointedDripstoneBlock.spawnFallingBlock(state, world, pos);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        PointedDripstoneBlock.dripTick(state, world, pos, random.nextFloat());
        if (random.nextFloat() < 0.011377778f && PointedDripstoneBlock.isHeldByPointedDripstone(state, world, pos)) {
            PointedDripstoneBlock.tryGrow(state, world, pos, random);
        }
    }

    @VisibleForTesting
    public static void dripTick(BlockState state, ServerWorld world, BlockPos pos, float dripChance) {
        float g;
        if (dripChance > 0.17578125f && dripChance > 0.05859375f) {
            return;
        }
        if (!PointedDripstoneBlock.isHeldByPointedDripstone(state, world, pos)) {
            return;
        }
        Optional<DrippingFluid> optional = PointedDripstoneBlock.getFluid(world, pos, state);
        if (optional.isEmpty()) {
            return;
        }
        Fluid lv = optional.get().fluid;
        if (lv == Fluids.WATER) {
            g = 0.17578125f;
        } else if (lv == Fluids.LAVA) {
            g = 0.05859375f;
        } else {
            return;
        }
        if (dripChance >= g) {
            return;
        }
        BlockPos lv2 = PointedDripstoneBlock.getTipPos(state, world, pos, 11, false);
        if (lv2 == null) {
            return;
        }
        if (optional.get().sourceState.isOf(Blocks.MUD) && lv == Fluids.WATER) {
            BlockState lv3 = Blocks.CLAY.getDefaultState();
            world.setBlockState(optional.get().pos, lv3);
            Block.pushEntitiesUpBeforeBlockChange(optional.get().sourceState, lv3, world, optional.get().pos);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, optional.get().pos, GameEvent.Emitter.of(lv3));
            world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS, lv2, 0);
            return;
        }
        BlockPos lv4 = PointedDripstoneBlock.getCauldronPos(world, lv2, lv);
        if (lv4 == null) {
            return;
        }
        world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS, lv2, 0);
        int i = lv2.getY() - lv4.getY();
        int j = 50 + i;
        BlockState lv5 = world.getBlockState(lv4);
        world.scheduleBlockTick(lv4, lv5.getBlock(), j);
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv3;
        BlockPos lv2;
        World lv = ctx.getWorld();
        Direction lv4 = PointedDripstoneBlock.getDirectionToPlaceAt(lv, lv2 = ctx.getBlockPos(), lv3 = ctx.getVerticalPlayerLookDirection().getOpposite());
        if (lv4 == null) {
            return null;
        }
        boolean bl = !ctx.shouldCancelInteraction();
        Thickness lv5 = PointedDripstoneBlock.getThickness(lv, lv2, lv4, bl);
        if (lv5 == null) {
            return null;
        }
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(VERTICAL_DIRECTION, lv4)).with(THICKNESS, lv5)).with(WATERLOGGED, lv.getFluidState(lv2).getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Thickness lv = state.get(THICKNESS);
        VoxelShape lv2 = lv == Thickness.TIP_MERGE ? TIP_MERGE_SHAPE : (lv == Thickness.TIP ? (state.get(VERTICAL_DIRECTION) == Direction.DOWN ? DOWN_TIP_SHAPE : UP_TIP_SHAPE) : (lv == Thickness.FRUSTUM ? BASE_SHAPE : (lv == Thickness.MIDDLE ? FRUSTUM_SHAPE : MIDDLE_SHAPE)));
        Vec3d lv3 = state.getModelOffset(world, pos);
        return lv2.offset(lv3.x, 0.0, lv3.z);
    }

    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public float getMaxHorizontalModelOffset() {
        return 0.125f;
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        if (!fallingBlockEntity.isSilent()) {
            world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_LANDS, pos, 0);
        }
    }

    @Override
    public DamageSource getDamageSource(Entity attacker) {
        return attacker.getDamageSources().fallingStalactite(attacker);
    }

    @Override
    public Predicate<Entity> getEntityPredicate() {
        return EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_LIVING_ENTITY);
    }

    private static void spawnFallingBlock(BlockState state, ServerWorld world, BlockPos pos) {
        BlockPos.Mutable lv = pos.mutableCopy();
        BlockState lv2 = state;
        while (PointedDripstoneBlock.isPointingDown(lv2)) {
            FallingBlockEntity lv3 = FallingBlockEntity.spawnFromBlock(world, lv, lv2);
            if (PointedDripstoneBlock.isTip(lv2, true)) {
                int i = Math.max(1 + pos.getY() - lv.getY(), 6);
                float f = 1.0f * (float)i;
                lv3.setHurtEntities(f, 40);
                break;
            }
            lv.move(Direction.DOWN);
            lv2 = world.getBlockState(lv);
        }
    }

    @VisibleForTesting
    public static void tryGrow(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState lv2;
        BlockState lv = world.getBlockState(pos.up(1));
        if (!PointedDripstoneBlock.canGrow(lv, lv2 = world.getBlockState(pos.up(2)))) {
            return;
        }
        BlockPos lv3 = PointedDripstoneBlock.getTipPos(state, world, pos, 7, false);
        if (lv3 == null) {
            return;
        }
        BlockState lv4 = world.getBlockState(lv3);
        if (!PointedDripstoneBlock.canDrip(lv4) || !PointedDripstoneBlock.canGrow(lv4, world, lv3)) {
            return;
        }
        if (random.nextBoolean()) {
            PointedDripstoneBlock.tryGrow(world, lv3, Direction.DOWN);
        } else {
            PointedDripstoneBlock.tryGrowStalagmite(world, lv3);
        }
    }

    private static void tryGrowStalagmite(ServerWorld world, BlockPos pos) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int i = 0; i < 10; ++i) {
            lv.move(Direction.DOWN);
            BlockState lv2 = world.getBlockState(lv);
            if (!lv2.getFluidState().isEmpty()) {
                return;
            }
            if (PointedDripstoneBlock.isTip(lv2, Direction.UP) && PointedDripstoneBlock.canGrow(lv2, world, lv)) {
                PointedDripstoneBlock.tryGrow(world, lv, Direction.UP);
                return;
            }
            if (PointedDripstoneBlock.canPlaceAtWithDirection(world, lv, Direction.UP) && !world.isWater((BlockPos)lv.down())) {
                PointedDripstoneBlock.tryGrow(world, (BlockPos)lv.down(), Direction.UP);
                return;
            }
            if (PointedDripstoneBlock.canDripThrough(world, lv, lv2)) continue;
            return;
        }
    }

    private static void tryGrow(ServerWorld world, BlockPos pos, Direction direction) {
        BlockPos lv = pos.offset(direction);
        BlockState lv2 = world.getBlockState(lv);
        if (PointedDripstoneBlock.isTip(lv2, direction.getOpposite())) {
            PointedDripstoneBlock.growMerged(lv2, world, lv);
        } else if (lv2.isAir() || lv2.isOf(Blocks.WATER)) {
            PointedDripstoneBlock.place(world, lv, direction, Thickness.TIP);
        }
    }

    private static void place(WorldAccess world, BlockPos pos, Direction direction, Thickness thickness) {
        BlockState lv = (BlockState)((BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.getDefaultState().with(VERTICAL_DIRECTION, direction)).with(THICKNESS, thickness)).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
        world.setBlockState(pos, lv, Block.NOTIFY_ALL);
    }

    private static void growMerged(BlockState state, WorldAccess world, BlockPos pos) {
        BlockPos lv2;
        BlockPos lv;
        if (state.get(VERTICAL_DIRECTION) == Direction.UP) {
            lv = pos;
            lv2 = pos.up();
        } else {
            lv2 = pos;
            lv = pos.down();
        }
        PointedDripstoneBlock.place(world, lv2, Direction.DOWN, Thickness.TIP_MERGE);
        PointedDripstoneBlock.place(world, lv, Direction.UP, Thickness.TIP_MERGE);
    }

    public static void createParticle(World world, BlockPos pos, BlockState state) {
        PointedDripstoneBlock.getFluid(world, pos, state).ifPresent(fluid -> PointedDripstoneBlock.createParticle(world, pos, state, fluid.fluid));
    }

    private static void createParticle(World world, BlockPos pos, BlockState state, Fluid fluid) {
        Vec3d lv = state.getModelOffset(world, pos);
        double d = 0.0625;
        double e = (double)pos.getX() + 0.5 + lv.x;
        double f = (double)((float)(pos.getY() + 1) - 0.6875f) - 0.0625;
        double g = (double)pos.getZ() + 0.5 + lv.z;
        Fluid lv2 = PointedDripstoneBlock.getDripFluid(world, fluid);
        DefaultParticleType lv3 = lv2.isIn(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
        world.addParticle(lv3, e, f, g, 0.0, 0.0, 0.0);
    }

    @Nullable
    private static BlockPos getTipPos(BlockState state2, WorldAccess world, BlockPos pos2, int range, boolean allowMerged) {
        if (PointedDripstoneBlock.isTip(state2, allowMerged)) {
            return pos2;
        }
        Direction lv = state2.get(VERTICAL_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (pos, state) -> state.isOf(Blocks.POINTED_DRIPSTONE) && state.get(VERTICAL_DIRECTION) == lv;
        return PointedDripstoneBlock.searchInDirection(world, pos2, lv.getDirection(), biPredicate, state -> PointedDripstoneBlock.isTip(state, allowMerged), range).orElse(null);
    }

    @Nullable
    private static Direction getDirectionToPlaceAt(WorldView world, BlockPos pos, Direction direction) {
        Direction lv;
        if (PointedDripstoneBlock.canPlaceAtWithDirection(world, pos, direction)) {
            lv = direction;
        } else if (PointedDripstoneBlock.canPlaceAtWithDirection(world, pos, direction.getOpposite())) {
            lv = direction.getOpposite();
        } else {
            return null;
        }
        return lv;
    }

    private static Thickness getThickness(WorldView world, BlockPos pos, Direction direction, boolean tryMerge) {
        Direction lv = direction.getOpposite();
        BlockState lv2 = world.getBlockState(pos.offset(direction));
        if (PointedDripstoneBlock.isPointedDripstoneFacingDirection(lv2, lv)) {
            if (tryMerge || lv2.get(THICKNESS) == Thickness.TIP_MERGE) {
                return Thickness.TIP_MERGE;
            }
            return Thickness.TIP;
        }
        if (!PointedDripstoneBlock.isPointedDripstoneFacingDirection(lv2, direction)) {
            return Thickness.TIP;
        }
        Thickness lv3 = lv2.get(THICKNESS);
        if (lv3 == Thickness.TIP || lv3 == Thickness.TIP_MERGE) {
            return Thickness.FRUSTUM;
        }
        BlockState lv4 = world.getBlockState(pos.offset(lv));
        if (!PointedDripstoneBlock.isPointedDripstoneFacingDirection(lv4, direction)) {
            return Thickness.BASE;
        }
        return Thickness.MIDDLE;
    }

    public static boolean canDrip(BlockState state) {
        return PointedDripstoneBlock.isPointingDown(state) && state.get(THICKNESS) == Thickness.TIP && state.get(WATERLOGGED) == false;
    }

    private static boolean canGrow(BlockState state, ServerWorld world, BlockPos pos) {
        Direction lv = state.get(VERTICAL_DIRECTION);
        BlockPos lv2 = pos.offset(lv);
        BlockState lv3 = world.getBlockState(lv2);
        if (!lv3.getFluidState().isEmpty()) {
            return false;
        }
        if (lv3.isAir()) {
            return true;
        }
        return PointedDripstoneBlock.isTip(lv3, lv.getOpposite());
    }

    private static Optional<BlockPos> getSupportingPos(World world, BlockPos pos2, BlockState state2, int range) {
        Direction lv = state2.get(VERTICAL_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (pos, state) -> state.isOf(Blocks.POINTED_DRIPSTONE) && state.get(VERTICAL_DIRECTION) == lv;
        return PointedDripstoneBlock.searchInDirection(world, pos2, lv.getOpposite().getDirection(), biPredicate, state -> !state.isOf(Blocks.POINTED_DRIPSTONE), range);
    }

    private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos, Direction direction) {
        BlockPos lv = pos.offset(direction.getOpposite());
        BlockState lv2 = world.getBlockState(lv);
        return lv2.isSideSolidFullSquare(world, lv, direction) || PointedDripstoneBlock.isPointedDripstoneFacingDirection(lv2, direction);
    }

    private static boolean isTip(BlockState state, boolean allowMerged) {
        if (!state.isOf(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        Thickness lv = state.get(THICKNESS);
        return lv == Thickness.TIP || allowMerged && lv == Thickness.TIP_MERGE;
    }

    private static boolean isTip(BlockState state, Direction direction) {
        return PointedDripstoneBlock.isTip(state, false) && state.get(VERTICAL_DIRECTION) == direction;
    }

    private static boolean isPointingDown(BlockState state) {
        return PointedDripstoneBlock.isPointedDripstoneFacingDirection(state, Direction.DOWN);
    }

    private static boolean isPointingUp(BlockState state) {
        return PointedDripstoneBlock.isPointedDripstoneFacingDirection(state, Direction.UP);
    }

    private static boolean isHeldByPointedDripstone(BlockState state, WorldView world, BlockPos pos) {
        return PointedDripstoneBlock.isPointingDown(state) && !world.getBlockState(pos.up()).isOf(Blocks.POINTED_DRIPSTONE);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    private static boolean isPointedDripstoneFacingDirection(BlockState state, Direction direction) {
        return state.isOf(Blocks.POINTED_DRIPSTONE) && state.get(VERTICAL_DIRECTION) == direction;
    }

    @Nullable
    private static BlockPos getCauldronPos(World world, BlockPos pos2, Fluid fluid) {
        Predicate<BlockState> predicate = state -> state.getBlock() instanceof AbstractCauldronBlock && ((AbstractCauldronBlock)state.getBlock()).canBeFilledByDripstone(fluid);
        BiPredicate<BlockPos, BlockState> biPredicate = (pos, state) -> PointedDripstoneBlock.canDripThrough(world, pos, state);
        return PointedDripstoneBlock.searchInDirection(world, pos2, Direction.DOWN.getDirection(), biPredicate, predicate, 11).orElse(null);
    }

    @Nullable
    public static BlockPos getDripPos(World world, BlockPos pos2) {
        BiPredicate<BlockPos, BlockState> biPredicate = (pos, state) -> PointedDripstoneBlock.canDripThrough(world, pos, state);
        return PointedDripstoneBlock.searchInDirection(world, pos2, Direction.UP.getDirection(), biPredicate, PointedDripstoneBlock::canDrip, 11).orElse(null);
    }

    public static Fluid getDripFluid(ServerWorld world, BlockPos pos) {
        return PointedDripstoneBlock.getFluid(world, pos, world.getBlockState(pos)).map(fluid -> fluid.fluid).filter(PointedDripstoneBlock::isFluidLiquid).orElse(Fluids.EMPTY);
    }

    private static Optional<DrippingFluid> getFluid(World world, BlockPos pos2, BlockState state) {
        if (!PointedDripstoneBlock.isPointingDown(state)) {
            return Optional.empty();
        }
        return PointedDripstoneBlock.getSupportingPos(world, pos2, state, 11).map(pos -> {
            BlockPos lv = pos.up();
            BlockState lv2 = world.getBlockState(lv);
            Fluid lv3 = lv2.isOf(Blocks.MUD) && !world.getDimension().ultrawarm() ? Fluids.WATER : world.getFluidState(lv).getFluid();
            return new DrippingFluid(lv, lv3, lv2);
        });
    }

    private static boolean isFluidLiquid(Fluid fluid) {
        return fluid == Fluids.LAVA || fluid == Fluids.WATER;
    }

    private static boolean canGrow(BlockState dripstoneBlockState, BlockState waterState) {
        return dripstoneBlockState.isOf(Blocks.DRIPSTONE_BLOCK) && waterState.isOf(Blocks.WATER) && waterState.getFluidState().isStill();
    }

    private static Fluid getDripFluid(World world, Fluid fluid) {
        if (fluid.matchesType(Fluids.EMPTY)) {
            return world.getDimension().ultrawarm() ? Fluids.LAVA : Fluids.WATER;
        }
        return fluid;
    }

    private static Optional<BlockPos> searchInDirection(WorldAccess world, BlockPos pos, Direction.AxisDirection direction, BiPredicate<BlockPos, BlockState> continuePredicate, Predicate<BlockState> stopPredicate, int range) {
        Direction lv = Direction.get(direction, Direction.Axis.Y);
        BlockPos.Mutable lv2 = pos.mutableCopy();
        for (int j = 1; j < range; ++j) {
            lv2.move(lv);
            BlockState lv3 = world.getBlockState(lv2);
            if (stopPredicate.test(lv3)) {
                return Optional.of(lv2.toImmutable());
            }
            if (!world.isOutOfHeightLimit(lv2.getY()) && continuePredicate.test(lv2, lv3)) continue;
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static boolean canDripThrough(BlockView world, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (state.isOpaqueFullCube(world, pos)) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        VoxelShape lv = state.getCollisionShape(world, pos);
        return !VoxelShapes.matchesAnywhere(DRIP_COLLISION_SHAPE, lv, BooleanBiFunction.AND);
    }

    record DrippingFluid(BlockPos pos, Fluid fluid, BlockState sourceState) {
    }
}

