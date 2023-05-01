/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancementCriterion {
    @Nullable
    private final CriterionConditions conditions;

    public AdvancementCriterion(CriterionConditions conditions) {
        this.conditions = conditions;
    }

    public AdvancementCriterion() {
        this.conditions = null;
    }

    public void toPacket(PacketByteBuf buf) {
    }

    public static AdvancementCriterion fromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        Identifier lv = new Identifier(JsonHelper.getString(obj, "trigger"));
        Criterion lv2 = Criteria.getById(lv);
        if (lv2 == null) {
            throw new JsonSyntaxException("Invalid criterion trigger: " + lv);
        }
        Object lv3 = lv2.conditionsFromJson(JsonHelper.getObject(obj, "conditions", new JsonObject()), predicateDeserializer);
        return new AdvancementCriterion((CriterionConditions)lv3);
    }

    public static AdvancementCriterion fromPacket(PacketByteBuf buf) {
        return new AdvancementCriterion();
    }

    public static Map<String, AdvancementCriterion> criteriaFromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        HashMap<String, AdvancementCriterion> map = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            map.put(entry.getKey(), AdvancementCriterion.fromJson(JsonHelper.asObject(entry.getValue(), "criterion"), predicateDeserializer));
        }
        return map;
    }

    public static Map<String, AdvancementCriterion> criteriaFromPacket(PacketByteBuf buf) {
        return buf.readMap(PacketByteBuf::readString, AdvancementCriterion::fromPacket);
    }

    public static void criteriaToPacket(Map<String, AdvancementCriterion> criteria, PacketByteBuf buf2) {
        buf2.writeMap(criteria, PacketByteBuf::writeString, (buf, criterion) -> criterion.toPacket((PacketByteBuf)buf));
    }

    @Nullable
    public CriterionConditions getConditions() {
        return this.conditions;
    }

    public JsonElement toJson() {
        if (this.conditions == null) {
            throw new JsonSyntaxException("Missing trigger");
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trigger", this.conditions.getId().toString());
        JsonObject jsonObject2 = this.conditions.toJson(AdvancementEntityPredicateSerializer.INSTANCE);
        if (jsonObject2.size() != 0) {
            jsonObject.add("conditions", jsonObject2);
        }
        return jsonObject;
    }
}

