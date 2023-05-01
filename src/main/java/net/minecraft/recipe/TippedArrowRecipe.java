/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TippedArrowRecipe
extends SpecialCraftingRecipe {
    public TippedArrowRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        if (arg.getWidth() != 3 || arg.getHeight() != 3) {
            return false;
        }
        for (int i = 0; i < arg.getWidth(); ++i) {
            for (int j = 0; j < arg.getHeight(); ++j) {
                ItemStack lv = arg.getStack(i + j * arg.getWidth());
                if (lv.isEmpty()) {
                    return false;
                }
                if (!(i == 1 && j == 1 ? !lv.isOf(Items.LINGERING_POTION) : !lv.isOf(Items.ARROW))) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        ItemStack lv = arg.getStack(1 + arg.getWidth());
        if (!lv.isOf(Items.LINGERING_POTION)) {
            return ItemStack.EMPTY;
        }
        ItemStack lv2 = new ItemStack(Items.TIPPED_ARROW, 8);
        PotionUtil.setPotion(lv2, PotionUtil.getPotion(lv));
        PotionUtil.setCustomPotionEffects(lv2, PotionUtil.getCustomPotionEffects(lv));
        return lv2;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.TIPPED_ARROW;
    }
}

