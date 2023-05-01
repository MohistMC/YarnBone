/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;

public interface CraftingRecipe
extends Recipe<CraftingInventory> {
    @Override
    default public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public CraftingRecipeCategory getCategory();
}

