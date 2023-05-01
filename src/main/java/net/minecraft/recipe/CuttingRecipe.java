/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public abstract class CuttingRecipe
implements Recipe<Inventory> {
    protected final Ingredient input;
    protected final ItemStack output;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;
    protected final Identifier id;
    protected final String group;

    public CuttingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, Identifier id, String group, Ingredient input, ItemStack output) {
        this.type = type;
        this.serializer = serializer;
        this.id = id;
        this.group = group;
        this.input = input;
        this.output = output;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.output;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> lv = DefaultedList.of();
        lv.add(this.input);
        return lv;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return this.output.copy();
    }

    public static class Serializer<T extends CuttingRecipe>
    implements RecipeSerializer<T> {
        final RecipeFactory<T> recipeFactory;

        protected Serializer(RecipeFactory<T> recipeFactory) {
            this.recipeFactory = recipeFactory;
        }

        @Override
        public T read(Identifier arg, JsonObject jsonObject) {
            String string = JsonHelper.getString(jsonObject, "group", "");
            Ingredient lv = JsonHelper.hasArray(jsonObject, "ingredient") ? Ingredient.fromJson(JsonHelper.getArray(jsonObject, "ingredient")) : Ingredient.fromJson(JsonHelper.getObject(jsonObject, "ingredient"));
            String string2 = JsonHelper.getString(jsonObject, "result");
            int i = JsonHelper.getInt(jsonObject, "count");
            ItemStack lv2 = new ItemStack(Registries.ITEM.get(new Identifier(string2)), i);
            return this.recipeFactory.create(arg, string, lv, lv2);
        }

        @Override
        public T read(Identifier arg, PacketByteBuf arg2) {
            String string = arg2.readString();
            Ingredient lv = Ingredient.fromPacket(arg2);
            ItemStack lv2 = arg2.readItemStack();
            return this.recipeFactory.create(arg, string, lv, lv2);
        }

        @Override
        public void write(PacketByteBuf arg, T arg2) {
            arg.writeString(((CuttingRecipe)arg2).group);
            ((CuttingRecipe)arg2).input.write(arg);
            arg.writeItemStack(((CuttingRecipe)arg2).output);
        }

        @Override
        public /* synthetic */ Recipe read(Identifier id, PacketByteBuf buf) {
            return this.read(id, buf);
        }

        @Override
        public /* synthetic */ Recipe read(Identifier id, JsonObject json) {
            return this.read(id, json);
        }

        static interface RecipeFactory<T extends CuttingRecipe> {
            public T create(Identifier var1, String var2, Ingredient var3, ItemStack var4);
        }
    }
}

