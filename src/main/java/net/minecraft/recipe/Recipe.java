/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public interface Recipe<C extends Inventory> {
    public boolean matches(C var1, World var2);

    public ItemStack craft(C var1, DynamicRegistryManager var2);

    public boolean fits(int var1, int var2);

    public ItemStack getOutput(DynamicRegistryManager var1);

    default public DefaultedList<ItemStack> getRemainder(C inventory) {
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < lv.size(); ++i) {
            Item lv2 = inventory.getStack(i).getItem();
            if (!lv2.hasRecipeRemainder()) continue;
            lv.set(i, new ItemStack(lv2.getRecipeRemainder()));
        }
        return lv;
    }

    default public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.of();
    }

    default public boolean isIgnoredInRecipeBook() {
        return false;
    }

    default public boolean showNotification() {
        return true;
    }

    default public String getGroup() {
        return "";
    }

    default public ItemStack createIcon() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }

    public Identifier getId();

    public RecipeSerializer<?> getSerializer();

    public RecipeType<?> getType();

    default public boolean isEmpty() {
        DefaultedList<Ingredient> lv = this.getIngredients();
        return lv.isEmpty() || lv.stream().anyMatch(ingredient -> ingredient.getMatchingStacks().length == 0);
    }
}

