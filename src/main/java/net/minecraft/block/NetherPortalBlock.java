/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.NetherPortal;

public class NetherPortalBlock
extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    protected static final int field_31196 = 2;
    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public NetherPortalBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(AXIS)) {
            case Z: {
                return Z_SHAPE;
            }
        }
        return X_SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getDimension().natural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && random.nextInt(2000) < world.getDifficulty().getId()) {
            ZombifiedPiglinEntity lv;
            while (world.getBlockState(pos).isOf(this)) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).allowsSpawning(world, pos, EntityType.ZOMBIFIED_PIGLIN) && (lv = EntityType.ZOMBIFIED_PIGLIN.spawn(world, pos.up(), SpawnReason.STRUCTURE)) != null) {
                lv.resetPortalCooldown();
            }
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        boolean bl;
        Direction.Axis lv = direction.getAxis();
        Direction.Axis lv2 = state.get(AXIS);
        boolean bl2 = bl = lv2 != lv && lv.isHorizontal();
        if (bl || neighborState.isOf(this) || new NetherPortal(world, pos, lv2).wasAlreadyValid()) {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.canUsePortals()) {
            entity.setInNetherPortal(pos);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(100) == 0) {
            world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5f, random.nextFloat() * 0.4f + 0.8f, false);
        }
        for (int i = 0; i < 4; ++i) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = ((double)random.nextFloat() - 0.5) * 0.5;
            double h = ((double)random.nextFloat() - 0.5) * 0.5;
            double j = ((double)random.nextFloat() - 0.5) * 0.5;
            int k = random.nextInt(2) * 2 - 1;
            if (world.getBlockState(pos.west()).isOf(this) || world.getBlockState(pos.east()).isOf(this)) {
                f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
                j = random.nextFloat() * 2.0f * (float)k;
            } else {
                d = (double)pos.getX() + 0.5 + 0.25 * (double)k;
                g = random.nextFloat() * 2.0f * (float)k;
            }
            world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, j);
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                switch (state.get(AXIS)) {
                    case X: {
                        return (BlockState)state.with(AXIS, Direction.Axis.Z);
                    }
                    case Z: {
                        return (BlockState)state.with(AXIS, Direction.Axis.X);
                    }
                }
                return state;
            }
        }
        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}

