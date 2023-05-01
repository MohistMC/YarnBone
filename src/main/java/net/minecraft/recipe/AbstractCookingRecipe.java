/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public abstract class AbstractCookingRecipe
implements Recipe<Inventory> {
    protected final RecipeType<?> type;
    protected final Identifier id;
    private final CookingRecipeCategory category;
    protected final String group;
    protected final Ingredient input;
    protected final ItemStack output;
    protected final float experience;
    protected final int cookTime;

    public AbstractCookingRecipe(RecipeType<?> type, Identifier id, String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
        this.type = type;
        this.category = category;
        this.id = id;
        this.group = group;
        this.input = input;
        this.output = output;
        this.experience = experience;
        this.cookTime = cookTime;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> lv = DefaultedList.of();
        lv.add(this.input);
        return lv;
    }

    public float getExperience() {
        return this.experience;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.output;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    public int getCookTime() {
        return this.cookTime;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    public CookingRecipeCategory getCategory() {
        return this.category;
    }
}

