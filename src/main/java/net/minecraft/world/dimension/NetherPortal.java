/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.dimension;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class NetherPortal {
    private static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    private static final int field_31826 = 3;
    public static final int field_31824 = 21;
    private static final AbstractBlock.ContextPredicate IS_VALID_FRAME_BLOCK = (state, world, pos) -> state.isOf(Blocks.OBSIDIAN);
    private static final float FALLBACK_THRESHOLD = 4.0f;
    private static final double HEIGHT_STRETCH = 1.0;
    private final WorldAccess world;
    private final Direction.Axis axis;
    private final Direction negativeDir;
    private int foundPortalBlocks;
    @Nullable
    private BlockPos lowerCorner;
    private int height;
    private final int width;

    public static Optional<NetherPortal> getNewPortal(WorldAccess world, BlockPos pos, Direction.Axis axis) {
        return NetherPortal.getOrEmpty(world, pos, areaHelper -> areaHelper.isValid() && areaHelper.foundPortalBlocks == 0, axis);
    }

    public static Optional<NetherPortal> getOrEmpty(WorldAccess world, BlockPos pos, Predicate<NetherPortal> validator, Direction.Axis axis) {
        Optional<NetherPortal> optional = Optional.of(new NetherPortal(world, pos, axis)).filter(validator);
        if (optional.isPresent()) {
            return optional;
        }
        Direction.Axis lv = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(new NetherPortal(world, pos, lv)).filter(validator);
    }

    public NetherPortal(WorldAccess world, BlockPos pos, Direction.Axis axis) {
        this.world = world;
        this.axis = axis;
        this.negativeDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.lowerCorner = this.getLowerCorner(pos);
        if (this.lowerCorner == null) {
            this.lowerCorner = pos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.getWidth();
            if (this.width > 0) {
                this.height = this.getHeight();
            }
        }
    }

    @Nullable
    private BlockPos getLowerCorner(BlockPos pos) {
        int i = Math.max(this.world.getBottomY(), pos.getY() - 21);
        while (pos.getY() > i && NetherPortal.validStateInsidePortal(this.world.getBlockState(pos.down()))) {
            pos = pos.down();
        }
        Direction lv = this.negativeDir.getOpposite();
        int j = this.getWidth(pos, lv) - 1;
        if (j < 0) {
            return null;
        }
        return pos.offset(lv, j);
    }

    private int getWidth() {
        int i = this.getWidth(this.lowerCorner, this.negativeDir);
        if (i < 2 || i > 21) {
            return 0;
        }
        return i;
    }

    private int getWidth(BlockPos pos, Direction direction) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int i = 0; i <= 21; ++i) {
            lv.set(pos).move(direction, i);
            BlockState lv2 = this.world.getBlockState(lv);
            if (!NetherPortal.validStateInsidePortal(lv2)) {
                if (!IS_VALID_FRAME_BLOCK.test(lv2, this.world, lv)) break;
                return i;
            }
            BlockState lv3 = this.world.getBlockState(lv.move(Direction.DOWN));
            if (!IS_VALID_FRAME_BLOCK.test(lv3, this.world, lv)) break;
        }
        return 0;
    }

    private int getHeight() {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        int i = this.getPotentialHeight(lv);
        if (i < 3 || i > 21 || !this.isHorizontalFrameValid(lv, i)) {
            return 0;
        }
        return i;
    }

    private boolean isHorizontalFrameValid(BlockPos.Mutable pos, int height) {
        for (int j = 0; j < this.width; ++j) {
            BlockPos.Mutable lv = pos.set(this.lowerCorner).move(Direction.UP, height).move(this.negativeDir, j);
            if (IS_VALID_FRAME_BLOCK.test(this.world.getBlockState(lv), this.world, lv)) continue;
            return false;
        }
        return true;
    }

    private int getPotentialHeight(BlockPos.Mutable pos) {
        for (int i = 0; i < 21; ++i) {
            pos.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, -1);
            if (!IS_VALID_FRAME_BLOCK.test(this.world.getBlockState(pos), this.world, pos)) {
                return i;
            }
            pos.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, this.width);
            if (!IS_VALID_FRAME_BLOCK.test(this.world.getBlockState(pos), this.world, pos)) {
                return i;
            }
            for (int j = 0; j < this.width; ++j) {
                pos.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, j);
                BlockState lv = this.world.getBlockState(pos);
                if (!NetherPortal.validStateInsidePortal(lv)) {
                    return i;
                }
                if (!lv.isOf(Blocks.NETHER_PORTAL)) continue;
                ++this.foundPortalBlocks;
            }
        }
        return 21;
    }

    private static boolean validStateInsidePortal(BlockState state) {
        return state.isAir() || state.isIn(BlockTags.FIRE) || state.isOf(Blocks.NETHER_PORTAL);
    }

    public boolean isValid() {
        return this.lowerCorner != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortal() {
        BlockState lv = (BlockState)Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, this.axis);
        BlockPos.iterate(this.lowerCorner, this.lowerCorner.offset(Direction.UP, this.height - 1).offset(this.negativeDir, this.width - 1)).forEach(arg2 -> this.world.setBlockState((BlockPos)arg2, lv, Block.NOTIFY_LISTENERS | Block.FORCE_STATE));
    }

    public boolean wasAlreadyValid() {
        return this.isValid() && this.foundPortalBlocks == this.width * this.height;
    }

    public static Vec3d entityPosInPortal(BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d entityPos, EntityDimensions entityDimensions) {
        double h;
        Direction.Axis lv2;
        double g;
        double d = (double)portalRect.width - (double)entityDimensions.width;
        double e = (double)portalRect.height - (double)entityDimensions.height;
        BlockPos lv = portalRect.lowerLeft;
        if (d > 0.0) {
            float f = (float)lv.getComponentAlongAxis(portalAxis) + entityDimensions.width / 2.0f;
            g = MathHelper.clamp(MathHelper.getLerpProgress(entityPos.getComponentAlongAxis(portalAxis) - (double)f, 0.0, d), 0.0, 1.0);
        } else {
            g = 0.5;
        }
        if (e > 0.0) {
            lv2 = Direction.Axis.Y;
            h = MathHelper.clamp(MathHelper.getLerpProgress(entityPos.getComponentAlongAxis(lv2) - (double)lv.getComponentAlongAxis(lv2), 0.0, e), 0.0, 1.0);
        } else {
            h = 0.0;
        }
        lv2 = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double i = entityPos.getComponentAlongAxis(lv2) - ((double)lv.getComponentAlongAxis(lv2) + 0.5);
        return new Vec3d(g, h, i);
    }

    public static TeleportTarget getNetherTeleportTarget(ServerWorld destination, BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d offset, Entity entity, Vec3d velocity, float yaw, float pitch) {
        BlockPos lv = portalRect.lowerLeft;
        BlockState lv2 = destination.getBlockState(lv);
        Direction.Axis lv3 = lv2.getOrEmpty(Properties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double d = portalRect.width;
        double e = portalRect.height;
        EntityDimensions lv4 = entity.getDimensions(entity.getPose());
        int i = portalAxis == lv3 ? 0 : 90;
        Vec3d lv5 = portalAxis == lv3 ? velocity : new Vec3d(velocity.z, velocity.y, -velocity.x);
        double h = (double)lv4.width / 2.0 + (d - (double)lv4.width) * offset.getX();
        double j = (e - (double)lv4.height) * offset.getY();
        double k = 0.5 + offset.getZ();
        boolean bl = lv3 == Direction.Axis.X;
        Vec3d lv6 = new Vec3d((double)lv.getX() + (bl ? h : k), (double)lv.getY() + j, (double)lv.getZ() + (bl ? k : h));
        Vec3d lv7 = NetherPortal.findOpenPosition(lv6, destination, entity, lv4);
        return new TeleportTarget(lv7, lv5, yaw + (float)i, pitch);
    }

    private static Vec3d findOpenPosition(Vec3d fallback, ServerWorld world, Entity entity, EntityDimensions dimensions) {
        if (dimensions.width > 4.0f || dimensions.height > 4.0f) {
            return fallback;
        }
        double d = (double)dimensions.height / 2.0;
        Vec3d lv = fallback.add(0.0, d, 0.0);
        VoxelShape lv2 = VoxelShapes.cuboid(Box.of(lv, dimensions.width, 0.0, dimensions.width).stretch(0.0, 1.0, 0.0).expand(1.0E-6));
        Optional<Vec3d> optional = world.findClosestCollision(entity, lv2, lv, dimensions.width, dimensions.height, dimensions.width);
        Optional<Vec3d> optional2 = optional.map(pos -> pos.subtract(0.0, d, 0.0));
        return optional2.orElse(fallback);
    }
}

