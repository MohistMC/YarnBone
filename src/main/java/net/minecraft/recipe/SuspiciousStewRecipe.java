/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SuspiciousStewRecipe
extends SpecialCraftingRecipe {
    public SuspiciousStewRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv = arg.getStack(i);
            if (lv.isEmpty()) continue;
            if (lv.isOf(Blocks.BROWN_MUSHROOM.asItem()) && !bl3) {
                bl3 = true;
                continue;
            }
            if (lv.isOf(Blocks.RED_MUSHROOM.asItem()) && !bl2) {
                bl2 = true;
                continue;
            }
            if (lv.isIn(ItemTags.SMALL_FLOWERS) && !bl) {
                bl = true;
                continue;
            }
            if (lv.isOf(Items.BOWL) && !bl4) {
                bl4 = true;
                continue;
            }
            return false;
        }
        return bl && bl3 && bl2 && bl4;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        ItemStack lv = new ItemStack(Items.SUSPICIOUS_STEW, 1);
        for (int i = 0; i < arg.size(); ++i) {
            SuspiciousStewIngredient lv3;
            ItemStack lv2 = arg.getStack(i);
            if (lv2.isEmpty() || (lv3 = SuspiciousStewIngredient.of(lv2.getItem())) == null) continue;
            SuspiciousStewItem.addEffectToStew(lv, lv3.getEffectInStew(), lv3.getEffectInStewDuration());
            break;
        }
        return lv;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SUSPICIOUS_STEW;
    }
}

