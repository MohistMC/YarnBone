/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class SimpleInventory
implements Inventory,
RecipeInputProvider {
    private final int size;
    private final DefaultedList<ItemStack> stacks;
    @Nullable
    private List<InventoryChangedListener> listeners;

    public SimpleInventory(int size) {
        this.size = size;
        this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    public SimpleInventory(ItemStack ... items) {
        this.size = items.length;
        this.stacks = DefaultedList.copyOf(ItemStack.EMPTY, items);
    }

    public void addListener(InventoryChangedListener listener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }
        this.listeners.add(listener);
    }

    public void removeListener(InventoryChangedListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= this.stacks.size()) {
            return ItemStack.EMPTY;
        }
        return this.stacks.get(slot);
    }

    public List<ItemStack> clearToList() {
        List<ItemStack> list = this.stacks.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        this.clear();
        return list;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack lv = Inventories.splitStack(this.stacks, slot, amount);
        if (!lv.isEmpty()) {
            this.markDirty();
        }
        return lv;
    }

    public ItemStack removeItem(Item item, int count) {
        ItemStack lv = new ItemStack(item, 0);
        for (int j = this.size - 1; j >= 0; --j) {
            ItemStack lv2 = this.getStack(j);
            if (!lv2.getItem().equals(item)) continue;
            int k = count - lv.getCount();
            ItemStack lv3 = lv2.split(k);
            lv.increment(lv3.getCount());
            if (lv.getCount() == count) break;
        }
        if (!lv.isEmpty()) {
            this.markDirty();
        }
        return lv;
    }

    public ItemStack addStack(ItemStack stack) {
        ItemStack lv = stack.copy();
        this.addToExistingSlot(lv);
        if (lv.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.addToNewSlot(lv);
        if (lv.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return lv;
    }

    public boolean canInsert(ItemStack stack) {
        boolean bl = false;
        for (ItemStack lv : this.stacks) {
            if (!lv.isEmpty() && (!ItemStack.canCombine(lv, stack) || lv.getCount() >= lv.getMaxCount())) continue;
            bl = true;
            break;
        }
        return bl;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack lv = this.stacks.get(slot);
        if (lv.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.stacks.set(slot, ItemStack.EMPTY);
        return lv;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
        this.markDirty();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack lv : this.stacks) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void markDirty() {
        if (this.listeners != null) {
            for (InventoryChangedListener lv : this.listeners) {
                lv.onInventoryChanged(this);
            }
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
        this.markDirty();
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack lv : this.stacks) {
            finder.addInput(lv);
        }
    }

    public String toString() {
        return this.stacks.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList()).toString();
    }

    private void addToNewSlot(ItemStack stack) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack lv = this.getStack(i);
            if (!lv.isEmpty()) continue;
            this.setStack(i, stack.copy());
            stack.setCount(0);
            return;
        }
    }

    private void addToExistingSlot(ItemStack stack) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack lv = this.getStack(i);
            if (!ItemStack.canCombine(lv, stack)) continue;
            this.transfer(stack, lv);
            if (!stack.isEmpty()) continue;
            return;
        }
    }

    private void transfer(ItemStack source, ItemStack target) {
        int i = Math.min(this.getMaxCountPerStack(), target.getMaxCount());
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.increment(j);
            source.decrement(j);
            this.markDirty();
        }
    }

    public void readNbtList(NbtList nbtList) {
        this.clear();
        for (int i = 0; i < nbtList.size(); ++i) {
            ItemStack lv = ItemStack.fromNbt(nbtList.getCompound(i));
            if (lv.isEmpty()) continue;
            this.addStack(lv);
        }
    }

    public NbtList toNbtList() {
        NbtList lv = new NbtList();
        for (int i = 0; i < this.size(); ++i) {
            ItemStack lv2 = this.getStack(i);
            if (lv2.isEmpty()) continue;
            lv.add(lv2.writeNbt(new NbtCompound()));
        }
        return lv;
    }
}

