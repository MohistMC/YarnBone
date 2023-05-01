/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class LandPathNodeMaker
extends PathNodeMaker {
    public static final double Y_OFFSET = 0.5;
    private static final double MIN_STEP_HEIGHT = 1.125;
    protected float waterPathNodeTypeWeight;
    private final Long2ObjectMap<PathNodeType> nodeTypes = new Long2ObjectOpenHashMap<PathNodeType>();
    private final Object2BooleanMap<Box> collidedBoxes = new Object2BooleanOpenHashMap<Box>();

    @Override
    public void init(ChunkCache cachedWorld, MobEntity entity) {
        super.init(cachedWorld, entity);
        this.waterPathNodeTypeWeight = entity.getPathfindingPenalty(PathNodeType.WATER);
    }

    @Override
    public void clear() {
        this.entity.setPathfindingPenalty(PathNodeType.WATER, this.waterPathNodeTypeWeight);
        this.nodeTypes.clear();
        this.collidedBoxes.clear();
        super.clear();
    }

    @Override
    public PathNode getStart() {
        BlockPos lv3;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        int i = this.entity.getBlockY();
        BlockState lv2 = this.cachedWorld.getBlockState(lv.set(this.entity.getX(), (double)i, this.entity.getZ()));
        if (this.entity.canWalkOnFluid(lv2.getFluidState())) {
            while (this.entity.canWalkOnFluid(lv2.getFluidState())) {
                lv2 = this.cachedWorld.getBlockState(lv.set(this.entity.getX(), (double)(++i), this.entity.getZ()));
            }
            --i;
        } else if (this.canSwim() && this.entity.isTouchingWater()) {
            while (lv2.isOf(Blocks.WATER) || lv2.getFluidState() == Fluids.WATER.getStill(false)) {
                lv2 = this.cachedWorld.getBlockState(lv.set(this.entity.getX(), (double)(++i), this.entity.getZ()));
            }
            --i;
        } else if (this.entity.isOnGround()) {
            i = MathHelper.floor(this.entity.getY() + 0.5);
        } else {
            lv3 = this.entity.getBlockPos();
            while ((this.cachedWorld.getBlockState(lv3).isAir() || this.cachedWorld.getBlockState(lv3).canPathfindThrough(this.cachedWorld, lv3, NavigationType.LAND)) && lv3.getY() > this.entity.world.getBottomY()) {
                lv3 = lv3.down();
            }
            i = lv3.up().getY();
        }
        lv3 = this.entity.getBlockPos();
        if (!this.canPathThrough(lv.set(lv3.getX(), i, lv3.getZ()))) {
            Box lv4 = this.entity.getBoundingBox();
            if (this.canPathThrough(lv.set(lv4.minX, (double)i, lv4.minZ)) || this.canPathThrough(lv.set(lv4.minX, (double)i, lv4.maxZ)) || this.canPathThrough(lv.set(lv4.maxX, (double)i, lv4.minZ)) || this.canPathThrough(lv.set(lv4.maxX, (double)i, lv4.maxZ))) {
                return this.getStart(lv);
            }
        }
        return this.getStart(new BlockPos(lv3.getX(), i, lv3.getZ()));
    }

    protected PathNode getStart(BlockPos pos) {
        PathNode lv = this.getNode(pos);
        lv.type = this.getNodeType(this.entity, lv.getBlockPos());
        lv.penalty = this.entity.getPathfindingPenalty(lv.type);
        return lv;
    }

    protected boolean canPathThrough(BlockPos pos) {
        PathNodeType lv = this.getNodeType(this.entity, pos);
        return lv != PathNodeType.OPEN && this.entity.getPathfindingPenalty(lv) >= 0.0f;
    }

    @Override
    public TargetPathNode getNode(double x, double y, double z) {
        return this.asTargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        PathNode lv10;
        PathNode lv9;
        PathNode lv8;
        PathNode lv7;
        PathNode lv6;
        PathNode lv5;
        PathNode lv4;
        double d;
        PathNode lv3;
        int i = 0;
        int j = 0;
        PathNodeType lv = this.getNodeType(this.entity, node.x, node.y + 1, node.z);
        PathNodeType lv2 = this.getNodeType(this.entity, node.x, node.y, node.z);
        if (this.entity.getPathfindingPenalty(lv) >= 0.0f && lv2 != PathNodeType.STICKY_HONEY) {
            j = MathHelper.floor(Math.max(1.0f, this.entity.getStepHeight()));
        }
        if (this.isValidAdjacentSuccessor(lv3 = this.getPathNode(node.x, node.y, node.z + 1, j, d = this.getFeetY(new BlockPos(node.x, node.y, node.z)), Direction.SOUTH, lv2), node)) {
            successors[i++] = lv3;
        }
        if (this.isValidAdjacentSuccessor(lv4 = this.getPathNode(node.x - 1, node.y, node.z, j, d, Direction.WEST, lv2), node)) {
            successors[i++] = lv4;
        }
        if (this.isValidAdjacentSuccessor(lv5 = this.getPathNode(node.x + 1, node.y, node.z, j, d, Direction.EAST, lv2), node)) {
            successors[i++] = lv5;
        }
        if (this.isValidAdjacentSuccessor(lv6 = this.getPathNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH, lv2), node)) {
            successors[i++] = lv6;
        }
        if (this.isValidDiagonalSuccessor(node, lv4, lv6, lv7 = this.getPathNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH, lv2))) {
            successors[i++] = lv7;
        }
        if (this.isValidDiagonalSuccessor(node, lv5, lv6, lv8 = this.getPathNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH, lv2))) {
            successors[i++] = lv8;
        }
        if (this.isValidDiagonalSuccessor(node, lv4, lv3, lv9 = this.getPathNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH, lv2))) {
            successors[i++] = lv9;
        }
        if (this.isValidDiagonalSuccessor(node, lv5, lv3, lv10 = this.getPathNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH, lv2))) {
            successors[i++] = lv10;
        }
        return i;
    }

    protected boolean isValidAdjacentSuccessor(@Nullable PathNode node, PathNode successor1) {
        return node != null && !node.visited && (node.penalty >= 0.0f || successor1.penalty < 0.0f);
    }

    protected boolean isValidDiagonalSuccessor(PathNode xNode, @Nullable PathNode zNode, @Nullable PathNode xDiagNode, @Nullable PathNode zDiagNode) {
        if (zDiagNode == null || xDiagNode == null || zNode == null) {
            return false;
        }
        if (zDiagNode.visited) {
            return false;
        }
        if (xDiagNode.y > xNode.y || zNode.y > xNode.y) {
            return false;
        }
        if (zNode.type == PathNodeType.WALKABLE_DOOR || xDiagNode.type == PathNodeType.WALKABLE_DOOR || zDiagNode.type == PathNodeType.WALKABLE_DOOR) {
            return false;
        }
        boolean bl = xDiagNode.type == PathNodeType.FENCE && zNode.type == PathNodeType.FENCE && (double)this.entity.getWidth() < 0.5;
        return zDiagNode.penalty >= 0.0f && (xDiagNode.y < xNode.y || xDiagNode.penalty >= 0.0f || bl) && (zNode.y < xNode.y || zNode.penalty >= 0.0f || bl);
    }

    private static boolean isBlocked(PathNodeType nodeType) {
        return nodeType == PathNodeType.FENCE || nodeType == PathNodeType.DOOR_WOOD_CLOSED || nodeType == PathNodeType.DOOR_IRON_CLOSED;
    }

    private boolean isBlocked(PathNode node) {
        Box lv = this.entity.getBoundingBox();
        Vec3d lv2 = new Vec3d((double)node.x - this.entity.getX() + lv.getXLength() / 2.0, (double)node.y - this.entity.getY() + lv.getYLength() / 2.0, (double)node.z - this.entity.getZ() + lv.getZLength() / 2.0);
        int i = MathHelper.ceil(lv2.length() / lv.getAverageSideLength());
        lv2 = lv2.multiply(1.0f / (float)i);
        for (int j = 1; j <= i; ++j) {
            if (!this.checkBoxCollision(lv = lv.offset(lv2))) continue;
            return false;
        }
        return true;
    }

    protected double getFeetY(BlockPos pos) {
        if ((this.canSwim() || this.isAmphibious()) && this.cachedWorld.getFluidState(pos).isIn(FluidTags.WATER)) {
            return (double)pos.getY() + 0.5;
        }
        return LandPathNodeMaker.getFeetY(this.cachedWorld, pos);
    }

    public static double getFeetY(BlockView world, BlockPos pos) {
        BlockPos lv = pos.down();
        VoxelShape lv2 = world.getBlockState(lv).getCollisionShape(world, lv);
        return (double)lv.getY() + (lv2.isEmpty() ? 0.0 : lv2.getMax(Direction.Axis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        double m;
        double h;
        Box lv4;
        PathNode lv = null;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        double e = this.getFeetY(lv2.set(x, y, z));
        if (e - prevFeetY > this.getStepHeight()) {
            return null;
        }
        PathNodeType lv3 = this.getNodeType(this.entity, x, y, z);
        float f = this.entity.getPathfindingPenalty(lv3);
        double g = (double)this.entity.getWidth() / 2.0;
        if (f >= 0.0f) {
            lv = this.getNodeWith(x, y, z, lv3, f);
        }
        if (LandPathNodeMaker.isBlocked(nodeType) && lv != null && lv.penalty >= 0.0f && !this.isBlocked(lv)) {
            lv = null;
        }
        if (lv3 == PathNodeType.WALKABLE || this.isAmphibious() && lv3 == PathNodeType.WATER) {
            return lv;
        }
        if ((lv == null || lv.penalty < 0.0f) && maxYStep > 0 && (lv3 != PathNodeType.FENCE || this.canWalkOverFences()) && lv3 != PathNodeType.UNPASSABLE_RAIL && lv3 != PathNodeType.TRAPDOOR && lv3 != PathNodeType.POWDER_SNOW && (lv = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY, direction, nodeType)) != null && (lv.type == PathNodeType.OPEN || lv.type == PathNodeType.WALKABLE) && this.entity.getWidth() < 1.0f && this.checkBoxCollision(lv4 = new Box((h = (double)(x - direction.getOffsetX()) + 0.5) - g, this.getFeetY(lv2.set(h, (double)(y + 1), m = (double)(z - direction.getOffsetZ()) + 0.5)) + 0.001, m - g, h + g, (double)this.entity.getHeight() + this.getFeetY(lv2.set((double)lv.x, (double)lv.y, (double)lv.z)) - 0.002, m + g))) {
            lv = null;
        }
        if (!this.isAmphibious() && lv3 == PathNodeType.WATER && !this.canSwim()) {
            if (this.getNodeType(this.entity, x, y - 1, z) != PathNodeType.WATER) {
                return lv;
            }
            while (y > this.entity.world.getBottomY()) {
                if ((lv3 = this.getNodeType(this.entity, x, --y, z)) == PathNodeType.WATER) {
                    lv = this.getNodeWith(x, y, z, lv3, this.entity.getPathfindingPenalty(lv3));
                    continue;
                }
                return lv;
            }
        }
        if (lv3 == PathNodeType.OPEN) {
            int n = 0;
            int o = y;
            while (lv3 == PathNodeType.OPEN) {
                if (--y < this.entity.world.getBottomY()) {
                    return this.getBlockedNode(x, o, z);
                }
                if (n++ >= this.entity.getSafeFallDistance()) {
                    return this.getBlockedNode(x, y, z);
                }
                lv3 = this.getNodeType(this.entity, x, y, z);
                f = this.entity.getPathfindingPenalty(lv3);
                if (lv3 != PathNodeType.OPEN && f >= 0.0f) {
                    lv = this.getNodeWith(x, y, z, lv3, f);
                    break;
                }
                if (!(f < 0.0f)) continue;
                return this.getBlockedNode(x, y, z);
            }
        }
        if (LandPathNodeMaker.isBlocked(lv3) && lv == null) {
            lv = this.getNode(x, y, z);
            lv.visited = true;
            lv.type = lv3;
            lv.penalty = lv3.getDefaultPenalty();
        }
        return lv;
    }

    private double getStepHeight() {
        return Math.max(1.125, (double)this.entity.getStepHeight());
    }

    private PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
        PathNode lv = this.getNode(x, y, z);
        lv.type = type;
        lv.penalty = Math.max(lv.penalty, penalty);
        return lv;
    }

    private PathNode getBlockedNode(int x, int y, int z) {
        PathNode lv = this.getNode(x, y, z);
        lv.type = PathNodeType.BLOCKED;
        lv.penalty = -1.0f;
        return lv;
    }

    private boolean checkBoxCollision(Box box) {
        return this.collidedBoxes.computeIfAbsent(box, box2 -> !this.cachedWorld.isSpaceEmpty(this.entity, box));
    }

    @Override
    public PathNodeType getNodeType(BlockView world, int x, int y, int z, MobEntity mob) {
        EnumSet<PathNodeType> enumSet = EnumSet.noneOf(PathNodeType.class);
        PathNodeType lv = PathNodeType.BLOCKED;
        lv = this.findNearbyNodeTypes(world, x, y, z, enumSet, lv, mob.getBlockPos());
        if (enumSet.contains((Object)PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        }
        if (enumSet.contains((Object)PathNodeType.UNPASSABLE_RAIL)) {
            return PathNodeType.UNPASSABLE_RAIL;
        }
        PathNodeType lv2 = PathNodeType.BLOCKED;
        for (PathNodeType lv3 : enumSet) {
            if (mob.getPathfindingPenalty(lv3) < 0.0f) {
                return lv3;
            }
            if (!(mob.getPathfindingPenalty(lv3) >= mob.getPathfindingPenalty(lv2))) continue;
            lv2 = lv3;
        }
        if (lv == PathNodeType.OPEN && mob.getPathfindingPenalty(lv2) == 0.0f && this.entityBlockXSize <= 1) {
            return PathNodeType.OPEN;
        }
        return lv2;
    }

    public PathNodeType findNearbyNodeTypes(BlockView world, int x, int y, int z, EnumSet<PathNodeType> nearbyTypes, PathNodeType type, BlockPos pos) {
        for (int l = 0; l < this.entityBlockXSize; ++l) {
            for (int m = 0; m < this.entityBlockYSize; ++m) {
                for (int n = 0; n < this.entityBlockZSize; ++n) {
                    int o = l + x;
                    int p = m + y;
                    int q = n + z;
                    PathNodeType lv = this.getDefaultNodeType(world, o, p, q);
                    lv = this.adjustNodeType(world, pos, lv);
                    if (l == 0 && m == 0 && n == 0) {
                        type = lv;
                    }
                    nearbyTypes.add(lv);
                }
            }
        }
        return type;
    }

    protected PathNodeType adjustNodeType(BlockView world, BlockPos pos, PathNodeType type) {
        boolean bl = this.canEnterOpenDoors();
        if (type == PathNodeType.DOOR_WOOD_CLOSED && this.canOpenDoors() && bl) {
            type = PathNodeType.WALKABLE_DOOR;
        }
        if (type == PathNodeType.DOOR_OPEN && !bl) {
            type = PathNodeType.BLOCKED;
        }
        if (type == PathNodeType.RAIL && !(world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) && !(world.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
            type = PathNodeType.UNPASSABLE_RAIL;
        }
        return type;
    }

    protected PathNodeType getNodeType(MobEntity entity, BlockPos pos) {
        return this.getNodeType(entity, pos.getX(), pos.getY(), pos.getZ());
    }

    protected PathNodeType getNodeType(MobEntity entity, int x, int y, int z) {
        return this.nodeTypes.computeIfAbsent(BlockPos.asLong(x, y, z), l -> this.getNodeType(this.cachedWorld, x, y, z, entity));
    }

    @Override
    public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
        return LandPathNodeMaker.getLandNodeType(world, new BlockPos.Mutable(x, y, z));
    }

    public static PathNodeType getLandNodeType(BlockView world, BlockPos.Mutable pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        PathNodeType lv = LandPathNodeMaker.getCommonNodeType(world, pos);
        if (lv == PathNodeType.OPEN && j >= world.getBottomY() + 1) {
            PathNodeType lv2 = LandPathNodeMaker.getCommonNodeType(world, pos.set(i, j - 1, k));
            PathNodeType pathNodeType = lv = lv2 == PathNodeType.WALKABLE || lv2 == PathNodeType.OPEN || lv2 == PathNodeType.WATER || lv2 == PathNodeType.LAVA ? PathNodeType.OPEN : PathNodeType.WALKABLE;
            if (lv2 == PathNodeType.DAMAGE_FIRE) {
                lv = PathNodeType.DAMAGE_FIRE;
            }
            if (lv2 == PathNodeType.DAMAGE_OTHER) {
                lv = PathNodeType.DAMAGE_OTHER;
            }
            if (lv2 == PathNodeType.STICKY_HONEY) {
                lv = PathNodeType.STICKY_HONEY;
            }
            if (lv2 == PathNodeType.POWDER_SNOW) {
                lv = PathNodeType.DANGER_POWDER_SNOW;
            }
        }
        if (lv == PathNodeType.WALKABLE) {
            lv = LandPathNodeMaker.getNodeTypeFromNeighbors(world, pos.set(i, j, k), lv);
        }
        return lv;
    }

    public static PathNodeType getNodeTypeFromNeighbors(BlockView world, BlockPos.Mutable pos, PathNodeType nodeType) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        for (int l = -1; l <= 1; ++l) {
            for (int m = -1; m <= 1; ++m) {
                for (int n = -1; n <= 1; ++n) {
                    if (l == 0 && n == 0) continue;
                    pos.set(i + l, j + m, k + n);
                    BlockState lv = world.getBlockState(pos);
                    if (lv.isOf(Blocks.CACTUS) || lv.isOf(Blocks.SWEET_BERRY_BUSH)) {
                        return PathNodeType.DANGER_OTHER;
                    }
                    if (LandPathNodeMaker.inflictsFireDamage(lv)) {
                        return PathNodeType.DANGER_FIRE;
                    }
                    if (!world.getFluidState(pos).isIn(FluidTags.WATER)) continue;
                    return PathNodeType.WATER_BORDER;
                }
            }
        }
        return nodeType;
    }

    protected static PathNodeType getCommonNodeType(BlockView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        Block lv2 = lv.getBlock();
        Material lv3 = lv.getMaterial();
        if (lv.isAir()) {
            return PathNodeType.OPEN;
        }
        if (lv.isIn(BlockTags.TRAPDOORS) || lv.isOf(Blocks.LILY_PAD) || lv.isOf(Blocks.BIG_DRIPLEAF)) {
            return PathNodeType.TRAPDOOR;
        }
        if (lv.isOf(Blocks.POWDER_SNOW)) {
            return PathNodeType.POWDER_SNOW;
        }
        if (lv.isOf(Blocks.CACTUS) || lv.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DAMAGE_OTHER;
        }
        if (lv.isOf(Blocks.HONEY_BLOCK)) {
            return PathNodeType.STICKY_HONEY;
        }
        if (lv.isOf(Blocks.COCOA)) {
            return PathNodeType.COCOA;
        }
        FluidState lv4 = world.getFluidState(pos);
        if (lv4.isIn(FluidTags.LAVA)) {
            return PathNodeType.LAVA;
        }
        if (LandPathNodeMaker.inflictsFireDamage(lv)) {
            return PathNodeType.DAMAGE_FIRE;
        }
        if (DoorBlock.isWoodenDoor(lv) && !lv.get(DoorBlock.OPEN).booleanValue()) {
            return PathNodeType.DOOR_WOOD_CLOSED;
        }
        if (lv2 instanceof DoorBlock && lv3 == Material.METAL && !lv.get(DoorBlock.OPEN).booleanValue()) {
            return PathNodeType.DOOR_IRON_CLOSED;
        }
        if (lv2 instanceof DoorBlock && lv.get(DoorBlock.OPEN).booleanValue()) {
            return PathNodeType.DOOR_OPEN;
        }
        if (lv2 instanceof AbstractRailBlock) {
            return PathNodeType.RAIL;
        }
        if (lv2 instanceof LeavesBlock) {
            return PathNodeType.LEAVES;
        }
        if (lv.isIn(BlockTags.FENCES) || lv.isIn(BlockTags.WALLS) || lv2 instanceof FenceGateBlock && !lv.get(FenceGateBlock.OPEN).booleanValue()) {
            return PathNodeType.FENCE;
        }
        if (!lv.canPathfindThrough(world, pos, NavigationType.LAND)) {
            return PathNodeType.BLOCKED;
        }
        if (lv4.isIn(FluidTags.WATER)) {
            return PathNodeType.WATER;
        }
        return PathNodeType.OPEN;
    }

    public static boolean inflictsFireDamage(BlockState state) {
        return state.isIn(BlockTags.FIRE) || state.isOf(Blocks.LAVA) || state.isOf(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state) || state.isOf(Blocks.LAVA_CAULDRON);
    }
}

