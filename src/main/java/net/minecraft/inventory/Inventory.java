/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.inventory;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Inventory
extends Clearable {
    public static final int MAX_COUNT_PER_STACK = 64;
    public static final int field_42619 = 8;

    public int size();

    public boolean isEmpty();

    public ItemStack getStack(int var1);

    public ItemStack removeStack(int var1, int var2);

    public ItemStack removeStack(int var1);

    public void setStack(int var1, ItemStack var2);

    default public int getMaxCountPerStack() {
        return 64;
    }

    public void markDirty();

    public boolean canPlayerUse(PlayerEntity var1);

    default public void onOpen(PlayerEntity player) {
    }

    default public void onClose(PlayerEntity player) {
    }

    default public boolean isValid(int slot, ItemStack stack) {
        return true;
    }

    default public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return true;
    }

    default public int count(Item item) {
        int i = 0;
        for (int j = 0; j < this.size(); ++j) {
            ItemStack lv = this.getStack(j);
            if (!lv.getItem().equals(item)) continue;
            i += lv.getCount();
        }
        return i;
    }

    default public boolean containsAny(Set<Item> items) {
        return this.containsAny((ItemStack stack) -> !stack.isEmpty() && items.contains(stack.getItem()));
    }

    default public boolean containsAny(Predicate<ItemStack> predicate) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack lv = this.getStack(i);
            if (!predicate.test(lv)) continue;
            return true;
        }
        return false;
    }

    public static boolean canPlayerUse(BlockEntity blockEntity, PlayerEntity player) {
        return Inventory.canPlayerUse(blockEntity, player, 8);
    }

    public static boolean canPlayerUse(BlockEntity blockEntity, PlayerEntity player, int range) {
        World lv = blockEntity.getWorld();
        BlockPos lv2 = blockEntity.getPos();
        if (lv == null) {
            return false;
        }
        if (lv.getBlockEntity(lv2) != blockEntity) {
            return false;
        }
        return player.squaredDistanceTo((double)lv2.getX() + 0.5, (double)lv2.getY() + 0.5, (double)lv2.getZ() + 0.5) <= (double)(range * range);
    }
}

