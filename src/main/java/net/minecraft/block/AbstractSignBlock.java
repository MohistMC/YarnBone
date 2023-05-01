/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class AbstractSignBlock
extends BlockWithEntity
implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected static final float field_31243 = 4.0f;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private final WoodType type;

    protected AbstractSignBlock(AbstractBlock.Settings settings, WoodType type) {
        super(settings);
        this.type = type;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public boolean canMobSpawnInside() {
        return true;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        boolean bl4;
        ItemStack lv = player.getStackInHand(hand);
        Item lv2 = lv.getItem();
        boolean bl = lv2 instanceof DyeItem;
        boolean bl2 = lv.isOf(Items.GLOW_INK_SAC);
        boolean bl3 = lv.isOf(Items.INK_SAC);
        boolean bl5 = bl4 = (bl2 || bl || bl3) && player.getAbilities().allowModifyWorld;
        if (world.isClient) {
            return bl4 ? ActionResult.SUCCESS : ActionResult.CONSUME;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity lv3 = (SignBlockEntity)blockEntity;
            boolean bl52 = lv3.isGlowingText();
            if (bl2 && bl52 || bl3 && !bl52) {
                return ActionResult.PASS;
            }
            if (bl4) {
                boolean bl6;
                if (bl2) {
                    world.playSound(null, pos, SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    bl6 = lv3.setGlowingText(true);
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, lv);
                    }
                } else if (bl3) {
                    world.playSound(null, pos, SoundEvents.ITEM_INK_SAC_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    bl6 = lv3.setGlowingText(false);
                } else {
                    world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    bl6 = lv3.setTextColor(((DyeItem)lv2).getColor());
                }
                if (bl6) {
                    if (!player.isCreative()) {
                        lv.decrement(1);
                    }
                    player.incrementStat(Stats.USED.getOrCreateStat(lv2));
                }
            }
            return lv3.onActivate((ServerPlayerEntity)player) ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    public WoodType getWoodType() {
        return this.type;
    }

    public static WoodType getWoodType(Block block) {
        WoodType lv = block instanceof AbstractSignBlock ? ((AbstractSignBlock)block).getWoodType() : WoodType.OAK;
        return lv;
    }
}

