/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public abstract class ViewerCountManager {
    private static final int SCHEDULE_TICK_DELAY = 5;
    private int viewerCount;

    protected abstract void onContainerOpen(World var1, BlockPos var2, BlockState var3);

    protected abstract void onContainerClose(World var1, BlockPos var2, BlockState var3);

    protected abstract void onViewerCountUpdate(World var1, BlockPos var2, BlockState var3, int var4, int var5);

    protected abstract boolean isPlayerViewing(PlayerEntity var1);

    public void openContainer(PlayerEntity player, World world, BlockPos pos, BlockState state) {
        int i;
        if ((i = this.viewerCount++) == 0) {
            this.onContainerOpen(world, pos, state);
            world.emitGameEvent((Entity)player, GameEvent.CONTAINER_OPEN, pos);
            ViewerCountManager.scheduleBlockTick(world, pos, state);
        }
        this.onViewerCountUpdate(world, pos, state, i, this.viewerCount);
    }

    public void closeContainer(PlayerEntity player, World world, BlockPos pos, BlockState state) {
        int i = this.viewerCount--;
        if (this.viewerCount == 0) {
            this.onContainerClose(world, pos, state);
            world.emitGameEvent((Entity)player, GameEvent.CONTAINER_CLOSE, pos);
        }
        this.onViewerCountUpdate(world, pos, state, i, this.viewerCount);
    }

    private int getInRangeViewerCount(World world, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        float f = 5.0f;
        Box lv = new Box((float)i - 5.0f, (float)j - 5.0f, (float)k - 5.0f, (float)(i + 1) + 5.0f, (float)(j + 1) + 5.0f, (float)(k + 1) + 5.0f);
        return world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), lv, this::isPlayerViewing).size();
    }

    public void updateViewerCount(World world, BlockPos pos, BlockState state) {
        int j = this.viewerCount;
        int i = this.getInRangeViewerCount(world, pos);
        if (j != i) {
            boolean bl2;
            boolean bl = i != 0;
            boolean bl3 = bl2 = j != 0;
            if (bl && !bl2) {
                this.onContainerOpen(world, pos, state);
                world.emitGameEvent(null, GameEvent.CONTAINER_OPEN, pos);
            } else if (!bl) {
                this.onContainerClose(world, pos, state);
                world.emitGameEvent(null, GameEvent.CONTAINER_CLOSE, pos);
            }
            this.viewerCount = i;
        }
        this.onViewerCountUpdate(world, pos, state, j, i);
        if (i > 0) {
            ViewerCountManager.scheduleBlockTick(world, pos, state);
        }
    }

    public int getViewerCount() {
        return this.viewerCount;
    }

    private static void scheduleBlockTick(World world, BlockPos pos, BlockState state) {
        world.scheduleBlockTick(pos, state.getBlock(), 5);
    }
}

