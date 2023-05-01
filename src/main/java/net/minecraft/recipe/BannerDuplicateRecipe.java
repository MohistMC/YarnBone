/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BannerDuplicateRecipe
extends SpecialCraftingRecipe {
    public BannerDuplicateRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        DyeColor lv = null;
        ItemStack lv2 = null;
        ItemStack lv3 = null;
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv4 = arg.getStack(i);
            if (lv4.isEmpty()) continue;
            Item lv5 = lv4.getItem();
            if (!(lv5 instanceof BannerItem)) {
                return false;
            }
            BannerItem lv6 = (BannerItem)lv5;
            if (lv == null) {
                lv = lv6.getColor();
            } else if (lv != lv6.getColor()) {
                return false;
            }
            int j = BannerBlockEntity.getPatternCount(lv4);
            if (j > 6) {
                return false;
            }
            if (j > 0) {
                if (lv2 == null) {
                    lv2 = lv4;
                    continue;
                }
                return false;
            }
            if (lv3 == null) {
                lv3 = lv4;
                continue;
            }
            return false;
        }
        return lv2 != null && lv3 != null;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        for (int i = 0; i < arg.size(); ++i) {
            int j;
            ItemStack lv = arg.getStack(i);
            if (lv.isEmpty() || (j = BannerBlockEntity.getPatternCount(lv)) <= 0 || j > 6) continue;
            ItemStack lv2 = lv.copy();
            lv2.setCount(1);
            return lv2;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingInventory arg) {
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(arg.size(), ItemStack.EMPTY);
        for (int i = 0; i < lv.size(); ++i) {
            ItemStack lv2 = arg.getStack(i);
            if (lv2.isEmpty()) continue;
            if (lv2.getItem().hasRecipeRemainder()) {
                lv.set(i, new ItemStack(lv2.getItem().getRecipeRemainder()));
                continue;
            }
            if (!lv2.hasNbt() || BannerBlockEntity.getPatternCount(lv2) <= 0) continue;
            ItemStack lv3 = lv2.copy();
            lv3.setCount(1);
            lv.set(i, lv3);
        }
        return lv;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }
}

