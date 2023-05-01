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
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FluidPredicate {
    public static final FluidPredicate ANY = new FluidPredicate(null, null, StatePredicate.ANY);
    @Nullable
    private final TagKey<Fluid> tag;
    @Nullable
    private final Fluid fluid;
    private final StatePredicate state;

    public FluidPredicate(@Nullable TagKey<Fluid> tag, @Nullable Fluid fluid, StatePredicate state) {
        this.tag = tag;
        this.fluid = fluid;
        this.state = state;
    }

    public boolean test(ServerWorld world, BlockPos pos) {
        if (this == ANY) {
            return true;
        }
        if (!world.canSetBlock(pos)) {
            return false;
        }
        FluidState lv = world.getFluidState(pos);
        if (this.tag != null && !lv.isIn(this.tag)) {
            return false;
        }
        if (this.fluid != null && !lv.isOf(this.fluid)) {
            return false;
        }
        return this.state.test(lv);
    }

    public static FluidPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "fluid");
        Fluid lv = null;
        if (jsonObject.has("fluid")) {
            Identifier lv2 = new Identifier(JsonHelper.getString(jsonObject, "fluid"));
            lv = Registries.FLUID.get(lv2);
        }
        TagKey<Fluid> lv3 = null;
        if (jsonObject.has("tag")) {
            Identifier lv4 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
            lv3 = TagKey.of(RegistryKeys.FLUID, lv4);
        }
        StatePredicate lv5 = StatePredicate.fromJson(jsonObject.get("state"));
        return new FluidPredicate(lv3, lv, lv5);
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.fluid != null) {
            jsonObject.addProperty("fluid", Registries.FLUID.getId(this.fluid).toString());
        }
        if (this.tag != null) {
            jsonObject.addProperty("tag", this.tag.id().toString());
        }
        jsonObject.add("state", this.state.toJson());
        return jsonObject;
    }

    public static class Builder {
        @Nullable
        private Fluid fluid;
        @Nullable
        private TagKey<Fluid> tag;
        private StatePredicate state = StatePredicate.ANY;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder fluid(Fluid fluid) {
            this.fluid = fluid;
            return this;
        }

        public Builder tag(TagKey<Fluid> tag) {
            this.tag = tag;
            return this;
        }

        public Builder state(StatePredicate state) {
            this.state = state;
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.tag, this.fluid, this.state);
        }
    }
}

