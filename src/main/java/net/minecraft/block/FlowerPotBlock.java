/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class FlowerPotBlock
extends Block {
    private static final Map<Block, Block> CONTENT_TO_POTTED = Maps.newHashMap();
    public static final float field_31095 = 3.0f;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
    private final Block content;

    public FlowerPotBlock(Block content, AbstractBlock.Settings settings) {
        super(settings);
        this.content = content;
        CONTENT_TO_POTTED.put(content, this);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        boolean bl2;
        ItemStack lv = player.getStackInHand(hand);
        Item lv2 = lv.getItem();
        BlockState lv3 = (lv2 instanceof BlockItem ? CONTENT_TO_POTTED.getOrDefault(((BlockItem)lv2).getBlock(), Blocks.AIR) : Blocks.AIR).getDefaultState();
        boolean bl = lv3.isOf(Blocks.AIR);
        if (bl != (bl2 = this.isEmpty())) {
            if (bl2) {
                world.setBlockState(pos, lv3, Block.NOTIFY_ALL);
                player.incrementStat(Stats.POT_FLOWER);
                if (!player.getAbilities().creativeMode) {
                    lv.decrement(1);
                }
            } else {
                ItemStack lv4 = new ItemStack(this.content);
                if (lv.isEmpty()) {
                    player.setStackInHand(hand, lv4);
                } else if (!player.giveItemStack(lv4)) {
                    player.dropItem(lv4, false);
                }
                world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState(), Block.NOTIFY_ALL);
            }
            world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        if (this.isEmpty()) {
            return super.getPickStack(world, pos, state);
        }
        return new ItemStack(this.content);
    }

    private boolean isEmpty() {
        return this.content == Blocks.AIR;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public Block getContent() {
        return this.content;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}

