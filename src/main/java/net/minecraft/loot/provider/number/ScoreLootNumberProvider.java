/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.provider.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.score.ContextLootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class ScoreLootNumberProvider
implements LootNumberProvider {
    final LootScoreProvider target;
    final String score;
    final float scale;

    ScoreLootNumberProvider(LootScoreProvider target, String score, float scale) {
        this.target = target;
        this.score = score;
        this.scale = scale;
    }

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.SCORE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.target.getRequiredParameters();
    }

    public static ScoreLootNumberProvider create(LootContext.EntityTarget target, String score) {
        return ScoreLootNumberProvider.create(target, score, 1.0f);
    }

    public static ScoreLootNumberProvider create(LootContext.EntityTarget target, String score, float scale) {
        return new ScoreLootNumberProvider(ContextLootScoreProvider.create(target), score, scale);
    }

    @Override
    public float nextFloat(LootContext context) {
        String string = this.target.getName(context);
        if (string == null) {
            return 0.0f;
        }
        ServerScoreboard lv = context.getWorld().getScoreboard();
        ScoreboardObjective lv2 = lv.getNullableObjective(this.score);
        if (lv2 == null) {
            return 0.0f;
        }
        if (!lv.playerHasObjective(string, lv2)) {
            return 0.0f;
        }
        return (float)lv.getPlayerScore(string, lv2).getScore() * this.scale;
    }

    public static class Serializer
    implements JsonSerializer<ScoreLootNumberProvider> {
        @Override
        public ScoreLootNumberProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            String string = JsonHelper.getString(jsonObject, "score");
            float f = JsonHelper.getFloat(jsonObject, "scale", 1.0f);
            LootScoreProvider lv = JsonHelper.deserialize(jsonObject, "target", jsonDeserializationContext, LootScoreProvider.class);
            return new ScoreLootNumberProvider(lv, string, f);
        }

        @Override
        public void toJson(JsonObject jsonObject, ScoreLootNumberProvider arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("score", arg.score);
            jsonObject.add("target", jsonSerializationContext.serialize(arg.target));
            jsonObject.addProperty("scale", Float.valueOf(arg.scale));
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

