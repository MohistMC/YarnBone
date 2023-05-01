/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.data.server.recipe;

import com.google.gson.JsonObject;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface RecipeJsonProvider {
    public void serialize(JsonObject var1);

    default public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", Registries.RECIPE_SERIALIZER.getId(this.getSerializer()).toString());
        this.serialize(jsonObject);
        return jsonObject;
    }

    public Identifier getRecipeId();

    public RecipeSerializer<?> getSerializer();

    @Nullable
    public JsonObject toAdvancementJson();

    @Nullable
    public Identifier getAdvancementId();
}

