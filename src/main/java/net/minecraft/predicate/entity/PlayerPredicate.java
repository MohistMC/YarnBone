/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.TypeSpecificPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class PlayerPredicate
implements TypeSpecificPredicate {
    public static final int field_33928 = 100;
    private final NumberRange.IntRange experienceLevel;
    @Nullable
    private final GameMode gameMode;
    private final Map<Stat<?>, NumberRange.IntRange> stats;
    private final Object2BooleanMap<Identifier> recipes;
    private final Map<Identifier, AdvancementPredicate> advancements;
    private final EntityPredicate lookingAt;

    private static AdvancementPredicate criterionFromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            boolean bl = json.getAsBoolean();
            return new CompletedAdvancementPredicate(bl);
        }
        Object2BooleanOpenHashMap<String> object2BooleanMap = new Object2BooleanOpenHashMap<String>();
        JsonObject jsonObject = JsonHelper.asObject(json, "criterion data");
        jsonObject.entrySet().forEach(entry -> {
            boolean bl = JsonHelper.asBoolean((JsonElement)entry.getValue(), "criterion test");
            object2BooleanMap.put((String)entry.getKey(), bl);
        });
        return new AdvancementCriteriaPredicate(object2BooleanMap);
    }

    PlayerPredicate(NumberRange.IntRange experienceLevel, @Nullable GameMode gameMode, Map<Stat<?>, NumberRange.IntRange> stats, Object2BooleanMap<Identifier> recipes, Map<Identifier, AdvancementPredicate> advancements, EntityPredicate lookingAt) {
        this.experienceLevel = experienceLevel;
        this.gameMode = gameMode;
        this.stats = stats;
        this.recipes = recipes;
        this.advancements = advancements;
        this.lookingAt = lookingAt;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof ServerPlayerEntity)) {
            return false;
        }
        ServerPlayerEntity lv = (ServerPlayerEntity)entity;
        if (!this.experienceLevel.test(lv.experienceLevel)) {
            return false;
        }
        if (this.gameMode != null && this.gameMode != lv.interactionManager.getGameMode()) {
            return false;
        }
        ServerStatHandler lv2 = lv.getStatHandler();
        for (Map.Entry<Stat<?>, NumberRange.IntRange> entry : this.stats.entrySet()) {
            int n = lv2.getStat(entry.getKey());
            if (entry.getValue().test(n)) continue;
            return false;
        }
        ServerRecipeBook lv3 = lv.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (lv3.contains((Identifier)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancementTracker playerAdvancementTracker = lv.getAdvancementTracker();
            ServerAdvancementLoader serverAdvancementLoader = lv.getServer().getAdvancementLoader();
            for (Map.Entry<Identifier, AdvancementPredicate> entry3 : this.advancements.entrySet()) {
                Advancement lv6 = serverAdvancementLoader.get(entry3.getKey());
                if (lv6 != null && entry3.getValue().test(playerAdvancementTracker.getProgress(lv6))) continue;
                return false;
            }
        }
        if (this.lookingAt != EntityPredicate.ANY) {
            Vec3d vec3d = lv.getEyePos();
            Vec3d vec3d2 = lv.getRotationVec(1.0f);
            Vec3d lv9 = vec3d.add(vec3d2.x * 100.0, vec3d2.y * 100.0, vec3d2.z * 100.0);
            EntityHitResult lv10 = ProjectileUtil.getEntityCollision(lv.world, lv, vec3d, lv9, new Box(vec3d, lv9).expand(1.0), arg -> !arg.isSpectator(), 0.0f);
            if (lv10 == null || lv10.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            Entity lv11 = lv10.getEntity();
            if (!this.lookingAt.test(lv, lv11) || !lv.canSee(lv11)) {
                return false;
            }
        }
        return true;
    }

    public static PlayerPredicate fromJson(JsonObject json) {
        NumberRange.IntRange lv = NumberRange.IntRange.fromJson(json.get("level"));
        String string = JsonHelper.getString(json, "gamemode", "");
        GameMode lv2 = GameMode.byName(string, null);
        HashMap<Stat<?>, NumberRange.IntRange> map = Maps.newHashMap();
        JsonArray jsonArray = JsonHelper.getArray(json, "stats", null);
        if (jsonArray != null) {
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject2 = JsonHelper.asObject(jsonElement, "stats entry");
                Identifier identifier = new Identifier(JsonHelper.getString(jsonObject2, "type"));
                StatType<?> lv4 = Registries.STAT_TYPE.get(identifier);
                if (lv4 == null) {
                    throw new JsonParseException("Invalid stat type: " + identifier);
                }
                Identifier identifier2 = new Identifier(JsonHelper.getString(jsonObject2, "stat"));
                Stat<?> lv6 = PlayerPredicate.getStat(lv4, identifier2);
                NumberRange.IntRange lv7 = NumberRange.IntRange.fromJson(jsonObject2.get("value"));
                map.put(lv6, lv7);
            }
        }
        Object2BooleanOpenHashMap<Identifier> object2BooleanMap = new Object2BooleanOpenHashMap<Identifier>();
        JsonObject jsonObject3 = JsonHelper.getObject(json, "recipes", new JsonObject());
        for (Map.Entry entry : jsonObject3.entrySet()) {
            Identifier lv8 = new Identifier((String)entry.getKey());
            boolean bl = JsonHelper.asBoolean((JsonElement)entry.getValue(), "recipe present");
            object2BooleanMap.put(lv8, bl);
        }
        HashMap<Identifier, AdvancementPredicate> map2 = Maps.newHashMap();
        JsonObject jsonObject = JsonHelper.getObject(json, "advancements", new JsonObject());
        for (Map.Entry entry : jsonObject.entrySet()) {
            Identifier lv9 = new Identifier((String)entry.getKey());
            AdvancementPredicate lv10 = PlayerPredicate.criterionFromJson((JsonElement)entry.getValue());
            map2.put(lv9, lv10);
        }
        EntityPredicate lv11 = EntityPredicate.fromJson(json.get("looking_at"));
        return new PlayerPredicate(lv, lv2, map, object2BooleanMap, map2, lv11);
    }

    private static <T> Stat<T> getStat(StatType<T> type, Identifier id) {
        Registry<T> lv = type.getRegistry();
        T object = lv.get(id);
        if (object == null) {
            throw new JsonParseException("Unknown object " + id + " for stat type " + Registries.STAT_TYPE.getId(type));
        }
        return type.getOrCreateStat(object);
    }

    private static <T> Identifier getStatId(Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }

    @Override
    public JsonObject typeSpecificToJson() {
        JsonObject jsonObject2;
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("level", this.experienceLevel.toJson());
        if (this.gameMode != null) {
            jsonObject.addProperty("gamemode", this.gameMode.getName());
        }
        if (!this.stats.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            this.stats.forEach((stat, arg2) -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", Registries.STAT_TYPE.getId(stat.getType()).toString());
                jsonObject.addProperty("stat", PlayerPredicate.getStatId(stat).toString());
                jsonObject.add("value", arg2.toJson());
                jsonArray.add(jsonObject);
            });
            jsonObject.add("stats", jsonArray);
        }
        if (!this.recipes.isEmpty()) {
            jsonObject2 = new JsonObject();
            this.recipes.forEach((id, boolean_) -> jsonObject2.addProperty(id.toString(), (Boolean)boolean_));
            jsonObject.add("recipes", jsonObject2);
        }
        if (!this.advancements.isEmpty()) {
            jsonObject2 = new JsonObject();
            this.advancements.forEach((id, arg2) -> jsonObject2.add(id.toString(), arg2.toJson()));
            jsonObject.add("advancements", jsonObject2);
        }
        jsonObject.add("looking_at", this.lookingAt.toJson());
        return jsonObject;
    }

    @Override
    public TypeSpecificPredicate.Deserializer getDeserializer() {
        return TypeSpecificPredicate.Deserializers.PLAYER;
    }

    static class CompletedAdvancementPredicate
    implements AdvancementPredicate {
        private final boolean done;

        public CompletedAdvancementPredicate(boolean done) {
            this.done = done;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.done);
        }

        @Override
        public boolean test(AdvancementProgress arg) {
            return arg.isDone() == this.done;
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }

    static class AdvancementCriteriaPredicate
    implements AdvancementPredicate {
        private final Object2BooleanMap<String> criteria;

        public AdvancementCriteriaPredicate(Object2BooleanMap<String> criteria) {
            this.criteria = criteria;
        }

        @Override
        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();
            this.criteria.forEach(jsonObject::addProperty);
            return jsonObject;
        }

        @Override
        public boolean test(AdvancementProgress arg) {
            for (Object2BooleanMap.Entry entry : this.criteria.object2BooleanEntrySet()) {
                CriterionProgress lv = arg.getCriterionProgress((String)entry.getKey());
                if (lv != null && lv.isObtained() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public JsonElement toJson();
    }

    public static class Builder {
        private NumberRange.IntRange experienceLevel = NumberRange.IntRange.ANY;
        @Nullable
        private GameMode gameMode;
        private final Map<Stat<?>, NumberRange.IntRange> stats = Maps.newHashMap();
        private final Object2BooleanMap<Identifier> recipes = new Object2BooleanOpenHashMap<Identifier>();
        private final Map<Identifier, AdvancementPredicate> advancements = Maps.newHashMap();
        private EntityPredicate lookingAt = EntityPredicate.ANY;

        public static Builder create() {
            return new Builder();
        }

        public Builder experienceLevel(NumberRange.IntRange experienceLevel) {
            this.experienceLevel = experienceLevel;
            return this;
        }

        public Builder stat(Stat<?> stat, NumberRange.IntRange value) {
            this.stats.put(stat, value);
            return this;
        }

        public Builder recipe(Identifier id, boolean unlocked) {
            this.recipes.put(id, unlocked);
            return this;
        }

        public Builder gameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder lookingAt(EntityPredicate lookingAt) {
            this.lookingAt = lookingAt;
            return this;
        }

        public Builder advancement(Identifier id, boolean done) {
            this.advancements.put(id, new CompletedAdvancementPredicate(done));
            return this;
        }

        public Builder advancement(Identifier id, Map<String, Boolean> criteria) {
            this.advancements.put(id, new AdvancementCriteriaPredicate(new Object2BooleanOpenHashMap<String>(criteria)));
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.experienceLevel, this.gameMode, this.stats, this.recipes, this.advancements, this.lookingAt);
        }
    }
}

