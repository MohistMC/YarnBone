/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class KnowledgeBookItem
extends Item {
    private static final String RECIPES_KEY = "Recipes";
    private static final Logger LOGGER = LogUtils.getLogger();

    public KnowledgeBookItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        NbtCompound lv2 = lv.getNbt();
        if (!user.getAbilities().creativeMode) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        if (lv2 == null || !lv2.contains(RECIPES_KEY, NbtElement.LIST_TYPE)) {
            LOGGER.error("Tag not valid: {}", (Object)lv2);
            return TypedActionResult.fail(lv);
        }
        if (!world.isClient) {
            NbtList lv3 = lv2.getList(RECIPES_KEY, NbtElement.STRING_TYPE);
            ArrayList<Recipe<?>> list = Lists.newArrayList();
            RecipeManager lv4 = world.getServer().getRecipeManager();
            for (int i = 0; i < lv3.size(); ++i) {
                String string = lv3.getString(i);
                Optional<Recipe<?>> optional = lv4.get(new Identifier(string));
                if (!optional.isPresent()) {
                    LOGGER.error("Invalid recipe: {}", (Object)string);
                    return TypedActionResult.fail(lv);
                }
                list.add(optional.get());
            }
            user.unlockRecipes(list);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return TypedActionResult.success(lv, world.isClient());
    }
}

