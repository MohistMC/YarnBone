/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PistonBlock
extends FacingBlock {
    public static final BooleanProperty EXTENDED = Properties.EXTENDED;
    public static final int field_31373 = 0;
    public static final int field_31374 = 1;
    public static final int field_31375 = 2;
    public static final float field_31376 = 4.0f;
    protected static final VoxelShape EXTENDED_EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    protected static final VoxelShape EXTENDED_WEST_SHAPE = Block.createCuboidShape(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EXTENDED_SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
    protected static final VoxelShape EXTENDED_NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EXTENDED_UP_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    protected static final VoxelShape EXTENDED_DOWN_SHAPE = Block.createCuboidShape(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
    private final boolean sticky;

    public PistonBlock(boolean sticky, AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(EXTENDED, false));
        this.sticky = sticky;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(EXTENDED).booleanValue()) {
            switch (state.get(FACING)) {
                case DOWN: {
                    return EXTENDED_DOWN_SHAPE;
                }
                default: {
                    return EXTENDED_UP_SHAPE;
                }
                case NORTH: {
                    return EXTENDED_NORTH_SHAPE;
                }
                case SOUTH: {
                    return EXTENDED_SOUTH_SHAPE;
                }
                case WEST: {
                    return EXTENDED_WEST_SHAPE;
                }
                case EAST: 
            }
            return EXTENDED_EAST_SHAPE;
        }
        return VoxelShapes.fullCube();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            this.tryMove(world, pos, state);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            this.tryMove(world, pos, state);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        if (!world.isClient && world.getBlockEntity(pos) == null) {
            this.tryMove(world, pos, state);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite())).with(EXTENDED, false);
    }

    private void tryMove(World world, BlockPos pos, BlockState state) {
        Direction lv = state.get(FACING);
        boolean bl = this.shouldExtend(world, pos, lv);
        if (bl && !state.get(EXTENDED).booleanValue()) {
            if (new PistonHandler(world, pos, lv, true).calculatePush()) {
                world.addSyncedBlockEvent(pos, this, 0, lv.getId());
            }
        } else if (!bl && state.get(EXTENDED).booleanValue()) {
            PistonBlockEntity lv5;
            BlockEntity lv4;
            BlockPos lv2 = pos.offset(lv, 2);
            BlockState lv3 = world.getBlockState(lv2);
            int i = 1;
            if (lv3.isOf(Blocks.MOVING_PISTON) && lv3.get(FACING) == lv && (lv4 = world.getBlockEntity(lv2)) instanceof PistonBlockEntity && (lv5 = (PistonBlockEntity)lv4).isExtending() && (lv5.getProgress(0.0f) < 0.5f || world.getTime() == lv5.getSavedWorldTime() || ((ServerWorld)world).isInBlockTick())) {
                i = 2;
            }
            world.addSyncedBlockEvent(pos, this, i, lv.getId());
        }
    }

    private boolean shouldExtend(World world, BlockPos pos, Direction pistonFace) {
        for (Direction lv : Direction.values()) {
            if (lv == pistonFace || !world.isEmittingRedstonePower(pos.offset(lv), lv)) continue;
            return true;
        }
        if (world.isEmittingRedstonePower(pos, Direction.DOWN)) {
            return true;
        }
        BlockPos lv2 = pos.up();
        for (Direction lv3 : Direction.values()) {
            if (lv3 == Direction.DOWN || !world.isEmittingRedstonePower(lv2.offset(lv3), lv3)) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        Direction lv = state.get(FACING);
        if (!world.isClient) {
            boolean bl = this.shouldExtend(world, pos, lv);
            if (bl && (type == 1 || type == 2)) {
                world.setBlockState(pos, (BlockState)state.with(EXTENDED, true), Block.NOTIFY_LISTENERS);
                return false;
            }
            if (!bl && type == 0) {
                return false;
            }
        }
        if (type == 0) {
            if (!this.move(world, pos, lv, true)) return false;
            world.setBlockState(pos, (BlockState)state.with(EXTENDED, true), Block.NOTIFY_ALL | Block.MOVED);
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.25f + 0.6f);
            world.emitGameEvent(null, GameEvent.PISTON_EXTEND, pos);
            return true;
        } else {
            if (type != 1 && type != 2) return true;
            BlockEntity lv2 = world.getBlockEntity(pos.offset(lv));
            if (lv2 instanceof PistonBlockEntity) {
                ((PistonBlockEntity)lv2).finish();
            }
            BlockState lv3 = (BlockState)((BlockState)Blocks.MOVING_PISTON.getDefaultState().with(PistonExtensionBlock.FACING, lv)).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
            world.setBlockState(pos, lv3, Block.NO_REDRAW | Block.FORCE_STATE);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(pos, lv3, (BlockState)this.getDefaultState().with(FACING, Direction.byId(data & 7)), lv, false, true));
            world.updateNeighbors(pos, lv3.getBlock());
            lv3.updateNeighbors(world, pos, Block.NOTIFY_LISTENERS);
            if (this.sticky) {
                PistonBlockEntity lv7;
                BlockEntity lv6;
                BlockPos lv4 = pos.add(lv.getOffsetX() * 2, lv.getOffsetY() * 2, lv.getOffsetZ() * 2);
                BlockState lv5 = world.getBlockState(lv4);
                boolean bl2 = false;
                if (lv5.isOf(Blocks.MOVING_PISTON) && (lv6 = world.getBlockEntity(lv4)) instanceof PistonBlockEntity && (lv7 = (PistonBlockEntity)lv6).getFacing() == lv && lv7.isExtending()) {
                    lv7.finish();
                    bl2 = true;
                }
                if (!bl2) {
                    if (type == 1 && !lv5.isAir() && PistonBlock.isMovable(lv5, world, lv4, lv.getOpposite(), false, lv) && (lv5.getPistonBehavior() == PistonBehavior.NORMAL || lv5.isOf(Blocks.PISTON) || lv5.isOf(Blocks.STICKY_PISTON))) {
                        this.move(world, pos, lv, false);
                    } else {
                        world.removeBlock(pos.offset(lv), false);
                    }
                }
            } else {
                world.removeBlock(pos.offset(lv), false);
            }
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.15f + 0.6f);
            world.emitGameEvent(null, GameEvent.PISTON_CONTRACT, pos);
        }
        return true;
    }

    public static boolean isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir) {
        if (pos.getY() < world.getBottomY() || pos.getY() > world.getTopY() - 1 || !world.getWorldBorder().contains(pos)) {
            return false;
        }
        if (state.isAir()) {
            return true;
        }
        if (state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.CRYING_OBSIDIAN) || state.isOf(Blocks.RESPAWN_ANCHOR) || state.isOf(Blocks.REINFORCED_DEEPSLATE)) {
            return false;
        }
        if (direction == Direction.DOWN && pos.getY() == world.getBottomY()) {
            return false;
        }
        if (direction == Direction.UP && pos.getY() == world.getTopY() - 1) {
            return false;
        }
        if (state.isOf(Blocks.PISTON) || state.isOf(Blocks.STICKY_PISTON)) {
            if (state.get(EXTENDED).booleanValue()) {
                return false;
            }
        } else {
            if (state.getHardness(world, pos) == -1.0f) {
                return false;
            }
            switch (state.getPistonBehavior()) {
                case BLOCK: {
                    return false;
                }
                case DESTROY: {
                    return canBreak;
                }
                case PUSH_ONLY: {
                    return direction == pistonDir;
                }
            }
        }
        return !state.hasBlockEntity();
    }

    private boolean move(World world, BlockPos pos, Direction dir, boolean retract) {
        int l;
        BlockPos lv14;
        BlockPos lv6;
        int k;
        PistonHandler lv2;
        BlockPos lv = pos.offset(dir);
        if (!retract && world.getBlockState(lv).isOf(Blocks.PISTON_HEAD)) {
            world.setBlockState(lv, Blocks.AIR.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
        }
        if (!(lv2 = new PistonHandler(world, pos, dir, retract)).calculatePush()) {
            return false;
        }
        HashMap<BlockPos, BlockState> map = Maps.newHashMap();
        List<BlockPos> list = lv2.getMovedBlocks();
        ArrayList<BlockState> list2 = Lists.newArrayList();
        for (int i = 0; i < list.size(); ++i) {
            BlockPos lv3 = list.get(i);
            BlockState lv4 = world.getBlockState(lv3);
            list2.add(lv4);
            map.put(lv3, lv4);
        }
        List<BlockPos> list3 = lv2.getBrokenBlocks();
        BlockState[] lvs = new BlockState[list.size() + list3.size()];
        Direction lv5 = retract ? dir : dir.getOpposite();
        int j = 0;
        for (k = list3.size() - 1; k >= 0; --k) {
            lv6 = list3.get(k);
            BlockState blockState = world.getBlockState(lv6);
            BlockEntity lv8 = blockState.hasBlockEntity() ? world.getBlockEntity(lv6) : null;
            PistonBlock.dropStacks(blockState, world, lv6, lv8);
            world.setBlockState(lv6, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, lv6, GameEvent.Emitter.of(blockState));
            if (!blockState.isIn(BlockTags.FIRE)) {
                world.addBlockBreakParticles(lv6, blockState);
            }
            lvs[j++] = blockState;
        }
        for (k = list.size() - 1; k >= 0; --k) {
            lv6 = list.get(k);
            BlockState blockState = world.getBlockState(lv6);
            lv6 = lv6.offset(lv5);
            map.remove(lv6);
            BlockState lv9 = (BlockState)Blocks.MOVING_PISTON.getDefaultState().with(FACING, dir);
            world.setBlockState(lv6, lv9, Block.NO_REDRAW | Block.MOVED);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(lv6, lv9, (BlockState)list2.get(k), dir, retract, false));
            lvs[j++] = blockState;
        }
        if (retract) {
            PistonType lv10 = this.sticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState lv11 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.FACING, dir)).with(PistonHeadBlock.TYPE, lv10);
            BlockState blockState = (BlockState)((BlockState)Blocks.MOVING_PISTON.getDefaultState().with(PistonExtensionBlock.FACING, dir)).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(lv);
            world.setBlockState(lv, blockState, Block.NO_REDRAW | Block.MOVED);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(lv, blockState, lv11, dir, true, true));
        }
        BlockState lv12 = Blocks.AIR.getDefaultState();
        for (BlockPos blockPos : map.keySet()) {
            world.setBlockState(blockPos, lv12, Block.NOTIFY_LISTENERS | Block.FORCE_STATE | Block.MOVED);
        }
        for (Map.Entry entry : map.entrySet()) {
            lv14 = (BlockPos)entry.getKey();
            BlockState lv15 = (BlockState)entry.getValue();
            lv15.prepare(world, lv14, 2);
            lv12.updateNeighbors(world, lv14, Block.NOTIFY_LISTENERS);
            lv12.prepare(world, lv14, 2);
        }
        j = 0;
        for (l = list3.size() - 1; l >= 0; --l) {
            BlockState blockState = lvs[j++];
            lv14 = list3.get(l);
            blockState.prepare(world, lv14, 2);
            world.updateNeighborsAlways(lv14, blockState.getBlock());
        }
        for (l = list.size() - 1; l >= 0; --l) {
            world.updateNeighborsAlways(list.get(l), lvs[j++].getBlock());
        }
        if (retract) {
            world.updateNeighborsAlways(lv, Blocks.PISTON_HEAD);
        }
        return true;
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
        builder.add(FACING, EXTENDED);
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return state.get(EXTENDED);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}

