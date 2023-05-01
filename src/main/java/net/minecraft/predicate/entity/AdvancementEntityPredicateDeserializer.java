/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.predicate.entity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class AdvancementEntityPredicateDeserializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier advancementId;
    private final LootConditionManager conditionManager;
    private final Gson gson = LootGsons.getConditionGsonBuilder().create();

    public AdvancementEntityPredicateDeserializer(Identifier advancementId, LootConditionManager conditionManager) {
        this.advancementId = advancementId;
        this.conditionManager = conditionManager;
    }

    public final LootCondition[] loadConditions(JsonArray array, String key, LootContextType contextType) {
        LootCondition[] lvs = this.gson.fromJson((JsonElement)array, LootCondition[].class);
        LootTableReporter lv = new LootTableReporter(contextType, this.conditionManager::get, tableId -> null);
        for (LootCondition lv2 : lvs) {
            lv2.validate(lv);
            lv.getMessages().forEach((name, message) -> LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", key, name, message));
        }
        return lvs;
    }

    public Identifier getAdvancementId() {
        return this.advancementId;
    }
}

