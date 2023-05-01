/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class InputSlotFiller<C extends Inventory>
implements RecipeGridAligner<Integer> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final RecipeMatcher matcher = new RecipeMatcher();
    protected PlayerInventory inventory;
    protected AbstractRecipeScreenHandler<C> handler;

    public InputSlotFiller(AbstractRecipeScreenHandler<C> handler) {
        this.handler = handler;
    }

    public void fillInputSlots(ServerPlayerEntity entity, @Nullable Recipe<C> recipe, boolean craftAll) {
        if (recipe == null || !entity.getRecipeBook().contains(recipe)) {
            return;
        }
        this.inventory = entity.getInventory();
        if (!this.canReturnInputs() && !entity.isCreative()) {
            return;
        }
        this.matcher.clear();
        entity.getInventory().populateRecipeFinder(this.matcher);
        this.handler.populateRecipeFinder(this.matcher);
        if (this.matcher.match(recipe, null)) {
            this.fillInputSlots(recipe, craftAll);
        } else {
            this.returnInputs();
            entity.networkHandler.sendPacket(new CraftFailedResponseS2CPacket(entity.currentScreenHandler.syncId, recipe));
        }
        entity.getInventory().markDirty();
    }

    protected void returnInputs() {
        for (int i = 0; i < this.handler.getCraftingSlotCount(); ++i) {
            if (!this.handler.canInsertIntoSlot(i)) continue;
            ItemStack lv = this.handler.getSlot(i).getStack().copy();
            this.inventory.offer(lv, false);
            this.handler.getSlot(i).setStackNoCallbacks(lv);
        }
        this.handler.clearCraftingSlots();
    }

    protected void fillInputSlots(Recipe<C> recipe, boolean craftAll) {
        IntArrayList intList;
        int j;
        boolean bl2 = this.handler.matches(recipe);
        int i = this.matcher.countCrafts(recipe, null);
        if (bl2) {
            for (j = 0; j < this.handler.getCraftingHeight() * this.handler.getCraftingWidth() + 1; ++j) {
                ItemStack lv;
                if (j == this.handler.getCraftingResultSlotIndex() || (lv = this.handler.getSlot(j).getStack()).isEmpty() || Math.min(i, lv.getMaxCount()) >= lv.getCount() + 1) continue;
                return;
            }
        }
        if (this.matcher.match(recipe, intList = new IntArrayList(), j = this.getAmountToFill(craftAll, i, bl2))) {
            int k = j;
            IntListIterator intListIterator = intList.iterator();
            while (intListIterator.hasNext()) {
                int l = (Integer)intListIterator.next();
                int m = RecipeMatcher.getStackFromId(l).getMaxCount();
                if (m >= k) continue;
                k = m;
            }
            j = k;
            if (this.matcher.match(recipe, intList, j)) {
                this.returnInputs();
                this.alignRecipeToGrid(this.handler.getCraftingWidth(), this.handler.getCraftingHeight(), this.handler.getCraftingResultSlotIndex(), recipe, intList.iterator(), j);
            }
        }
    }

    @Override
    public void acceptAlignedInput(Iterator<Integer> inputs, int slot, int amount, int gridX, int gridY) {
        Slot lv = this.handler.getSlot(slot);
        ItemStack lv2 = RecipeMatcher.getStackFromId(inputs.next());
        if (!lv2.isEmpty()) {
            for (int m = 0; m < amount; ++m) {
                this.fillInputSlot(lv, lv2);
            }
        }
    }

    protected int getAmountToFill(boolean craftAll, int limit, boolean recipeInCraftingSlots) {
        int j = 1;
        if (craftAll) {
            j = limit;
        } else if (recipeInCraftingSlots) {
            j = 64;
            for (int k = 0; k < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++k) {
                ItemStack lv;
                if (k == this.handler.getCraftingResultSlotIndex() || (lv = this.handler.getSlot(k).getStack()).isEmpty() || j <= lv.getCount()) continue;
                j = lv.getCount();
            }
            if (j < 64) {
                ++j;
            }
        }
        return j;
    }

    protected void fillInputSlot(Slot slot, ItemStack stack) {
        int i = this.inventory.indexOf(stack);
        if (i == -1) {
            return;
        }
        ItemStack lv = this.inventory.getStack(i).copy();
        if (lv.isEmpty()) {
            return;
        }
        if (lv.getCount() > 1) {
            this.inventory.removeStack(i, 1);
        } else {
            this.inventory.removeStack(i);
        }
        lv.setCount(1);
        if (slot.getStack().isEmpty()) {
            slot.setStackNoCallbacks(lv);
        } else {
            slot.getStack().increment(1);
        }
    }

    private boolean canReturnInputs() {
        ArrayList<ItemStack> list = Lists.newArrayList();
        int i = this.getFreeInventorySlots();
        for (int j = 0; j < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++j) {
            ItemStack lv;
            if (j == this.handler.getCraftingResultSlotIndex() || (lv = this.handler.getSlot(j).getStack().copy()).isEmpty()) continue;
            int k = this.inventory.getOccupiedSlotWithRoomForStack(lv);
            if (k == -1 && list.size() <= i) {
                for (ItemStack lv2 : list) {
                    if (!lv2.isItemEqual(lv) || lv2.getCount() == lv2.getMaxCount() || lv2.getCount() + lv.getCount() > lv2.getMaxCount()) continue;
                    lv2.increment(lv.getCount());
                    lv.setCount(0);
                    break;
                }
                if (lv.isEmpty()) continue;
                if (list.size() < i) {
                    list.add(lv);
                    continue;
                }
                return false;
            }
            if (k != -1) continue;
            return false;
        }
        return true;
    }

    private int getFreeInventorySlots() {
        int i = 0;
        for (ItemStack lv : this.inventory.main) {
            if (!lv.isEmpty()) continue;
            ++i;
        }
        return i;
    }
}

