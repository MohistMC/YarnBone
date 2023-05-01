/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BookCloningRecipe
extends SpecialCraftingRecipe {
    public BookCloningRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        int i = 0;
        ItemStack lv = ItemStack.EMPTY;
        for (int j = 0; j < arg.size(); ++j) {
            ItemStack lv2 = arg.getStack(j);
            if (lv2.isEmpty()) continue;
            if (lv2.isOf(Items.WRITTEN_BOOK)) {
                if (!lv.isEmpty()) {
                    return false;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.WRITABLE_BOOK)) {
                ++i;
                continue;
            }
            return false;
        }
        return !lv.isEmpty() && lv.hasNbt() && i > 0;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        int i = 0;
        ItemStack lv = ItemStack.EMPTY;
        for (int j = 0; j < arg.size(); ++j) {
            ItemStack lv2 = arg.getStack(j);
            if (lv2.isEmpty()) continue;
            if (lv2.isOf(Items.WRITTEN_BOOK)) {
                if (!lv.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.WRITABLE_BOOK)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (lv.isEmpty() || !lv.hasNbt() || i < 1 || WrittenBookItem.getGeneration(lv) >= 2) {
            return ItemStack.EMPTY;
        }
        ItemStack lv3 = new ItemStack(Items.WRITTEN_BOOK, i);
        NbtCompound lv4 = lv.getNbt().copy();
        lv4.putInt("generation", WrittenBookItem.getGeneration(lv) + 1);
        lv3.setNbt(lv4);
        return lv3;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingInventory arg) {
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(arg.size(), ItemStack.EMPTY);
        for (int i = 0; i < lv.size(); ++i) {
            ItemStack lv2 = arg.getStack(i);
            if (lv2.getItem().hasRecipeRemainder()) {
                lv.set(i, new ItemStack(lv2.getItem().getRecipeRemainder()));
                continue;
            }
            if (!(lv2.getItem() instanceof WrittenBookItem)) continue;
            ItemStack lv3 = lv2.copy();
            lv3.setCount(1);
            lv.set(i, lv3);
            break;
        }
        return lv;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }
}

