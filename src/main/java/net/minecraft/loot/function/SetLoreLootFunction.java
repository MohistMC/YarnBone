/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.function.SetNameLootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class SetLoreLootFunction
extends ConditionalLootFunction {
    final boolean replace;
    final List<Text> lore;
    @Nullable
    final LootContext.EntityTarget entity;

    public SetLoreLootFunction(LootCondition[] conditions, boolean replace, List<Text> lore, @Nullable LootContext.EntityTarget entity) {
        super(conditions);
        this.replace = replace;
        this.lore = ImmutableList.copyOf(lore);
        this.entity = entity;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_LORE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.entity != null ? ImmutableSet.of(this.entity.getParameter()) : ImmutableSet.of();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        NbtList lv = this.getLoreForMerge(stack, !this.lore.isEmpty());
        if (lv != null) {
            if (this.replace) {
                lv.clear();
            }
            UnaryOperator<Text> unaryOperator = SetNameLootFunction.applySourceEntity(context, this.entity);
            this.lore.stream().map(unaryOperator).map(Text.Serializer::toJson).map(NbtString::of).forEach(lv::add);
        }
        return stack;
    }

    @Nullable
    private NbtList getLoreForMerge(ItemStack stack, boolean otherLoreExists) {
        NbtCompound lv2;
        NbtCompound lv;
        if (stack.hasNbt()) {
            lv = stack.getNbt();
        } else if (otherLoreExists) {
            lv = new NbtCompound();
            stack.setNbt(lv);
        } else {
            return null;
        }
        if (lv.contains("display", NbtElement.COMPOUND_TYPE)) {
            lv2 = lv.getCompound("display");
        } else if (otherLoreExists) {
            lv2 = new NbtCompound();
            lv.put("display", lv2);
        } else {
            return null;
        }
        if (lv2.contains("Lore", NbtElement.LIST_TYPE)) {
            return lv2.getList("Lore", NbtElement.STRING_TYPE);
        }
        if (otherLoreExists) {
            NbtList lv3 = new NbtList();
            lv2.put("Lore", lv3);
            return lv3;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private boolean replace;
        private LootContext.EntityTarget target;
        private final List<Text> lore = Lists.newArrayList();

        public Builder replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public Builder target(LootContext.EntityTarget target) {
            this.target = target;
            return this;
        }

        public Builder lore(Text lore) {
            this.lore.add(lore);
            return this;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetLoreLootFunction(this.getConditions(), this.replace, this.lore, this.target);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetLoreLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetLoreLootFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, arg, jsonSerializationContext);
            jsonObject.addProperty("replace", arg.replace);
            JsonArray jsonArray = new JsonArray();
            for (Text lv : arg.lore) {
                jsonArray.add(Text.Serializer.toJsonTree(lv));
            }
            jsonObject.add("lore", jsonArray);
            if (arg.entity != null) {
                jsonObject.add("entity", jsonSerializationContext.serialize((Object)arg.entity));
            }
        }

        @Override
        public SetLoreLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            boolean bl = JsonHelper.getBoolean(jsonObject, "replace", false);
            List list = Streams.stream(JsonHelper.getArray(jsonObject, "lore")).map(Text.Serializer::fromJson).collect(ImmutableList.toImmutableList());
            LootContext.EntityTarget lv = JsonHelper.deserialize(jsonObject, "entity", null, jsonDeserializationContext, LootContext.EntityTarget.class);
            return new SetLoreLootFunction(args, bl, list, lv);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

