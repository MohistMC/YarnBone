/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockPredicate {
    public static final BlockPredicate ANY = new BlockPredicate(null, null, StatePredicate.ANY, NbtPredicate.ANY);
    @Nullable
    private final TagKey<Block> tag;
    @Nullable
    private final Set<Block> blocks;
    private final StatePredicate state;
    private final NbtPredicate nbt;

    public BlockPredicate(@Nullable TagKey<Block> tag, @Nullable Set<Block> blocks, StatePredicate state, NbtPredicate nbt) {
        this.tag = tag;
        this.blocks = blocks;
        this.state = state;
        this.nbt = nbt;
    }

    public boolean test(ServerWorld world, BlockPos pos) {
        BlockEntity lv2;
        if (this == ANY) {
            return true;
        }
        if (!world.canSetBlock(pos)) {
            return false;
        }
        BlockState lv = world.getBlockState(pos);
        if (this.tag != null && !lv.isIn(this.tag)) {
            return false;
        }
        if (this.blocks != null && !this.blocks.contains(lv.getBlock())) {
            return false;
        }
        if (!this.state.test(lv)) {
            return false;
        }
        return this.nbt == NbtPredicate.ANY || (lv2 = world.getBlockEntity(pos)) != null && this.nbt.test(lv2.createNbtWithIdentifyingData());
    }

    public static BlockPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "block");
        NbtPredicate lv = NbtPredicate.fromJson(jsonObject.get("nbt"));
        ImmutableCollection set = null;
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "blocks", null);
        if (jsonArray != null) {
            ImmutableSet.Builder builder = ImmutableSet.builder();
            for (JsonElement jsonElement2 : jsonArray) {
                Identifier lv2 = new Identifier(JsonHelper.asString(jsonElement2, "block"));
                builder.add((Block)Registries.BLOCK.getOrEmpty(lv2).orElseThrow(() -> new JsonSyntaxException("Unknown block id '" + lv2 + "'")));
            }
            set = builder.build();
        }
        TagKey<Block> lv3 = null;
        if (jsonObject.has("tag")) {
            Identifier lv4 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
            lv3 = TagKey.of(RegistryKeys.BLOCK, lv4);
        }
        StatePredicate lv5 = StatePredicate.fromJson(jsonObject.get("state"));
        return new BlockPredicate(lv3, (Set<Block>)((Object)set), lv5, lv);
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.blocks != null) {
            JsonArray jsonArray = new JsonArray();
            for (Block lv : this.blocks) {
                jsonArray.add(Registries.BLOCK.getId(lv).toString());
            }
            jsonObject.add("blocks", jsonArray);
        }
        if (this.tag != null) {
            jsonObject.addProperty("tag", this.tag.id().toString());
        }
        jsonObject.add("nbt", this.nbt.toJson());
        jsonObject.add("state", this.state.toJson());
        return jsonObject;
    }

    public static class Builder {
        @Nullable
        private Set<Block> blocks;
        @Nullable
        private TagKey<Block> tag;
        private StatePredicate state = StatePredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder blocks(Block ... blocks) {
            this.blocks = ImmutableSet.copyOf(blocks);
            return this;
        }

        public Builder blocks(Iterable<Block> blocks) {
            this.blocks = ImmutableSet.copyOf(blocks);
            return this;
        }

        public Builder tag(TagKey<Block> tag) {
            this.tag = tag;
            return this;
        }

        public Builder nbt(NbtCompound nbt) {
            this.nbt = new NbtPredicate(nbt);
            return this;
        }

        public Builder state(StatePredicate state) {
            this.state = state;
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.tag, this.blocks, this.state, this.nbt);
        }
    }
}

