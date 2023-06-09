/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.advancement.criterion;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class InventoryChangedCriterion
extends AbstractCriterion<Conditions> {
    static final Identifier ID = new Identifier("inventory_changed");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
        JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "slots", new JsonObject());
        NumberRange.IntRange lv = NumberRange.IntRange.fromJson(jsonObject2.get("occupied"));
        NumberRange.IntRange lv2 = NumberRange.IntRange.fromJson(jsonObject2.get("full"));
        NumberRange.IntRange lv3 = NumberRange.IntRange.fromJson(jsonObject2.get("empty"));
        ItemPredicate[] lvs = ItemPredicate.deserializeAll(jsonObject.get("items"));
        return new Conditions(arg, lv, lv2, lv3, lvs);
    }

    public void trigger(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack) {
        int i = 0;
        int j = 0;
        int k = 0;
        for (int l = 0; l < inventory.size(); ++l) {
            ItemStack lv = inventory.getStack(l);
            if (lv.isEmpty()) {
                ++j;
                continue;
            }
            ++k;
            if (lv.getCount() < lv.getMaxCount()) continue;
            ++i;
        }
        this.trigger(player, inventory, stack, i, j, k);
    }

    private void trigger(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack, int full, int empty, int occupied) {
        this.trigger(player, conditions -> conditions.matches(inventory, stack, full, empty, occupied));
    }

    @Override
    public /* synthetic */ AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
    }

    public static class Conditions
    extends AbstractCriterionConditions {
        private final NumberRange.IntRange occupied;
        private final NumberRange.IntRange full;
        private final NumberRange.IntRange empty;
        private final ItemPredicate[] items;

        public Conditions(EntityPredicate.Extended player, NumberRange.IntRange occupied, NumberRange.IntRange full, NumberRange.IntRange empty, ItemPredicate[] items) {
            super(ID, player);
            this.occupied = occupied;
            this.full = full;
            this.empty = empty;
            this.items = items;
        }

        public static Conditions items(ItemPredicate ... items) {
            return new Conditions(EntityPredicate.Extended.EMPTY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, items);
        }

        public static Conditions items(ItemConvertible ... items) {
            ItemPredicate[] lvs = new ItemPredicate[items.length];
            for (int i = 0; i < items.length; ++i) {
                lvs[i] = new ItemPredicate(null, ImmutableSet.of(items[i].asItem()), NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, EnchantmentPredicate.ARRAY_OF_ANY, EnchantmentPredicate.ARRAY_OF_ANY, null, NbtPredicate.ANY);
            }
            return Conditions.items(lvs);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            if (!(this.occupied.isDummy() && this.full.isDummy() && this.empty.isDummy())) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.add("occupied", this.occupied.toJson());
                jsonObject2.add("full", this.full.toJson());
                jsonObject2.add("empty", this.empty.toJson());
                jsonObject.add("slots", jsonObject2);
            }
            if (this.items.length > 0) {
                JsonArray jsonArray = new JsonArray();
                for (ItemPredicate lv : this.items) {
                    jsonArray.add(lv.toJson());
                }
                jsonObject.add("items", jsonArray);
            }
            return jsonObject;
        }

        public boolean matches(PlayerInventory inventory, ItemStack stack, int full, int empty, int occupied) {
            if (!this.full.test(full)) {
                return false;
            }
            if (!this.empty.test(empty)) {
                return false;
            }
            if (!this.occupied.test(occupied)) {
                return false;
            }
            int l = this.items.length;
            if (l == 0) {
                return true;
            }
            if (l == 1) {
                return !stack.isEmpty() && this.items[0].test(stack);
            }
            ObjectArrayList<ItemPredicate> list = new ObjectArrayList<ItemPredicate>(this.items);
            int m = inventory.size();
            for (int n = 0; n < m; ++n) {
                if (list.isEmpty()) {
                    return true;
                }
                ItemStack lv = inventory.getStack(n);
                if (lv.isEmpty()) continue;
                list.removeIf(item -> item.test(lv));
            }
            return list.isEmpty();
        }
    }
}

