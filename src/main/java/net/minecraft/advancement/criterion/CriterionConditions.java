/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.util.Identifier;

public interface CriterionConditions {
    public Identifier getId();

    public JsonObject toJson(AdvancementEntityPredicateSerializer var1);
}

