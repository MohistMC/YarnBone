/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class StewItem
extends Item {
    public StewItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack lv = super.finishUsing(stack, world, user);
        if (user instanceof PlayerEntity && ((PlayerEntity)user).getAbilities().creativeMode) {
            return lv;
        }
        return new ItemStack(Items.BOWL);
    }
}

