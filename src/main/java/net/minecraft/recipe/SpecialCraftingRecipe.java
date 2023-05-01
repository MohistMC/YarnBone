/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public abstract class SpecialCraftingRecipe
implements CraftingRecipe {
    private final Identifier id;
    private final CraftingRecipeCategory category;

    public SpecialCraftingRecipe(Identifier id, CraftingRecipeCategory category) {
        this.id = id;
        this.category = category;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return this.category;
    }
}

