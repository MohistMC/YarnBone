/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.SeekSkyTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import org.jetbrains.annotations.Nullable;

public class CelebrateRaidWinTask
extends MultiTickTask<VillagerEntity> {
    @Nullable
    private Raid raid;

    public CelebrateRaidWinTask(int minRunTime, int maxRunTime) {
        super(ImmutableMap.of(), minRunTime, maxRunTime);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        BlockPos lv = arg2.getBlockPos();
        this.raid = arg.getRaidAt(lv);
        return this.raid != null && this.raid.hasWon() && SeekSkyTask.isSkyVisible(arg, arg2, lv);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return this.raid != null && !this.raid.hasStopped();
    }

    @Override
    protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        this.raid = null;
        arg2.getBrain().refreshActivities(arg.getTimeOfDay(), arg.getTime());
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        Random lv = arg2.getRandom();
        if (lv.nextInt(100) == 0) {
            arg2.playCelebrateSound();
        }
        if (lv.nextInt(200) == 0 && SeekSkyTask.isSkyVisible(arg, arg2, arg2.getBlockPos())) {
            DyeColor lv2 = Util.getRandom(DyeColor.values(), lv);
            int i = lv.nextInt(3);
            ItemStack lv3 = this.createFirework(lv2, i);
            FireworkRocketEntity lv4 = new FireworkRocketEntity(arg2.world, arg2, arg2.getX(), arg2.getEyeY(), arg2.getZ(), lv3);
            arg2.world.spawnEntity(lv4);
        }
    }

    private ItemStack createFirework(DyeColor color, int flight) {
        ItemStack lv = new ItemStack(Items.FIREWORK_ROCKET, 1);
        ItemStack lv2 = new ItemStack(Items.FIREWORK_STAR);
        NbtCompound lv3 = lv2.getOrCreateSubNbt("Explosion");
        ArrayList<Integer> list = Lists.newArrayList();
        list.add(color.getFireworkColor());
        lv3.putIntArray("Colors", list);
        lv3.putByte("Type", (byte)FireworkRocketItem.Type.BURST.getId());
        NbtCompound lv4 = lv.getOrCreateSubNbt("Fireworks");
        NbtList lv5 = new NbtList();
        NbtCompound lv6 = lv2.getSubNbt("Explosion");
        if (lv6 != null) {
            lv5.add(lv6);
        }
        lv4.putByte("Flight", (byte)flight);
        if (!lv5.isEmpty()) {
            lv4.put("Explosions", lv5);
        }
        return lv;
    }
}

