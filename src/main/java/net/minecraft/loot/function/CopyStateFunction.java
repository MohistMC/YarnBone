/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CopyStateFunction
extends ConditionalLootFunction {
    final Block block;
    final Set<Property<?>> properties;

    CopyStateFunction(LootCondition[] conditions, Block block, Set<Property<?>> properties) {
        super(conditions);
        this.block = block;
        this.properties = properties;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.COPY_STATE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.BLOCK_STATE);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        BlockState lv = context.get(LootContextParameters.BLOCK_STATE);
        if (lv != null) {
            NbtCompound lv3;
            NbtCompound lv2 = stack.getOrCreateNbt();
            if (lv2.contains("BlockStateTag", NbtElement.COMPOUND_TYPE)) {
                lv3 = lv2.getCompound("BlockStateTag");
            } else {
                lv3 = new NbtCompound();
                lv2.put("BlockStateTag", lv3);
            }
            this.properties.stream().filter(lv::contains).forEach(property -> lv3.putString(property.getName(), CopyStateFunction.getPropertyName(lv, property)));
        }
        return stack;
    }

    public static Builder builder(Block block) {
        return new Builder(block);
    }

    private static <T extends Comparable<T>> String getPropertyName(BlockState state, Property<T> property) {
        T comparable = state.get(property);
        return property.name(comparable);
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final Block block;
        private final Set<Property<?>> properties = Sets.newHashSet();

        Builder(Block block) {
            this.block = block;
        }

        public Builder addProperty(Property<?> property) {
            if (!this.block.getStateManager().getProperties().contains(property)) {
                throw new IllegalStateException("Property " + property + " is not present on block " + this.block);
            }
            this.properties.add(property);
            return this;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new CopyStateFunction(this.getConditions(), this.block, this.properties);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<CopyStateFunction> {
        @Override
        public void toJson(JsonObject jsonObject, CopyStateFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, arg, jsonSerializationContext);
            jsonObject.addProperty("block", Registries.BLOCK.getId(arg.block).toString());
            JsonArray jsonArray = new JsonArray();
            arg.properties.forEach(property -> jsonArray.add(property.getName()));
            jsonObject.add("properties", jsonArray);
        }

        @Override
        public CopyStateFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "block"));
            Block lv2 = (Block)Registries.BLOCK.getOrEmpty(lv).orElseThrow(() -> new IllegalArgumentException("Can't find block " + lv));
            StateManager<Block, BlockState> lv3 = lv2.getStateManager();
            HashSet<Property<?>> set = Sets.newHashSet();
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "properties", null);
            if (jsonArray != null) {
                jsonArray.forEach(property -> set.add(lv3.getProperty(JsonHelper.asString(property, "property"))));
            }
            return new CopyStateFunction(args, lv2, set);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

