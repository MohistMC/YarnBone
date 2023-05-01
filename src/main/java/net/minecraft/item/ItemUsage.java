/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import java.util.stream.Stream;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemUsage {
    public static TypedActionResult<ItemStack> consumeHeldItem(World world, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    public static ItemStack exchangeStack(ItemStack inputStack, PlayerEntity player, ItemStack outputStack, boolean creativeOverride) {
        boolean bl2 = player.getAbilities().creativeMode;
        if (creativeOverride && bl2) {
            if (!player.getInventory().contains(outputStack)) {
                player.getInventory().insertStack(outputStack);
            }
            return inputStack;
        }
        if (!bl2) {
            inputStack.decrement(1);
        }
        if (inputStack.isEmpty()) {
            return outputStack;
        }
        if (!player.getInventory().insertStack(outputStack)) {
            player.dropItem(outputStack, false);
        }
        return inputStack;
    }

    public static ItemStack exchangeStack(ItemStack inputStack, PlayerEntity player, ItemStack outputStack) {
        return ItemUsage.exchangeStack(inputStack, player, outputStack, true);
    }

    public static void spawnItemContents(ItemEntity itemEntity, Stream<ItemStack> contents) {
        World lv = itemEntity.world;
        if (lv.isClient) {
            return;
        }
        contents.forEach(stack -> lv.spawnEntity(new ItemEntity(lv, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), (ItemStack)stack)));
    }
}

