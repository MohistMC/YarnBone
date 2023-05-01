/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CookingRecipeSerializer<T extends AbstractCookingRecipe>
implements RecipeSerializer<T> {
    private final int cookingTime;
    private final RecipeFactory<T> recipeFactory;

    public CookingRecipeSerializer(RecipeFactory<T> recipeFactory, int cookingTime) {
        this.cookingTime = cookingTime;
        this.recipeFactory = recipeFactory;
    }

    @Override
    public T read(Identifier arg, JsonObject jsonObject) {
        String string = JsonHelper.getString(jsonObject, "group", "");
        CookingRecipeCategory lv = CookingRecipeCategory.CODEC.byId(JsonHelper.getString(jsonObject, "category", null), CookingRecipeCategory.MISC);
        JsonElement jsonElement = JsonHelper.hasArray(jsonObject, "ingredient") ? JsonHelper.getArray(jsonObject, "ingredient") : JsonHelper.getObject(jsonObject, "ingredient");
        Ingredient lv2 = Ingredient.fromJson(jsonElement);
        String string2 = JsonHelper.getString(jsonObject, "result");
        Identifier lv3 = new Identifier(string2);
        ItemStack lv4 = new ItemStack((ItemConvertible)Registries.ITEM.getOrEmpty(lv3).orElseThrow(() -> new IllegalStateException("Item: " + string2 + " does not exist")));
        float f = JsonHelper.getFloat(jsonObject, "experience", 0.0f);
        int i = JsonHelper.getInt(jsonObject, "cookingtime", this.cookingTime);
        return this.recipeFactory.create(arg, string, lv, lv2, lv4, f, i);
    }

    @Override
    public T read(Identifier arg, PacketByteBuf arg2) {
        String string = arg2.readString();
        CookingRecipeCategory lv = arg2.readEnumConstant(CookingRecipeCategory.class);
        Ingredient lv2 = Ingredient.fromPacket(arg2);
        ItemStack lv3 = arg2.readItemStack();
        float f = arg2.readFloat();
        int i = arg2.readVarInt();
        return this.recipeFactory.create(arg, string, lv, lv2, lv3, f, i);
    }

    @Override
    public void write(PacketByteBuf arg, T arg2) {
        arg.writeString(((AbstractCookingRecipe)arg2).group);
        arg.writeEnumConstant(((AbstractCookingRecipe)arg2).getCategory());
        ((AbstractCookingRecipe)arg2).input.write(arg);
        arg.writeItemStack(((AbstractCookingRecipe)arg2).output);
        arg.writeFloat(((AbstractCookingRecipe)arg2).experience);
        arg.writeVarInt(((AbstractCookingRecipe)arg2).cookTime);
    }

    @Override
    public /* synthetic */ Recipe read(Identifier id, PacketByteBuf buf) {
        return this.read(id, buf);
    }

    @Override
    public /* synthetic */ Recipe read(Identifier id, JsonObject json) {
        return this.read(id, json);
    }

    static interface RecipeFactory<T extends AbstractCookingRecipe> {
        public T create(Identifier var1, String var2, CookingRecipeCategory var3, Ingredient var4, ItemStack var5, float var6, int var7);
    }
}

