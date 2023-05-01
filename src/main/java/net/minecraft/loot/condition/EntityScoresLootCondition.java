/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class EntityScoresLootCondition
implements LootCondition {
    final Map<String, BoundedIntUnaryOperator> scores;
    final LootContext.EntityTarget target;

    EntityScoresLootCondition(Map<String, BoundedIntUnaryOperator> scores, LootContext.EntityTarget target) {
        this.scores = ImmutableMap.copyOf(scores);
        this.target = target;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENTITY_SCORES;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Stream.concat(Stream.of(this.target.getParameter()), this.scores.values().stream().flatMap(arg -> arg.getRequiredParameters().stream())).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public boolean test(LootContext arg) {
        Entity lv = arg.get(this.target.getParameter());
        if (lv == null) {
            return false;
        }
        Scoreboard lv2 = lv.world.getScoreboard();
        for (Map.Entry<String, BoundedIntUnaryOperator> entry : this.scores.entrySet()) {
            if (this.entityScoreIsInRange(arg, lv, lv2, entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    protected boolean entityScoreIsInRange(LootContext context, Entity entity, Scoreboard scoreboard, String objectiveName, BoundedIntUnaryOperator range) {
        ScoreboardObjective lv = scoreboard.getNullableObjective(objectiveName);
        if (lv == null) {
            return false;
        }
        String string2 = entity.getEntityName();
        if (!scoreboard.playerHasObjective(string2, lv)) {
            return false;
        }
        return range.test(context, scoreboard.getPlayerScore(string2, lv).getScore());
    }

    public static Builder create(LootContext.EntityTarget target) {
        return new Builder(target);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        private final Map<String, BoundedIntUnaryOperator> scores = Maps.newHashMap();
        private final LootContext.EntityTarget target;

        public Builder(LootContext.EntityTarget target) {
            this.target = target;
        }

        public Builder score(String name, BoundedIntUnaryOperator value) {
            this.scores.put(name, value);
            return this;
        }

        @Override
        public LootCondition build() {
            return new EntityScoresLootCondition(this.scores, this.target);
        }
    }

    public static class Serializer
    implements JsonSerializer<EntityScoresLootCondition> {
        @Override
        public void toJson(JsonObject jsonObject, EntityScoresLootCondition arg, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject2 = new JsonObject();
            for (Map.Entry<String, BoundedIntUnaryOperator> entry : arg.scores.entrySet()) {
                jsonObject2.add(entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
            }
            jsonObject.add("scores", jsonObject2);
            jsonObject.add("entity", jsonSerializationContext.serialize((Object)arg.target));
        }

        @Override
        public EntityScoresLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Set<Map.Entry<String, JsonElement>> set = JsonHelper.getObject(jsonObject, "scores").entrySet();
            LinkedHashMap<String, BoundedIntUnaryOperator> map = Maps.newLinkedHashMap();
            for (Map.Entry<String, JsonElement> entry : set) {
                map.put(entry.getKey(), JsonHelper.deserialize(entry.getValue(), "score", jsonDeserializationContext, BoundedIntUnaryOperator.class));
            }
            return new EntityScoresLootCondition(map, JsonHelper.deserialize(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

