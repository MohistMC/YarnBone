/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class HangingSignBlock
extends AbstractSignBlock {
    public static final IntProperty ROTATION = Properties.ROTATION;
    public static final BooleanProperty ATTACHED = Properties.ATTACHED;
    protected static final float field_40302 = 5.0f;
    protected static final VoxelShape DEFAULT_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final Map<Integer, VoxelShape> SHAPES_FOR_ROTATION = Maps.newHashMap(ImmutableMap.of(0, Block.createCuboidShape(1.0, 0.0, 7.0, 15.0, 10.0, 9.0), 4, Block.createCuboidShape(7.0, 0.0, 1.0, 9.0, 10.0, 15.0), 8, Block.createCuboidShape(1.0, 0.0, 7.0, 15.0, 10.0, 9.0), 12, Block.createCuboidShape(7.0, 0.0, 1.0, 9.0, 10.0, 15.0)));

    public HangingSignBlock(AbstractBlock.Settings arg, WoodType arg2) {
        super(arg.sounds(arg2.hangingSignSoundType()), arg2);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ROTATION, 0)).with(ATTACHED, false)).with(WATERLOGGED, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity lv = (SignBlockEntity)blockEntity;
            ItemStack lv2 = player.getStackInHand(hand);
            if (!lv.shouldRunCommand(player) && lv2.getItem() instanceof BlockItem) {
                return ActionResult.PASS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.up()).isSideSolid(world, pos.up(), Direction.DOWN, SideShapeType.CENTER);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean bl2;
        World lv = ctx.getWorld();
        FluidState lv2 = lv.getFluidState(ctx.getBlockPos());
        BlockPos lv3 = ctx.getBlockPos().up();
        BlockState lv4 = lv.getBlockState(lv3);
        boolean bl = lv4.isIn(BlockTags.ALL_HANGING_SIGNS);
        Direction lv5 = Direction.fromRotation(ctx.getPlayerYaw());
        boolean bl3 = bl2 = !Block.isFaceFullSquare(lv4.getCollisionShape(lv, lv3), Direction.DOWN) || ctx.shouldCancelInteraction();
        if (bl && !ctx.shouldCancelInteraction()) {
            Optional<Direction> optional;
            if (lv4.contains(WallHangingSignBlock.FACING)) {
                Direction lv6 = lv4.get(WallHangingSignBlock.FACING);
                if (lv6.getAxis().test(lv5)) {
                    bl2 = false;
                }
            } else if (lv4.contains(ROTATION) && (optional = RotationPropertyHelper.toDirection(lv4.get(ROTATION))).isPresent() && optional.get().getAxis().test(lv5)) {
                bl2 = false;
            }
        }
        int i = !bl2 ? RotationPropertyHelper.fromDirection(lv5.getOpposite()) : RotationPropertyHelper.fromYaw(ctx.getPlayerYaw() + 180.0f);
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(ATTACHED, bl2)).with(ROTATION, i)).with(WATERLOGGED, lv2.getFluid() == Fluids.WATER);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape lv = SHAPES_FOR_ROTATION.get(state.get(ROTATION));
        return lv == null ? DEFAULT_SHAPE : lv;
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return this.getOutlineShape(state, world, pos, ShapeContext.absent());
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !this.canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(ROTATION, rotation.rotate(state.get(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState)state.with(ROTATION, mirror.mirror(state.get(ROTATION), 16));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, ATTACHED, WATERLOGGED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HangingSignBlockEntity(pos, state);
    }
}

