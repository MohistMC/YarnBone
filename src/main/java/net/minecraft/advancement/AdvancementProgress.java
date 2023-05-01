/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancementProgress
implements Comparable<AdvancementProgress> {
    final Map<String, CriterionProgress> criteriaProgresses;
    private String[][] requirements = new String[0][];

    private AdvancementProgress(Map<String, CriterionProgress> criteriaProgresses) {
        this.criteriaProgresses = criteriaProgresses;
    }

    public AdvancementProgress() {
        this.criteriaProgresses = Maps.newHashMap();
    }

    public void init(Map<String, AdvancementCriterion> criteria, String[][] requirements) {
        Set<String> set = criteria.keySet();
        this.criteriaProgresses.entrySet().removeIf(progress -> !set.contains(progress.getKey()));
        for (String string : set) {
            if (this.criteriaProgresses.containsKey(string)) continue;
            this.criteriaProgresses.put(string, new CriterionProgress());
        }
        this.requirements = requirements;
    }

    public boolean isDone() {
        if (this.requirements.length == 0) {
            return false;
        }
        for (String[] strings : this.requirements) {
            boolean bl = false;
            for (String string : strings) {
                CriterionProgress lv = this.getCriterionProgress(string);
                if (lv == null || !lv.isObtained()) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            return false;
        }
        return true;
    }

    public boolean isAnyObtained() {
        for (CriterionProgress lv : this.criteriaProgresses.values()) {
            if (!lv.isObtained()) continue;
            return true;
        }
        return false;
    }

    public boolean obtain(String name) {
        CriterionProgress lv = this.criteriaProgresses.get(name);
        if (lv != null && !lv.isObtained()) {
            lv.obtain();
            return true;
        }
        return false;
    }

    public boolean reset(String name) {
        CriterionProgress lv = this.criteriaProgresses.get(name);
        if (lv != null && lv.isObtained()) {
            lv.reset();
            return true;
        }
        return false;
    }

    public String toString() {
        return "AdvancementProgress{criteria=" + this.criteriaProgresses + ", requirements=" + Arrays.deepToString((Object[])this.requirements) + "}";
    }

    public void toPacket(PacketByteBuf buf2) {
        buf2.writeMap(this.criteriaProgresses, PacketByteBuf::writeString, (buf, progresses) -> progresses.toPacket((PacketByteBuf)buf));
    }

    public static AdvancementProgress fromPacket(PacketByteBuf buf) {
        Map<String, CriterionProgress> map = buf.readMap(PacketByteBuf::readString, CriterionProgress::fromPacket);
        return new AdvancementProgress(map);
    }

    @Nullable
    public CriterionProgress getCriterionProgress(String name) {
        return this.criteriaProgresses.get(name);
    }

    public float getProgressBarPercentage() {
        if (this.criteriaProgresses.isEmpty()) {
            return 0.0f;
        }
        float f = this.requirements.length;
        float g = this.countObtainedRequirements();
        return g / f;
    }

    @Nullable
    public String getProgressBarFraction() {
        if (this.criteriaProgresses.isEmpty()) {
            return null;
        }
        int i = this.requirements.length;
        if (i <= 1) {
            return null;
        }
        int j = this.countObtainedRequirements();
        return j + "/" + i;
    }

    private int countObtainedRequirements() {
        int i = 0;
        for (String[] strings : this.requirements) {
            boolean bl = false;
            for (String string : strings) {
                CriterionProgress lv = this.getCriterionProgress(string);
                if (lv == null || !lv.isObtained()) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            ++i;
        }
        return i;
    }

    public Iterable<String> getUnobtainedCriteria() {
        ArrayList<String> list = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> entry : this.criteriaProgresses.entrySet()) {
            if (entry.getValue().isObtained()) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    public Iterable<String> getObtainedCriteria() {
        ArrayList<String> list = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> entry : this.criteriaProgresses.entrySet()) {
            if (!entry.getValue().isObtained()) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    @Nullable
    public Date getEarliestProgressObtainDate() {
        Date date = null;
        for (CriterionProgress lv : this.criteriaProgresses.values()) {
            if (!lv.isObtained() || date != null && !lv.getObtainedDate().before(date)) continue;
            date = lv.getObtainedDate();
        }
        return date;
    }

    @Override
    public int compareTo(AdvancementProgress arg) {
        Date date = this.getEarliestProgressObtainDate();
        Date date2 = arg.getEarliestProgressObtainDate();
        if (date == null && date2 != null) {
            return 1;
        }
        if (date != null && date2 == null) {
            return -1;
        }
        if (date == null && date2 == null) {
            return 0;
        }
        return date.compareTo(date2);
    }

    public static class Serializer
    implements JsonDeserializer<AdvancementProgress>,
    JsonSerializer<AdvancementProgress> {
        @Override
        public JsonElement serialize(AdvancementProgress arg, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            JsonObject jsonObject2 = new JsonObject();
            for (Map.Entry<String, CriterionProgress> entry : arg.criteriaProgresses.entrySet()) {
                CriterionProgress lv = entry.getValue();
                if (!lv.isObtained()) continue;
                jsonObject2.add(entry.getKey(), lv.toJson());
            }
            if (!jsonObject2.entrySet().isEmpty()) {
                jsonObject.add("criteria", jsonObject2);
            }
            jsonObject.addProperty("done", arg.isDone());
            return jsonObject;
        }

        @Override
        public AdvancementProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = JsonHelper.asObject(jsonElement, "advancement");
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "criteria", new JsonObject());
            AdvancementProgress lv = new AdvancementProgress();
            for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                String string = entry.getKey();
                lv.criteriaProgresses.put(string, CriterionProgress.obtainedAt(JsonHelper.asString(entry.getValue(), string)));
            }
            return lv;
        }
    }
}

