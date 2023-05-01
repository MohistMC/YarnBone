/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.screen;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class BrewingStandScreenHandler
extends ScreenHandler {
    private static final int field_30763 = 0;
    private static final int field_30764 = 2;
    private static final int field_30765 = 3;
    private static final int field_30766 = 4;
    private static final int field_30767 = 5;
    private static final int field_30768 = 2;
    private static final int field_30769 = 5;
    private static final int field_30770 = 32;
    private static final int field_30771 = 32;
    private static final int field_30772 = 41;
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final Slot ingredientSlot;

    public BrewingStandScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(5), new ArrayPropertyDelegate(2));
    }

    public BrewingStandScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerType.BREWING_STAND, syncId);
        int j;
        BrewingStandScreenHandler.checkSize(inventory, 5);
        BrewingStandScreenHandler.checkDataCount(propertyDelegate, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.addSlot(new PotionSlot(inventory, 0, 56, 51));
        this.addSlot(new PotionSlot(inventory, 1, 79, 58));
        this.addSlot(new PotionSlot(inventory, 2, 102, 51));
        this.ingredientSlot = this.addSlot(new IngredientSlot(inventory, 3, 79, 17));
        this.addSlot(new FuelSlot(inventory, 4, 17, 17));
        this.addProperties(propertyDelegate);
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot >= 0 && slot <= 2 || slot == 3 || slot == 4) {
                if (!this.insertItem(lv3, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (FuelSlot.matches(lv) ? this.insertItem(lv3, 4, 5, false) || this.ingredientSlot.canInsert(lv3) && !this.insertItem(lv3, 3, 4, false) : (this.ingredientSlot.canInsert(lv3) ? !this.insertItem(lv3, 3, 4, false) : (PotionSlot.matches(lv) && lv.getCount() == 1 ? !this.insertItem(lv3, 0, 3, false) : (slot >= 5 && slot < 32 ? !this.insertItem(lv3, 32, 41, false) : (slot >= 32 && slot < 41 ? !this.insertItem(lv3, 5, 32, false) : !this.insertItem(lv3, 5, 41, false)))))) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            } else {
                lv2.markDirty();
            }
            if (lv3.getCount() == lv.getCount()) {
                return ItemStack.EMPTY;
            }
            lv2.onTakeItem(player, lv3);
        }
        return lv;
    }

    public int getFuel() {
        return this.propertyDelegate.get(1);
    }

    public int getBrewTime() {
        return this.propertyDelegate.get(0);
    }

    static class PotionSlot
    extends Slot {
        public PotionSlot(Inventory arg, int i, int j, int k) {
            super(arg, i, j, k);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return PotionSlot.matches(stack);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            Potion lv = PotionUtil.getPotion(stack);
            if (player instanceof ServerPlayerEntity) {
                Criteria.BREWED_POTION.trigger((ServerPlayerEntity)player, lv);
            }
            super.onTakeItem(player, stack);
        }

        public static boolean matches(ItemStack stack) {
            return stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION) || stack.isOf(Items.GLASS_BOTTLE);
        }
    }

    static class IngredientSlot
    extends Slot {
        public IngredientSlot(Inventory arg, int i, int j, int k) {
            super(arg, i, j, k);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return BrewingRecipeRegistry.isValidIngredient(stack);
        }

        @Override
        public int getMaxItemCount() {
            return 64;
        }
    }

    static class FuelSlot
    extends Slot {
        public FuelSlot(Inventory arg, int i, int j, int k) {
            super(arg, i, j, k);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return FuelSlot.matches(stack);
        }

        public static boolean matches(ItemStack stack) {
            return stack.isOf(Items.BLAZE_POWDER);
        }

        @Override
        public int getMaxItemCount() {
            return 64;
        }
    }
}

