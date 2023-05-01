/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.condition;

import net.minecraft.loot.condition.LootCondition;
import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.JsonSerializer;

public class LootConditionType
extends JsonSerializableType<LootCondition> {
    public LootConditionType(JsonSerializer<? extends LootCondition> arg) {
        super(arg);
    }
}

