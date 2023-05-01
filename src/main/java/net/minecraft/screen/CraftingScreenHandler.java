/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.screen;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class CraftingScreenHandler
extends AbstractRecipeScreenHandler<CraftingInventory> {
    public static final int field_30781 = 0;
    private static final int field_30782 = 1;
    private static final int field_30783 = 10;
    private static final int field_30784 = 10;
    private static final int field_30785 = 37;
    private static final int field_30786 = 37;
    private static final int field_30787 = 46;
    private final CraftingInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();
    private final ScreenHandlerContext context;
    private final PlayerEntity player;

    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.CRAFTING, syncId);
        int k;
        int j;
        this.context = context;
        this.player = playerInventory.player;
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 3; ++k) {
                this.addSlot(new Slot(this.input, k + j * 3, 30 + k * 18, 17 + j * 18));
            }
        }
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
        ItemStack lv4;
        CraftingRecipe lv3;
        if (world.isClient) {
            return;
        }
        ServerPlayerEntity lv = (ServerPlayerEntity)player;
        ItemStack lv2 = ItemStack.EMPTY;
        Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
        if (optional.isPresent() && resultInventory.shouldCraftRecipe(world, lv, lv3 = optional.get()) && (lv4 = lv3.craft(craftingInventory, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures())) {
            lv2 = lv4;
        }
        resultInventory.setStack(0, lv2);
        handler.setPreviousTrackedSlot(0, lv2);
        lv.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, lv2));
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.context.run((world, pos) -> CraftingScreenHandler.updateResult(this, world, this.player, this.input, this.result));
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
    }

    @Override
    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return recipe.matches(this.input, this.player.world);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return CraftingScreenHandler.canUse(this.context, player, Blocks.CRAFTING_TABLE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == 0) {
                this.context.run((world, pos) -> lv3.getItem().onCraft(lv3, (World)world, player));
                if (!this.insertItem(lv3, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot >= 10 && slot < 46 ? !this.insertItem(lv3, 1, 10, false) && (slot < 37 ? !this.insertItem(lv3, 37, 46, false) : !this.insertItem(lv3, 10, 37, false)) : !this.insertItem(lv3, 10, 46, false)) {
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
            if (slot == 0) {
                player.dropItem(lv3, false);
            }
        }
        return lv;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.input.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }
}

