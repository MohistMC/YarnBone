/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BredAnimalsCriterion
extends AbstractCriterion<Conditions> {
    static final Identifier ID = new Identifier("bred_animals");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
        EntityPredicate.Extended lv = EntityPredicate.Extended.getInJson(jsonObject, "parent", arg2);
        EntityPredicate.Extended lv2 = EntityPredicate.Extended.getInJson(jsonObject, "partner", arg2);
        EntityPredicate.Extended lv3 = EntityPredicate.Extended.getInJson(jsonObject, "child", arg2);
        return new Conditions(arg, lv, lv2, lv3);
    }

    public void trigger(ServerPlayerEntity player, AnimalEntity parent, AnimalEntity partner, @Nullable PassiveEntity child) {
        LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, parent);
        LootContext lv2 = EntityPredicate.createAdvancementEntityLootContext(player, partner);
        LootContext lv3 = child != null ? EntityPredicate.createAdvancementEntityLootContext(player, child) : null;
        this.trigger(player, conditions -> conditions.matches(lv, lv2, lv3));
    }

    public static class Conditions
    extends AbstractCriterionConditions {
        private final EntityPredicate.Extended parent;
        private final EntityPredicate.Extended partner;
        private final EntityPredicate.Extended child;

        public Conditions(EntityPredicate.Extended player, EntityPredicate.Extended parent, EntityPredicate.Extended partner, EntityPredicate.Extended child) {
            super(ID, player);
            this.parent = parent;
            this.partner = partner;
            this.child = child;
        }

        public static Conditions any() {
            return new Conditions(EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.EMPTY);
        }

        public static Conditions create(EntityPredicate.Builder child) {
            return new Conditions(EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.ofLegacy(child.build()));
        }

        public static Conditions create(EntityPredicate parent, EntityPredicate partner, EntityPredicate child) {
            return new Conditions(EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.ofLegacy(parent), EntityPredicate.Extended.ofLegacy(partner), EntityPredicate.Extended.ofLegacy(child));
        }

        public boolean matches(LootContext parentContext, LootContext partnerContext, @Nullable LootContext childContext) {
            if (!(this.child == EntityPredicate.Extended.EMPTY || childContext != null && this.child.test(childContext))) {
                return false;
            }
            return this.parent.test(parentContext) && this.partner.test(partnerContext) || this.parent.test(partnerContext) && this.partner.test(parentContext);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("parent", this.parent.toJson(predicateSerializer));
            jsonObject.add("partner", this.partner.toJson(predicateSerializer));
            jsonObject.add("child", this.child.toJson(predicateSerializer));
            return jsonObject;
        }
    }
}

