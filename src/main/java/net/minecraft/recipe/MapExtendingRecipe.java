/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class MapExtendingRecipe
extends ShapedRecipe {
    public MapExtendingRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, "", category, 3, 3, DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.FILLED_MAP), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER)), new ItemStack(Items.MAP));
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        if (!super.matches(arg, arg2)) {
            return false;
        }
        ItemStack lv = ItemStack.EMPTY;
        for (int i = 0; i < arg.size() && lv.isEmpty(); ++i) {
            ItemStack lv2 = arg.getStack(i);
            if (!lv2.isOf(Items.FILLED_MAP)) continue;
            lv = lv2;
        }
        if (lv.isEmpty()) {
            return false;
        }
        MapState lv3 = FilledMapItem.getMapState(lv, arg2);
        if (lv3 == null) {
            return false;
        }
        if (lv3.hasMonumentIcon()) {
            return false;
        }
        return lv3.scale < 4;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        ItemStack lv = ItemStack.EMPTY;
        for (int i = 0; i < arg.size() && lv.isEmpty(); ++i) {
            ItemStack lv2 = arg.getStack(i);
            if (!lv2.isOf(Items.FILLED_MAP)) continue;
            lv = lv2;
        }
        lv = lv.copy();
        lv.setCount(1);
        lv.getOrCreateNbt().putInt("map_scale_direction", 1);
        return lv;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_EXTENDING;
    }
}

