/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class LightPredicate {
    public static final LightPredicate ANY = new LightPredicate(NumberRange.IntRange.ANY);
    private final NumberRange.IntRange range;

    LightPredicate(NumberRange.IntRange range) {
        this.range = range;
    }

    public boolean test(ServerWorld world, BlockPos pos) {
        if (this == ANY) {
            return true;
        }
        if (!world.canSetBlock(pos)) {
            return false;
        }
        return this.range.test(world.getLightLevel(pos));
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("light", this.range.toJson());
        return jsonObject;
    }

    public static LightPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "light");
        NumberRange.IntRange lv = NumberRange.IntRange.fromJson(jsonObject.get("light"));
        return new LightPredicate(lv);
    }

    public static class Builder {
        private NumberRange.IntRange light = NumberRange.IntRange.ANY;

        public static Builder create() {
            return new Builder();
        }

        public Builder light(NumberRange.IntRange light) {
            this.light = light;
            return this;
        }

        public LightPredicate build() {
            return new LightPredicate(this.light);
        }
    }
}

