/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.Set;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;

public class ExplorationMapLootFunction
extends ConditionalLootFunction {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
    public static final String MANSION = "mansion";
    public static final MapIcon.Type DEFAULT_DECORATION = MapIcon.Type.MANSION;
    public static final byte field_31851 = 2;
    public static final int field_31852 = 50;
    public static final boolean field_31853 = true;
    final TagKey<Structure> destination;
    final MapIcon.Type decoration;
    final byte zoom;
    final int searchRadius;
    final boolean skipExistingChunks;

    ExplorationMapLootFunction(LootCondition[] conditions, TagKey<Structure> destination, MapIcon.Type decoration, byte zoom, int searchRadius, boolean skipExistingChunks) {
        super(conditions);
        this.destination = destination;
        this.decoration = decoration;
        this.zoom = zoom;
        this.searchRadius = searchRadius;
        this.skipExistingChunks = skipExistingChunks;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.EXPLORATION_MAP;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.ORIGIN);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        ServerWorld lv2;
        BlockPos lv3;
        if (!stack.isOf(Items.MAP)) {
            return stack;
        }
        Vec3d lv = context.get(LootContextParameters.ORIGIN);
        if (lv != null && (lv3 = (lv2 = context.getWorld()).locateStructure(this.destination, BlockPos.ofFloored(lv), this.searchRadius, this.skipExistingChunks)) != null) {
            ItemStack lv4 = FilledMapItem.createMap(lv2, lv3.getX(), lv3.getZ(), this.zoom, true, true);
            FilledMapItem.fillExplorationMap(lv2, lv4);
            MapState.addDecorationsNbt(lv4, lv3, "+", this.decoration);
            return lv4;
        }
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private TagKey<Structure> destination = DEFAULT_DESTINATION;
        private MapIcon.Type decoration = DEFAULT_DECORATION;
        private byte zoom = (byte)2;
        private int searchRadius = 50;
        private boolean skipExistingChunks = true;

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder withDestination(TagKey<Structure> destination) {
            this.destination = destination;
            return this;
        }

        public Builder withDecoration(MapIcon.Type decoration) {
            this.decoration = decoration;
            return this;
        }

        public Builder withZoom(byte zoom) {
            this.zoom = zoom;
            return this;
        }

        public Builder searchRadius(int searchRadius) {
            this.searchRadius = searchRadius;
            return this;
        }

        public Builder withSkipExistingChunks(boolean skipExistingChunks) {
            this.skipExistingChunks = skipExistingChunks;
            return this;
        }

        @Override
        public LootFunction build() {
            return new ExplorationMapLootFunction(this.getConditions(), this.destination, this.decoration, this.zoom, this.searchRadius, this.skipExistingChunks);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<ExplorationMapLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, ExplorationMapLootFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, arg, jsonSerializationContext);
            if (!arg.destination.equals(DEFAULT_DESTINATION)) {
                jsonObject.addProperty("destination", arg.destination.id().toString());
            }
            if (arg.decoration != DEFAULT_DECORATION) {
                jsonObject.add("decoration", jsonSerializationContext.serialize(arg.decoration.toString().toLowerCase(Locale.ROOT)));
            }
            if (arg.zoom != 2) {
                jsonObject.addProperty("zoom", arg.zoom);
            }
            if (arg.searchRadius != 50) {
                jsonObject.addProperty("search_radius", arg.searchRadius);
            }
            if (!arg.skipExistingChunks) {
                jsonObject.addProperty("skip_existing_chunks", arg.skipExistingChunks);
            }
        }

        @Override
        public ExplorationMapLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            TagKey<Structure> lv = Serializer.getDestination(jsonObject);
            String string = jsonObject.has("decoration") ? JsonHelper.getString(jsonObject, "decoration") : ExplorationMapLootFunction.MANSION;
            MapIcon.Type lv2 = DEFAULT_DECORATION;
            try {
                lv2 = MapIcon.Type.valueOf(string.toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", (Object)string, (Object)DEFAULT_DECORATION);
            }
            byte b = JsonHelper.getByte(jsonObject, "zoom", (byte)2);
            int i = JsonHelper.getInt(jsonObject, "search_radius", 50);
            boolean bl = JsonHelper.getBoolean(jsonObject, "skip_existing_chunks", true);
            return new ExplorationMapLootFunction(args, lv, lv2, b, i, bl);
        }

        private static TagKey<Structure> getDestination(JsonObject json) {
            if (json.has("destination")) {
                String string = JsonHelper.getString(json, "destination");
                return TagKey.of(RegistryKeys.STRUCTURE, new Identifier(string));
            }
            return DEFAULT_DESTINATION;
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

