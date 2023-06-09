/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.condition;

import java.util.function.Predicate;
import net.minecraft.loot.condition.AlternativeLootCondition;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.DamageSourcePropertiesLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.EntityScoresLootCondition;
import net.minecraft.loot.condition.InvertedLootCondition;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.RandomChanceWithLootingLootCondition;
import net.minecraft.loot.condition.ReferenceLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.condition.TimeCheckLootCondition;
import net.minecraft.loot.condition.ValueCheckLootCondition;
import net.minecraft.loot.condition.WeatherCheckLootCondition;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;

public class LootConditionTypes {
    public static final LootConditionType INVERTED = LootConditionTypes.register("inverted", new InvertedLootCondition.Serializer());
    public static final LootConditionType ALTERNATIVE = LootConditionTypes.register("alternative", new AlternativeLootCondition.Serializer());
    public static final LootConditionType RANDOM_CHANCE = LootConditionTypes.register("random_chance", new RandomChanceLootCondition.Serializer());
    public static final LootConditionType RANDOM_CHANCE_WITH_LOOTING = LootConditionTypes.register("random_chance_with_looting", new RandomChanceWithLootingLootCondition.Serializer());
    public static final LootConditionType ENTITY_PROPERTIES = LootConditionTypes.register("entity_properties", new EntityPropertiesLootCondition.Serializer());
    public static final LootConditionType KILLED_BY_PLAYER = LootConditionTypes.register("killed_by_player", new KilledByPlayerLootCondition.Serializer());
    public static final LootConditionType ENTITY_SCORES = LootConditionTypes.register("entity_scores", new EntityScoresLootCondition.Serializer());
    public static final LootConditionType BLOCK_STATE_PROPERTY = LootConditionTypes.register("block_state_property", new BlockStatePropertyLootCondition.Serializer());
    public static final LootConditionType MATCH_TOOL = LootConditionTypes.register("match_tool", new MatchToolLootCondition.Serializer());
    public static final LootConditionType TABLE_BONUS = LootConditionTypes.register("table_bonus", new TableBonusLootCondition.Serializer());
    public static final LootConditionType SURVIVES_EXPLOSION = LootConditionTypes.register("survives_explosion", new SurvivesExplosionLootCondition.Serializer());
    public static final LootConditionType DAMAGE_SOURCE_PROPERTIES = LootConditionTypes.register("damage_source_properties", new DamageSourcePropertiesLootCondition.Serializer());
    public static final LootConditionType LOCATION_CHECK = LootConditionTypes.register("location_check", new LocationCheckLootCondition.Serializer());
    public static final LootConditionType WEATHER_CHECK = LootConditionTypes.register("weather_check", new WeatherCheckLootCondition.Serializer());
    public static final LootConditionType REFERENCE = LootConditionTypes.register("reference", new ReferenceLootCondition.Serializer());
    public static final LootConditionType TIME_CHECK = LootConditionTypes.register("time_check", new TimeCheckLootCondition.Serializer());
    public static final LootConditionType VALUE_CHECK = LootConditionTypes.register("value_check", new ValueCheckLootCondition.Serializer());

    private static LootConditionType register(String id, JsonSerializer<? extends LootCondition> serializer) {
        return Registry.register(Registries.LOOT_CONDITION_TYPE, new Identifier(id), new LootConditionType(serializer));
    }

    public static Object createGsonSerializer() {
        return JsonSerializing.createSerializerBuilder(Registries.LOOT_CONDITION_TYPE, "condition", "condition", LootCondition::getType).build();
    }

    public static <T> Predicate<T> joinAnd(Predicate<T>[] predicates2) {
        switch (predicates2.length) {
            case 0: {
                return predicates -> true;
            }
            case 1: {
                return predicates2[0];
            }
            case 2: {
                return predicates2[0].and(predicates2[1]);
            }
        }
        return operand -> {
            for (Predicate predicate : predicates2) {
                if (predicate.test(operand)) continue;
                return false;
            }
            return true;
        };
    }

    public static <T> Predicate<T> joinOr(Predicate<T>[] predicates2) {
        switch (predicates2.length) {
            case 0: {
                return predicates -> false;
            }
            case 1: {
                return predicates2[0];
            }
            case 2: {
                return predicates2[0].or(predicates2[1]);
            }
        }
        return operand -> {
            for (Predicate predicate : predicates2) {
                if (!predicate.test(operand)) continue;
                return true;
            }
            return false;
        };
    }
}

