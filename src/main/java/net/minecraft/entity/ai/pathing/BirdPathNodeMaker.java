/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class BirdPathNodeMaker
extends LandPathNodeMaker {
    private final Long2ObjectMap<PathNodeType> pathNodes = new Long2ObjectOpenHashMap<PathNodeType>();
    private static final float field_41681 = 1.5f;
    private static final int field_41682 = 10;

    @Override
    public void init(ChunkCache cachedWorld, MobEntity entity) {
        super.init(cachedWorld, entity);
        this.pathNodes.clear();
        this.waterPathNodeTypeWeight = entity.getPathfindingPenalty(PathNodeType.WATER);
    }

    @Override
    public void clear() {
        this.entity.setPathfindingPenalty(PathNodeType.WATER, this.waterPathNodeTypeWeight);
        this.pathNodes.clear();
        super.clear();
    }

    @Override
    public PathNode getStart() {
        BlockPos lv3;
        int i;
        if (this.canSwim() && this.entity.isTouchingWater()) {
            i = this.entity.getBlockY();
            BlockPos.Mutable lv = new BlockPos.Mutable(this.entity.getX(), (double)i, this.entity.getZ());
            BlockState lv2 = this.cachedWorld.getBlockState(lv);
            while (lv2.isOf(Blocks.WATER)) {
                lv.set(this.entity.getX(), (double)(++i), this.entity.getZ());
                lv2 = this.cachedWorld.getBlockState(lv);
            }
        } else {
            i = MathHelper.floor(this.entity.getY() + 0.5);
        }
        if (!this.canPathThrough(lv3 = BlockPos.ofFloored(this.entity.getX(), i, this.entity.getZ()))) {
            for (BlockPos lv4 : this.getPotentialEscapePositions(this.entity)) {
                if (!this.canPathThrough(lv4)) continue;
                return super.getStart(lv4);
            }
        }
        return super.getStart(lv3);
    }

    @Override
    protected boolean canPathThrough(BlockPos pos) {
        PathNodeType lv = this.getNodeType(this.entity, pos);
        return this.entity.getPathfindingPenalty(lv) >= 0.0f;
    }

    @Override
    public TargetPathNode getNode(double x, double y, double z) {
        return this.asTargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        PathNode lv26;
        PathNode lv25;
        PathNode lv24;
        PathNode lv23;
        PathNode lv22;
        PathNode lv21;
        PathNode lv20;
        PathNode lv19;
        PathNode lv18;
        PathNode lv17;
        PathNode lv16;
        PathNode lv15;
        PathNode lv14;
        PathNode lv13;
        PathNode lv12;
        PathNode lv11;
        PathNode lv10;
        PathNode lv9;
        PathNode lv8;
        PathNode lv7;
        PathNode lv6;
        PathNode lv5;
        PathNode lv4;
        PathNode lv3;
        PathNode lv2;
        int i = 0;
        PathNode lv = this.getPassableNode(node.x, node.y, node.z + 1);
        if (this.unvisited(lv)) {
            successors[i++] = lv;
        }
        if (this.unvisited(lv2 = this.getPassableNode(node.x - 1, node.y, node.z))) {
            successors[i++] = lv2;
        }
        if (this.unvisited(lv3 = this.getPassableNode(node.x + 1, node.y, node.z))) {
            successors[i++] = lv3;
        }
        if (this.unvisited(lv4 = this.getPassableNode(node.x, node.y, node.z - 1))) {
            successors[i++] = lv4;
        }
        if (this.unvisited(lv5 = this.getPassableNode(node.x, node.y + 1, node.z))) {
            successors[i++] = lv5;
        }
        if (this.unvisited(lv6 = this.getPassableNode(node.x, node.y - 1, node.z))) {
            successors[i++] = lv6;
        }
        if (this.unvisited(lv7 = this.getPassableNode(node.x, node.y + 1, node.z + 1)) && this.isPassable(lv) && this.isPassable(lv5)) {
            successors[i++] = lv7;
        }
        if (this.unvisited(lv8 = this.getPassableNode(node.x - 1, node.y + 1, node.z)) && this.isPassable(lv2) && this.isPassable(lv5)) {
            successors[i++] = lv8;
        }
        if (this.unvisited(lv9 = this.getPassableNode(node.x + 1, node.y + 1, node.z)) && this.isPassable(lv3) && this.isPassable(lv5)) {
            successors[i++] = lv9;
        }
        if (this.unvisited(lv10 = this.getPassableNode(node.x, node.y + 1, node.z - 1)) && this.isPassable(lv4) && this.isPassable(lv5)) {
            successors[i++] = lv10;
        }
        if (this.unvisited(lv11 = this.getPassableNode(node.x, node.y - 1, node.z + 1)) && this.isPassable(lv) && this.isPassable(lv6)) {
            successors[i++] = lv11;
        }
        if (this.unvisited(lv12 = this.getPassableNode(node.x - 1, node.y - 1, node.z)) && this.isPassable(lv2) && this.isPassable(lv6)) {
            successors[i++] = lv12;
        }
        if (this.unvisited(lv13 = this.getPassableNode(node.x + 1, node.y - 1, node.z)) && this.isPassable(lv3) && this.isPassable(lv6)) {
            successors[i++] = lv13;
        }
        if (this.unvisited(lv14 = this.getPassableNode(node.x, node.y - 1, node.z - 1)) && this.isPassable(lv4) && this.isPassable(lv6)) {
            successors[i++] = lv14;
        }
        if (this.unvisited(lv15 = this.getPassableNode(node.x + 1, node.y, node.z - 1)) && this.isPassable(lv4) && this.isPassable(lv3)) {
            successors[i++] = lv15;
        }
        if (this.unvisited(lv16 = this.getPassableNode(node.x + 1, node.y, node.z + 1)) && this.isPassable(lv) && this.isPassable(lv3)) {
            successors[i++] = lv16;
        }
        if (this.unvisited(lv17 = this.getPassableNode(node.x - 1, node.y, node.z - 1)) && this.isPassable(lv4) && this.isPassable(lv2)) {
            successors[i++] = lv17;
        }
        if (this.unvisited(lv18 = this.getPassableNode(node.x - 1, node.y, node.z + 1)) && this.isPassable(lv) && this.isPassable(lv2)) {
            successors[i++] = lv18;
        }
        if (this.unvisited(lv19 = this.getPassableNode(node.x + 1, node.y + 1, node.z - 1)) && this.isPassable(lv15) && this.isPassable(lv4) && this.isPassable(lv3) && this.isPassable(lv5) && this.isPassable(lv10) && this.isPassable(lv9)) {
            successors[i++] = lv19;
        }
        if (this.unvisited(lv20 = this.getPassableNode(node.x + 1, node.y + 1, node.z + 1)) && this.isPassable(lv16) && this.isPassable(lv) && this.isPassable(lv3) && this.isPassable(lv5) && this.isPassable(lv7) && this.isPassable(lv9)) {
            successors[i++] = lv20;
        }
        if (this.unvisited(lv21 = this.getPassableNode(node.x - 1, node.y + 1, node.z - 1)) && this.isPassable(lv17) && this.isPassable(lv4) && this.isPassable(lv2) && this.isPassable(lv5) && this.isPassable(lv10) && this.isPassable(lv8)) {
            successors[i++] = lv21;
        }
        if (this.unvisited(lv22 = this.getPassableNode(node.x - 1, node.y + 1, node.z + 1)) && this.isPassable(lv18) && this.isPassable(lv) && this.isPassable(lv2) && this.isPassable(lv5) && this.isPassable(lv7) && this.isPassable(lv8)) {
            successors[i++] = lv22;
        }
        if (this.unvisited(lv23 = this.getPassableNode(node.x + 1, node.y - 1, node.z - 1)) && this.isPassable(lv15) && this.isPassable(lv4) && this.isPassable(lv3) && this.isPassable(lv6) && this.isPassable(lv14) && this.isPassable(lv13)) {
            successors[i++] = lv23;
        }
        if (this.unvisited(lv24 = this.getPassableNode(node.x + 1, node.y - 1, node.z + 1)) && this.isPassable(lv16) && this.isPassable(lv) && this.isPassable(lv3) && this.isPassable(lv6) && this.isPassable(lv11) && this.isPassable(lv13)) {
            successors[i++] = lv24;
        }
        if (this.unvisited(lv25 = this.getPassableNode(node.x - 1, node.y - 1, node.z - 1)) && this.isPassable(lv17) && this.isPassable(lv4) && this.isPassable(lv2) && this.isPassable(lv6) && this.isPassable(lv14) && this.isPassable(lv12)) {
            successors[i++] = lv25;
        }
        if (this.unvisited(lv26 = this.getPassableNode(node.x - 1, node.y - 1, node.z + 1)) && this.isPassable(lv18) && this.isPassable(lv) && this.isPassable(lv2) && this.isPassable(lv6) && this.isPassable(lv11) && this.isPassable(lv12)) {
            successors[i++] = lv26;
        }
        return i;
    }

    private boolean isPassable(@Nullable PathNode node) {
        return node != null && node.penalty >= 0.0f;
    }

    private boolean unvisited(@Nullable PathNode node) {
        return node != null && !node.visited;
    }

    @Nullable
    protected PathNode getPassableNode(int x, int y, int z) {
        PathNode lv = null;
        PathNodeType lv2 = this.getNodeType(x, y, z);
        float f = this.entity.getPathfindingPenalty(lv2);
        if (f >= 0.0f) {
            lv = this.getNode(x, y, z);
            lv.type = lv2;
            lv.penalty = Math.max(lv.penalty, f);
            if (lv2 == PathNodeType.WALKABLE) {
                lv.penalty += 1.0f;
            }
        }
        return lv;
    }

    private PathNodeType getNodeType(int x, int y, int z) {
        return this.pathNodes.computeIfAbsent(BlockPos.asLong(x, y, z), pos -> this.getNodeType(this.cachedWorld, x, y, z, this.entity));
    }

    @Override
    public PathNodeType getNodeType(BlockView world, int x, int y, int z, MobEntity mob) {
        EnumSet<PathNodeType> enumSet = EnumSet.noneOf(PathNodeType.class);
        PathNodeType lv = PathNodeType.BLOCKED;
        BlockPos lv2 = mob.getBlockPos();
        lv = super.findNearbyNodeTypes(world, x, y, z, enumSet, lv, lv2);
        if (enumSet.contains((Object)PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        }
        PathNodeType lv3 = PathNodeType.BLOCKED;
        for (PathNodeType lv4 : enumSet) {
            if (mob.getPathfindingPenalty(lv4) < 0.0f) {
                return lv4;
            }
            if (!(mob.getPathfindingPenalty(lv4) >= mob.getPathfindingPenalty(lv3))) continue;
            lv3 = lv4;
        }
        if (lv == PathNodeType.OPEN && mob.getPathfindingPenalty(lv3) == 0.0f) {
            return PathNodeType.OPEN;
        }
        return lv3;
    }

    @Override
    public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        PathNodeType lv2 = BirdPathNodeMaker.getCommonNodeType(world, lv.set(x, y, z));
        if (lv2 == PathNodeType.OPEN && y >= world.getBottomY() + 1) {
            PathNodeType lv3 = BirdPathNodeMaker.getCommonNodeType(world, lv.set(x, y - 1, z));
            if (lv3 == PathNodeType.DAMAGE_FIRE || lv3 == PathNodeType.LAVA) {
                lv2 = PathNodeType.DAMAGE_FIRE;
            } else if (lv3 == PathNodeType.DAMAGE_OTHER) {
                lv2 = PathNodeType.DAMAGE_OTHER;
            } else if (lv3 == PathNodeType.COCOA) {
                lv2 = PathNodeType.COCOA;
            } else if (lv3 == PathNodeType.FENCE) {
                if (!lv.equals(this.entity.getBlockPos())) {
                    lv2 = PathNodeType.FENCE;
                }
            } else {
                PathNodeType pathNodeType = lv2 = lv3 == PathNodeType.WALKABLE || lv3 == PathNodeType.OPEN || lv3 == PathNodeType.WATER ? PathNodeType.OPEN : PathNodeType.WALKABLE;
            }
        }
        if (lv2 == PathNodeType.WALKABLE || lv2 == PathNodeType.OPEN) {
            lv2 = BirdPathNodeMaker.getNodeTypeFromNeighbors(world, lv.set(x, y, z), lv2);
        }
        return lv2;
    }

    private Iterable<BlockPos> getPotentialEscapePositions(MobEntity entity) {
        boolean bl;
        float f = 1.0f;
        Box lv = entity.getBoundingBox();
        boolean bl2 = bl = lv.getAverageSideLength() < 1.0;
        if (!bl) {
            return List.of(BlockPos.ofFloored(lv.minX, entity.getBlockY(), lv.minZ), BlockPos.ofFloored(lv.minX, entity.getBlockY(), lv.maxZ), BlockPos.ofFloored(lv.maxX, entity.getBlockY(), lv.minZ), BlockPos.ofFloored(lv.maxX, entity.getBlockY(), lv.maxZ));
        }
        double d = Math.max(0.0, (1.5 - lv.getZLength()) / 2.0);
        double e = Math.max(0.0, (1.5 - lv.getXLength()) / 2.0);
        double g = Math.max(0.0, (1.5 - lv.getYLength()) / 2.0);
        Box lv2 = lv.expand(e, g, d);
        return BlockPos.iterateRandomly(entity.getRandom(), 10, MathHelper.floor(lv2.minX), MathHelper.floor(lv2.minY), MathHelper.floor(lv2.minZ), MathHelper.floor(lv2.maxX), MathHelper.floor(lv2.maxY), MathHelper.floor(lv2.maxZ));
    }
}

