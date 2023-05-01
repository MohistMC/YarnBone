/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RepairItemRecipe
extends SpecialCraftingRecipe {
    public RepairItemRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv2;
            ItemStack lv = arg.getStack(i);
            if (lv.isEmpty()) continue;
            list.add(lv);
            if (list.size() <= 1 || lv.isOf((lv2 = (ItemStack)list.get(0)).getItem()) && lv2.getCount() == 1 && lv.getCount() == 1 && lv2.getItem().isDamageable()) continue;
            return false;
        }
        return list.size() == 2;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        ItemStack lv3;
        ItemStack lv;
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv2;
            lv = arg.getStack(i);
            if (lv.isEmpty()) continue;
            list.add(lv);
            if (list.size() <= 1 || lv.isOf((lv2 = (ItemStack)list.get(0)).getItem()) && lv2.getCount() == 1 && lv.getCount() == 1 && lv2.getItem().isDamageable()) continue;
            return ItemStack.EMPTY;
        }
        if (list.size() == 2 && (lv3 = (ItemStack)list.get(0)).isOf((lv = (ItemStack)list.get(1)).getItem()) && lv3.getCount() == 1 && lv.getCount() == 1 && lv3.getItem().isDamageable()) {
            Item lv4 = lv3.getItem();
            int j = lv4.getMaxDamage() - lv3.getDamage();
            int k = lv4.getMaxDamage() - lv.getDamage();
            int l = j + k + lv4.getMaxDamage() * 5 / 100;
            int m = lv4.getMaxDamage() - l;
            if (m < 0) {
                m = 0;
            }
            ItemStack lv5 = new ItemStack(lv3.getItem());
            lv5.setDamage(m);
            HashMap<Enchantment, Integer> map = Maps.newHashMap();
            Map<Enchantment, Integer> map2 = EnchantmentHelper.get(lv3);
            Map<Enchantment, Integer> map3 = EnchantmentHelper.get(lv);
            Registries.ENCHANTMENT.stream().filter(Enchantment::isCursed).forEach(enchantment -> {
                int i = Math.max(map2.getOrDefault(enchantment, 0), map3.getOrDefault(enchantment, 0));
                if (i > 0) {
                    map.put((Enchantment)enchantment, i);
                }
            });
            if (!map.isEmpty()) {
                EnchantmentHelper.set(map, lv5);
            }
            return lv5;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}

