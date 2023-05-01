/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.ai.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MobNavigation
extends EntityNavigation {
    private boolean avoidSunlight;

    public MobNavigation(MobEntity arg, World arg2) {
        super(arg, arg2);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new LandPathNodeMaker();
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    @Override
    protected boolean isAtValidPosition() {
        return this.entity.isOnGround() || this.isInLiquid() || this.entity.hasVehicle();
    }

    @Override
    protected Vec3d getPos() {
        return new Vec3d(this.entity.getX(), this.getPathfindingY(), this.entity.getZ());
    }

    @Override
    public Path findPathTo(BlockPos target, int distance) {
        BlockPos lv;
        if (this.world.getBlockState(target).isAir()) {
            lv = target.down();
            while (lv.getY() > this.world.getBottomY() && this.world.getBlockState(lv).isAir()) {
                lv = lv.down();
            }
            if (lv.getY() > this.world.getBottomY()) {
                return super.findPathTo(lv.up(), distance);
            }
            while (lv.getY() < this.world.getTopY() && this.world.getBlockState(lv).isAir()) {
                lv = lv.up();
            }
            target = lv;
        }
        if (this.world.getBlockState(target).getMaterial().isSolid()) {
            lv = target.up();
            while (lv.getY() < this.world.getTopY() && this.world.getBlockState(lv).getMaterial().isSolid()) {
                lv = lv.up();
            }
            return super.findPathTo(lv, distance);
        }
        return super.findPathTo(target, distance);
    }

    @Override
    public Path findPathTo(Entity entity, int distance) {
        return this.findPathTo(entity.getBlockPos(), distance);
    }

    private int getPathfindingY() {
        if (!this.entity.isTouchingWater() || !this.canSwim()) {
            return MathHelper.floor(this.entity.getY() + 0.5);
        }
        int i = this.entity.getBlockY();
        BlockState lv = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), i, this.entity.getZ()));
        int j = 0;
        while (lv.isOf(Blocks.WATER)) {
            lv = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), ++i, this.entity.getZ()));
            if (++j <= 16) continue;
            return this.entity.getBlockY();
        }
        return i;
    }

    @Override
    protected void adjustPath() {
        super.adjustPath();
        if (this.avoidSunlight) {
            if (this.world.isSkyVisible(BlockPos.ofFloored(this.entity.getX(), this.entity.getY() + 0.5, this.entity.getZ()))) {
                return;
            }
            for (int i = 0; i < this.currentPath.getLength(); ++i) {
                PathNode lv = this.currentPath.getNode(i);
                if (!this.world.isSkyVisible(new BlockPos(lv.x, lv.y, lv.z))) continue;
                this.currentPath.setLength(i);
                return;
            }
        }
    }

    protected boolean canWalkOnPath(PathNodeType pathType) {
        if (pathType == PathNodeType.WATER) {
            return false;
        }
        if (pathType == PathNodeType.LAVA) {
            return false;
        }
        return pathType != PathNodeType.OPEN;
    }

    public void setCanPathThroughDoors(boolean canPathThroughDoors) {
        this.nodeMaker.setCanOpenDoors(canPathThroughDoors);
    }

    public boolean method_35140() {
        return this.nodeMaker.canEnterOpenDoors();
    }

    public void setCanEnterOpenDoors(boolean canEnterOpenDoors) {
        this.nodeMaker.setCanEnterOpenDoors(canEnterOpenDoors);
    }

    public boolean canEnterOpenDoors() {
        return this.nodeMaker.canEnterOpenDoors();
    }

    public void setAvoidSunlight(boolean avoidSunlight) {
        this.avoidSunlight = avoidSunlight;
    }

    public void setCanWalkOverFences(boolean canWalkOverFences) {
        this.nodeMaker.setCanWalkOverFences(canWalkOverFences);
    }
}

