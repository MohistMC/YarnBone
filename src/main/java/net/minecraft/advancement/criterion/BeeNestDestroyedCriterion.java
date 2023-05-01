/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class BeeNestDestroyedCriterion
extends AbstractCriterion<Conditions> {
    static final Identifier ID = new Identifier("bee_nest_destroyed");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
        Block lv = BeeNestDestroyedCriterion.getBlock(jsonObject);
        ItemPredicate lv2 = ItemPredicate.fromJson(jsonObject.get("item"));
        NumberRange.IntRange lv3 = NumberRange.IntRange.fromJson(jsonObject.get("num_bees_inside"));
        return new Conditions(arg, lv, lv2, lv3);
    }

    @Nullable
    private static Block getBlock(JsonObject root) {
        if (root.has("block")) {
            Identifier lv = new Identifier(JsonHelper.getString(root, "block"));
            return (Block)Registries.BLOCK.getOrEmpty(lv).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + lv + "'"));
        }
        return null;
    }

    public void trigger(ServerPlayerEntity player, BlockState state, ItemStack stack, int beeCount) {
        this.trigger(player, conditions -> conditions.test(state, stack, beeCount));
    }

    public static class Conditions
    extends AbstractCriterionConditions {
        @Nullable
        private final Block block;
        private final ItemPredicate item;
        private final NumberRange.IntRange beeCount;

        public Conditions(EntityPredicate.Extended player, @Nullable Block block, ItemPredicate item, NumberRange.IntRange beeCount) {
            super(ID, player);
            this.block = block;
            this.item = item;
            this.beeCount = beeCount;
        }

        public static Conditions create(Block block, ItemPredicate.Builder itemPredicateBuilder, NumberRange.IntRange beeCountRange) {
            return new Conditions(EntityPredicate.Extended.EMPTY, block, itemPredicateBuilder.build(), beeCountRange);
        }

        public boolean test(BlockState state, ItemStack stack, int count) {
            if (this.block != null && !state.isOf(this.block)) {
                return false;
            }
            if (!this.item.test(stack)) {
                return false;
            }
            return this.beeCount.test(count);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            if (this.block != null) {
                jsonObject.addProperty("block", Registries.BLOCK.getId(this.block).toString());
            }
            jsonObject.add("item", this.item.toJson());
            jsonObject.add("num_bees_inside", this.beeCount.toJson());
            return jsonObject;
        }
    }
}

