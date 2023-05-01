/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class PistonExtensionBlock
extends BlockWithEntity {
    public static final DirectionProperty FACING = PistonHeadBlock.FACING;
    public static final EnumProperty<PistonType> TYPE = PistonHeadBlock.TYPE;

    public PistonExtensionBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(TYPE, PistonType.DEFAULT));
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    public static BlockEntity createBlockEntityPiston(BlockPos pos, BlockState state, BlockState pushedBlock, Direction facing, boolean extending, boolean source) {
        return new PistonBlockEntity(pos, state, pushedBlock, facing, extending, source);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return PistonExtensionBlock.checkType(type, BlockEntityType.PISTON, PistonBlockEntity::tick);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof PistonBlockEntity) {
            ((PistonBlockEntity)lv).finish();
        }
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        BlockPos lv = pos.offset(state.get(FACING).getOpposite());
        BlockState lv2 = world.getBlockState(lv);
        if (lv2.getBlock() instanceof PistonBlock && lv2.get(PistonBlock.EXTENDED).booleanValue()) {
            world.removeBlock(lv, false);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) == null) {
            world.removeBlock(pos, false);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        PistonBlockEntity lv = this.getPistonBlockEntity(builder.getWorld(), BlockPos.ofFloored(builder.get(LootContextParameters.ORIGIN)));
        if (lv == null) {
            return Collections.emptyList();
        }
        return lv.getPushedBlock().getDroppedStacks(builder);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        PistonBlockEntity lv = this.getPistonBlockEntity(world, pos);
        if (lv != null) {
            return lv.getCollisionShape(world, pos);
        }
        return VoxelShapes.empty();
    }

    @Nullable
    private PistonBlockEntity getPistonBlockEntity(BlockView world, BlockPos pos) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof PistonBlockEntity) {
            return (PistonBlockEntity)lv;
        }
        return null;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}

