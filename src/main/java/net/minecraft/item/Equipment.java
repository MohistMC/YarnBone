/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Vanishable;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Equipment
extends Vanishable {
    public EquipmentSlot getSlotType();

    default public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    default public TypedActionResult<ItemStack> equipAndSwap(Item item, World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        EquipmentSlot lv2 = MobEntity.getPreferredEquipmentSlot(lv);
        ItemStack lv3 = user.getEquippedStack(lv2);
        if (EnchantmentHelper.hasBindingCurse(lv3) || ItemStack.areEqual(lv, lv3)) {
            return TypedActionResult.fail(lv);
        }
        user.equipStack(lv2, lv.copy());
        if (!world.isClient()) {
            user.incrementStat(Stats.USED.getOrCreateStat(item));
        }
        if (lv3.isEmpty()) {
            lv.setCount(0);
        } else {
            user.setStackInHand(hand, lv3.copy());
        }
        return TypedActionResult.success(lv, world.isClient());
    }

    @Nullable
    public static Equipment fromStack(ItemStack stack) {
        BlockItem lv2;
        Item item = stack.getItem();
        if (item instanceof Equipment) {
            Equipment lv = (Equipment)((Object)item);
            return lv;
        }
        ItemConvertible itemConvertible = stack.getItem();
        if (itemConvertible instanceof BlockItem && (itemConvertible = (lv2 = (BlockItem)itemConvertible).getBlock()) instanceof Equipment) {
            Equipment lv3 = (Equipment)((Object)itemConvertible);
            return lv3;
        }
        return null;
    }
}

