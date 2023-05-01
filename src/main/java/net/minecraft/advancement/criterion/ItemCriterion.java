/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ItemCriterion
extends AbstractCriterion<Conditions> {
    final Identifier id;

    public ItemCriterion(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
        LocationPredicate lv = LocationPredicate.fromJson(jsonObject.get("location"));
        ItemPredicate lv2 = ItemPredicate.fromJson(jsonObject.get("item"));
        return new Conditions(this.id, arg, lv, lv2);
    }

    public void trigger(ServerPlayerEntity player, BlockPos pos, ItemStack stack) {
        BlockState lv = player.getWorld().getBlockState(pos);
        this.trigger(player, conditions -> conditions.test(lv, player.getWorld(), pos, stack));
    }

    @Override
    public /* synthetic */ AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
    }

    public static class Conditions
    extends AbstractCriterionConditions {
        private final LocationPredicate location;
        private final ItemPredicate item;

        public Conditions(Identifier id, EntityPredicate.Extended entity, LocationPredicate location, ItemPredicate item) {
            super(id, entity);
            this.location = location;
            this.item = item;
        }

        public static Conditions create(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            return new Conditions(Criteria.ITEM_USED_ON_BLOCK.id, EntityPredicate.Extended.EMPTY, location.build(), item.build());
        }

        public static Conditions createAllayDropItemOnBlock(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            return new Conditions(Criteria.ALLAY_DROP_ITEM_ON_BLOCK.id, EntityPredicate.Extended.EMPTY, location.build(), item.build());
        }

        public boolean test(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack) {
            if (!this.location.test(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)) {
                return false;
            }
            return this.item.test(stack);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("location", this.location.toJson());
            jsonObject.add("item", this.item.toJson());
            return jsonObject;
        }
    }
}

