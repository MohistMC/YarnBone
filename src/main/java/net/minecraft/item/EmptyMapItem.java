/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NetworkSyncedItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EmptyMapItem
extends NetworkSyncedItem {
    public EmptyMapItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(lv);
        }
        if (!user.getAbilities().creativeMode) {
            lv.decrement(1);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        user.world.playSoundFromEntity(null, user, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, user.getSoundCategory(), 1.0f, 1.0f);
        ItemStack lv2 = FilledMapItem.createMap(world, user.getBlockX(), user.getBlockZ(), (byte)0, true, false);
        if (lv.isEmpty()) {
            return TypedActionResult.consume(lv2);
        }
        if (!user.getInventory().insertStack(lv2.copy())) {
            user.dropItem(lv2, false);
        }
        return TypedActionResult.consume(lv);
    }
}

