/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ArmorDyeRecipe
extends SpecialCraftingRecipe {
    public ArmorDyeRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        ItemStack lv = ItemStack.EMPTY;
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv2 = arg.getStack(i);
            if (lv2.isEmpty()) continue;
            if (lv2.getItem() instanceof DyeableItem) {
                if (!lv.isEmpty()) {
                    return false;
                }
                lv = lv2;
                continue;
            }
            if (lv2.getItem() instanceof DyeItem) {
                list.add(lv2);
                continue;
            }
            return false;
        }
        return !lv.isEmpty() && !list.isEmpty();
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        ArrayList<DyeItem> list = Lists.newArrayList();
        ItemStack lv = ItemStack.EMPTY;
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv2 = arg.getStack(i);
            if (lv2.isEmpty()) continue;
            Item lv3 = lv2.getItem();
            if (lv3 instanceof DyeableItem) {
                if (!lv.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                lv = lv2.copy();
                continue;
            }
            if (lv3 instanceof DyeItem) {
                list.add((DyeItem)lv3);
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (lv.isEmpty() || list.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return DyeableItem.blendAndSetColor(lv, list);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.ARMOR_DYE;
    }
}

