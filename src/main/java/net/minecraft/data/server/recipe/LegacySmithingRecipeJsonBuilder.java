/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.data.server.recipe;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval=true)
public class LegacySmithingRecipeJsonBuilder {
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Item result;
    private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
    private final RecipeSerializer<?> serializer;

    public LegacySmithingRecipeJsonBuilder(RecipeSerializer<?> serializer, Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        this.category = category;
        this.serializer = serializer;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public static LegacySmithingRecipeJsonBuilder create(Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        return new LegacySmithingRecipeJsonBuilder(RecipeSerializer.SMITHING, base, addition, category, result);
    }

    public LegacySmithingRecipeJsonBuilder criterion(String criterionName, CriterionConditions conditions) {
        this.advancementBuilder.criterion(criterionName, conditions);
        return this;
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, String recipeId) {
        this.offerTo(exporter, new Identifier(recipeId));
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        this.validate(recipeId);
        this.advancementBuilder.parent(CraftingRecipeJsonBuilder.ROOT).criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
        exporter.accept(new SmithingRecipeJsonProvider(recipeId, this.serializer, this.base, this.addition, this.result, this.advancementBuilder, recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(Identifier recipeId) {
        if (this.advancementBuilder.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }

    public static class SmithingRecipeJsonProvider
    implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final Ingredient base;
        private final Ingredient addition;
        private final Item result;
        private final Advancement.Builder advancementBuilder;
        private final Identifier advancementId;
        private final RecipeSerializer<?> serializer;

        public SmithingRecipeJsonProvider(Identifier recipeId, RecipeSerializer<?> serializer, Ingredient base, Ingredient addition, Item result, Advancement.Builder advancementBuilder, Identifier advancementId) {
            this.recipeId = recipeId;
            this.serializer = serializer;
            this.base = base;
            this.addition = addition;
            this.result = result;
            this.advancementBuilder = advancementBuilder;
            this.advancementId = advancementId;
        }

        @Override
        public void serialize(JsonObject json) {
            json.add("base", this.base.toJson());
            json.add("addition", this.addition.toJson());
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("item", Registries.ITEM.getId(this.result).toString());
            json.add("result", jsonObject2);
        }

        @Override
        public Identifier getRecipeId() {
            return this.recipeId;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return this.serializer;
        }

        @Override
        @Nullable
        public JsonObject toAdvancementJson() {
            return this.advancementBuilder.toJson();
        }

        @Override
        @Nullable
        public Identifier getAdvancementId() {
            return this.advancementId;
        }
    }
}

