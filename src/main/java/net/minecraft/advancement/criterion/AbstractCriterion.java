/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.advancement.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractCriterion<T extends AbstractCriterionConditions>
implements Criterion<T> {
    private final Map<PlayerAdvancementTracker, Set<Criterion.ConditionsContainer<T>>> progressions = Maps.newIdentityHashMap();

    @Override
    public final void beginTrackingCondition(PlayerAdvancementTracker manager2, Criterion.ConditionsContainer<T> conditions) {
        this.progressions.computeIfAbsent(manager2, manager -> Sets.newHashSet()).add(conditions);
    }

    @Override
    public final void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditions) {
        Set<Criterion.ConditionsContainer<T>> set = this.progressions.get(manager);
        if (set != null) {
            set.remove(conditions);
            if (set.isEmpty()) {
                this.progressions.remove(manager);
            }
        }
    }

    @Override
    public final void endTracking(PlayerAdvancementTracker tracker) {
        this.progressions.remove(tracker);
    }

    protected abstract T conditionsFromJson(JsonObject var1, EntityPredicate.Extended var2, AdvancementEntityPredicateDeserializer var3);

    @Override
    public final T conditionsFromJson(JsonObject jsonObject, AdvancementEntityPredicateDeserializer arg) {
        EntityPredicate.Extended lv = EntityPredicate.Extended.getInJson(jsonObject, "player", arg);
        return this.conditionsFromJson(jsonObject, lv, arg);
    }

    protected void trigger(ServerPlayerEntity player, Predicate<T> predicate) {
        PlayerAdvancementTracker lv = player.getAdvancementTracker();
        Set<Criterion.ConditionsContainer<T>> set = this.progressions.get(lv);
        if (set == null || set.isEmpty()) {
            return;
        }
        LootContext lv2 = EntityPredicate.createAdvancementEntityLootContext(player, player);
        ArrayList<Criterion.ConditionsContainer<T>> list = null;
        for (Criterion.ConditionsContainer<T> conditionsContainer : set) {
            AbstractCriterionConditions lv4 = (AbstractCriterionConditions)conditionsContainer.getConditions();
            if (!predicate.test(lv4) || !lv4.getPlayerPredicate().test(lv2)) continue;
            if (list == null) {
                list = Lists.newArrayList();
            }
            list.add(conditionsContainer);
        }
        if (list != null) {
            for (Criterion.ConditionsContainer<Object> conditionsContainer : list) {
                conditionsContainer.grant(lv);
            }
        }
    }
}

