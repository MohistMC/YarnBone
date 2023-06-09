/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import java.util.Collections;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface RecipeUnlocker {
    public void setLastRecipe(@Nullable Recipe<?> var1);

    @Nullable
    public Recipe<?> getLastRecipe();

    default public void unlockLastRecipe(PlayerEntity player) {
        Recipe<?> lv = this.getLastRecipe();
        if (lv != null && !lv.isIgnoredInRecipeBook()) {
            player.unlockRecipes(Collections.singleton(lv));
            this.setLastRecipe(null);
        }
    }

    default public boolean shouldCraftRecipe(World world, ServerPlayerEntity player, Recipe<?> recipe) {
        if (recipe.isIgnoredInRecipeBook() || !world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) || player.getRecipeBook().contains(recipe)) {
            this.setLastRecipe(recipe);
            return true;
        }
        return false;
    }
}

