/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import java.util.function.Supplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class StemBlock
extends PlantBlock
implements Fertilizable {
    public static final int MAX_AGE = 7;
    public static final IntProperty AGE = Properties.AGE_7;
    protected static final float field_31256 = 1.0f;
    protected static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 2.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 4.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 6.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 8.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 10.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 12.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 14.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)};
    private final GourdBlock gourdBlock;
    private final Supplier<Item> pickBlockItem;

    protected StemBlock(GourdBlock gourdBlock, Supplier<Item> pickBlockItem, AbstractBlock.Settings settings) {
        super(settings);
        this.gourdBlock = gourdBlock;
        this.pickBlockItem = pickBlockItem;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AGE_TO_SHAPE[state.get(AGE)];
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBaseLightLevel(pos, 0) < 9) {
            return;
        }
        float f = CropBlock.getAvailableMoisture(this, world, pos);
        if (random.nextInt((int)(25.0f / f) + 1) == 0) {
            int i = state.get(AGE);
            if (i < 7) {
                state = (BlockState)state.with(AGE, i + 1);
                world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            } else {
                Direction lv = Direction.Type.HORIZONTAL.random(random);
                BlockPos lv2 = pos.offset(lv);
                BlockState lv3 = world.getBlockState(lv2.down());
                if (world.getBlockState(lv2).isAir() && (lv3.isOf(Blocks.FARMLAND) || lv3.isIn(BlockTags.DIRT))) {
                    world.setBlockState(lv2, this.gourdBlock.getDefaultState());
                    world.setBlockState(pos, (BlockState)this.gourdBlock.getAttachedStem().getDefaultState().with(HorizontalFacingBlock.FACING, lv));
                }
            }
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(this.pickBlockItem.get());
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return state.get(AGE) != 7;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int i = Math.min(7, state.get(AGE) + MathHelper.nextInt(world.random, 2, 5));
        BlockState lv = (BlockState)state.with(AGE, i);
        world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
        if (i == 7) {
            lv.randomTick(world, pos, world.random);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public GourdBlock getGourdBlock() {
        return this.gourdBlock;
    }
}

