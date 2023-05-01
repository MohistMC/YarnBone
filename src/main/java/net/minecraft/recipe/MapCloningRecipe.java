/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MapCloningRecipe
extends SpecialCraftingRecipe {
    public MapCloningRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        int i = 0;
        ItemStack lv = ItemStack.EMPTY;
        for (int j = 0; j < arg.size(); ++j) {
            ItemStack lv2 = arg.getStack(j);
            if (lv2.isEmpty()) continue;
            if (lv2.isOf(Items.FILLED_MAP)) {
                if (!lv.isEmpty()) {
                    return false;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.MAP)) {
                ++i;
                continue;
            }
            return false;
        }
        return !lv.isEmpty() && i > 0;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        int i = 0;
        ItemStack lv = ItemStack.EMPTY;
        for (int j = 0; j < arg.size(); ++j) {
            ItemStack lv2 = arg.getStack(j);
            if (lv2.isEmpty()) continue;
            if (lv2.isOf(Items.FILLED_MAP)) {
                if (!lv.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.MAP)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (lv.isEmpty() || i < 1) {
            return ItemStack.EMPTY;
        }
        ItemStack lv3 = lv.copy();
        lv3.setCount(i + 1);
        return lv3;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}

