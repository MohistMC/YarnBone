/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractRecipeScreenHandler<C extends Inventory>
extends ScreenHandler {
    public AbstractRecipeScreenHandler(ScreenHandlerType<?> arg, int i) {
        super(arg, i);
    }

    public void fillInputSlots(boolean craftAll, Recipe<?> recipe, ServerPlayerEntity player) {
        new InputSlotFiller(this).fillInputSlots(player, recipe, craftAll);
    }

    public abstract void populateRecipeFinder(RecipeMatcher var1);

    public abstract void clearCraftingSlots();

    public abstract boolean matches(Recipe<? super C> var1);

    public abstract int getCraftingResultSlotIndex();

    public abstract int getCraftingWidth();

    public abstract int getCraftingHeight();

    public abstract int getCraftingSlotCount();

    public abstract RecipeBookCategory getCategory();

    public abstract boolean canInsertIntoSlot(int var1);
}

