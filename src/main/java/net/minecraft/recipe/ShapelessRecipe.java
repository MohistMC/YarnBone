/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ShapelessRecipe
implements CraftingRecipe {
    private final Identifier id;
    final String group;
    final CraftingRecipeCategory category;
    final ItemStack output;
    final DefaultedList<Ingredient> input;

    public ShapelessRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.output = output;
        this.input = input;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return this.category;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.output;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return this.input;
    }

    @Override
    public boolean matches(CraftingInventory arg, World arg2) {
        RecipeMatcher lv = new RecipeMatcher();
        int i = 0;
        for (int j = 0; j < arg.size(); ++j) {
            ItemStack lv2 = arg.getStack(j);
            if (lv2.isEmpty()) continue;
            ++i;
            lv.addInput(lv2, 1);
        }
        return i == this.input.size() && lv.match(this, null);
    }

    @Override
    public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= this.input.size();
    }

    public static class Serializer
    implements RecipeSerializer<ShapelessRecipe> {
        @Override
        public ShapelessRecipe read(Identifier arg, JsonObject jsonObject) {
            String string = JsonHelper.getString(jsonObject, "group", "");
            CraftingRecipeCategory lv = CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(jsonObject, "category", null), CraftingRecipeCategory.MISC);
            DefaultedList<Ingredient> lv2 = Serializer.getIngredients(JsonHelper.getArray(jsonObject, "ingredients"));
            if (lv2.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            }
            if (lv2.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            }
            ItemStack lv3 = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
            return new ShapelessRecipe(arg, string, lv, lv3, lv2);
        }

        private static DefaultedList<Ingredient> getIngredients(JsonArray json) {
            DefaultedList<Ingredient> lv = DefaultedList.of();
            for (int i = 0; i < json.size(); ++i) {
                Ingredient lv2 = Ingredient.fromJson(json.get(i));
                if (lv2.isEmpty()) continue;
                lv.add(lv2);
            }
            return lv;
        }

        @Override
        public ShapelessRecipe read(Identifier arg, PacketByteBuf arg2) {
            String string = arg2.readString();
            CraftingRecipeCategory lv = arg2.readEnumConstant(CraftingRecipeCategory.class);
            int i = arg2.readVarInt();
            DefaultedList<Ingredient> lv2 = DefaultedList.ofSize(i, Ingredient.EMPTY);
            for (int j = 0; j < lv2.size(); ++j) {
                lv2.set(j, Ingredient.fromPacket(arg2));
            }
            ItemStack lv3 = arg2.readItemStack();
            return new ShapelessRecipe(arg, string, lv, lv3, lv2);
        }

        @Override
        public void write(PacketByteBuf arg, ShapelessRecipe arg2) {
            arg.writeString(arg2.group);
            arg.writeEnumConstant(arg2.category);
            arg.writeVarInt(arg2.input.size());
            for (Ingredient lv : arg2.input) {
                lv.write(arg);
            }
            arg.writeItemStack(arg2.output);
        }

        @Override
        public /* synthetic */ Recipe read(Identifier id, PacketByteBuf buf) {
            return this.read(id, buf);
        }

        @Override
        public /* synthetic */ Recipe read(Identifier id, JsonObject json) {
            return this.read(id, json);
        }
    }
}

