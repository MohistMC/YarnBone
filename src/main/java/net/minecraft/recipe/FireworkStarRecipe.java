/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public class FireworkStarRecipe
extends SpecialCraftingRecipe {
    private static final Ingredient TYPE_MODIFIER = Ingredient.ofItems(Items.FIRE_CHARGE, Items.FEATHER, Items.GOLD_NUGGET, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.DRAGON_HEAD, Items.ZOMBIE_HEAD, Items.PIGLIN_HEAD);
    private static final Ingredient TRAIL_MODIFIER = Ingredient.ofItems(Items.DIAMOND);
    private static final Ingredient FLICKER_MODIFIER = Ingredient.ofItems(Items.GLOWSTONE_DUST);
    private static final Map<Item, FireworkRocketItem.Type> TYPE_MODIFIER_MAP = Util.make(Maps.newHashMap(), typeModifiers -> {
        typeModifiers.put(Items.FIRE_CHARGE, FireworkRocketItem.Type.LARGE_BALL);
        typeModifiers.put(Items.FEATHER, FireworkRocketItem.Type.BURST);
        typeModifiers.put(Items.GOLD_NUGGET, FireworkRocketItem.Type.STAR);
        typeModifiers.put(Items.SKELETON_SKULL, FireworkRocketItem.Type.CREEPER);
        typeModifiers.put(Items.WITHER_SKELETON_SKULL, FireworkRocketItem.Type.CREEPER);
        typeModifiers.put(Items.CREEPER_HEAD, FireworkRocketItem.Type.CREEPER);
        typeModifiers.put(Items.PLAYER_HEAD, FireworkRocketItem.Type.CREEPER);
        typeModifiers.put(Items.DRAGON_HEAD, FireworkRocketItem.Type.CREEPER);
        typeModifiers.put(Items.ZOMBIE_HEAD, FireworkRocketItem.Type.CREEPER);
        typeModifiers.put(Items.PIGLIN_HEAD, FireworkRocketItem.Type.CREEPER);
    });
    private static final Ingredient GUNPOWDER = Ingredient.ofItems(Items.GUNPOWDER);

    public FireworkStarRecipe(Identifier arg, CraftingRecipeCategory arg2) {
        super(arg, arg2);
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        boolean bl5 = false;
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv = arg.getStack(i);
            if (lv.isEmpty()) continue;
            if (TYPE_MODIFIER.test(lv)) {
                if (bl3) {
                    return false;
                }
                bl3 = true;
                continue;
            }
            if (FLICKER_MODIFIER.test(lv)) {
                if (bl5) {
                    return false;
                }
                bl5 = true;
                continue;
            }
            if (TRAIL_MODIFIER.test(lv)) {
                if (bl4) {
                    return false;
                }
                bl4 = true;
                continue;
            }
            if (GUNPOWDER.test(lv)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (lv.getItem() instanceof DyeItem) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl && bl2;
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        ItemStack lv = new ItemStack(Items.FIREWORK_STAR);
        NbtCompound lv2 = lv.getOrCreateSubNbt("Explosion");
        FireworkRocketItem.Type lv3 = FireworkRocketItem.Type.SMALL_BALL;
        ArrayList<Integer> list = Lists.newArrayList();
        for (int i = 0; i < arg.size(); ++i) {
            ItemStack lv4 = arg.getStack(i);
            if (lv4.isEmpty()) continue;
            if (TYPE_MODIFIER.test(lv4)) {
                lv3 = TYPE_MODIFIER_MAP.get(lv4.getItem());
                continue;
            }
            if (FLICKER_MODIFIER.test(lv4)) {
                lv2.putBoolean("Flicker", true);
                continue;
            }
            if (TRAIL_MODIFIER.test(lv4)) {
                lv2.putBoolean("Trail", true);
                continue;
            }
            if (!(lv4.getItem() instanceof DyeItem)) continue;
            list.add(((DyeItem)lv4.getItem()).getColor().getFireworkColor());
        }
        lv2.putIntArray("Colors", list);
        lv2.putByte("Type", (byte)lv3.getId());
        return lv;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return new ItemStack(Items.FIREWORK_STAR);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR;
    }
}

