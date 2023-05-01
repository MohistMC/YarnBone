/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SpecialRecipeSerializer<T extends CraftingRecipe>
implements RecipeSerializer<T> {
    private final Factory<T> factory;

    public SpecialRecipeSerializer(Factory<T> factory) {
        this.factory = factory;
    }

    @Override
    public T read(Identifier arg, JsonObject jsonObject) {
        CraftingRecipeCategory lv = CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(jsonObject, "category", null), CraftingRecipeCategory.MISC);
        return this.factory.create(arg, lv);
    }

    @Override
    public T read(Identifier arg, PacketByteBuf arg2) {
        CraftingRecipeCategory lv = arg2.readEnumConstant(CraftingRecipeCategory.class);
        return this.factory.create(arg, lv);
    }

    @Override
    public void write(PacketByteBuf arg, T arg2) {
        arg.writeEnumConstant(arg2.getCategory());
    }

    @Override
    public /* synthetic */ Recipe read(Identifier id, PacketByteBuf buf) {
        return this.read(id, buf);
    }

    @Override
    public /* synthetic */ Recipe read(Identifier id, JsonObject json) {
        return this.read(id, json);
    }

    @FunctionalInterface
    public static interface Factory<T extends CraftingRecipe> {
        public T create(Identifier var1, CraftingRecipeCategory var2);
    }
}

