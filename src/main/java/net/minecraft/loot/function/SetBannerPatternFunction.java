/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetBannerPatternFunction
extends ConditionalLootFunction {
    final List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns;
    final boolean append;

    SetBannerPatternFunction(LootCondition[] conditions, List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns, boolean append) {
        super(conditions);
        this.patterns = patterns;
        this.append = append;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        NbtList lv4;
        NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
        if (lv == null) {
            lv = new NbtCompound();
        }
        BannerPattern.Patterns lv2 = new BannerPattern.Patterns();
        this.patterns.forEach(lv2::add);
        NbtList lv3 = lv2.toNbt();
        if (this.append) {
            lv4 = lv.getList("Patterns", NbtElement.COMPOUND_TYPE).copy();
            lv4.addAll(lv3);
        } else {
            lv4 = lv3;
        }
        lv.put("Patterns", lv4);
        BlockItem.setBlockEntityNbt(stack, BlockEntityType.BANNER, lv);
        return stack;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_BANNER_PATTERN;
    }

    public static Builder builder(boolean append) {
        return new Builder(append);
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ImmutableList.Builder<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns = ImmutableList.builder();
        private final boolean append;

        Builder(boolean append) {
            this.append = append;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), (List<Pair<RegistryEntry<BannerPattern>, DyeColor>>)((Object)this.patterns.build()), this.append);
        }

        public Builder pattern(RegistryKey<BannerPattern> pattern, DyeColor color) {
            return this.pattern(Registries.BANNER_PATTERN.entryOf(pattern), color);
        }

        public Builder pattern(RegistryEntry<BannerPattern> pattern, DyeColor color) {
            this.patterns.add((Object)Pair.of(pattern, color));
            return this;
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetBannerPatternFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetBannerPatternFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, arg, jsonSerializationContext);
            JsonArray jsonArray = new JsonArray();
            arg.patterns.forEach(pair -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("pattern", ((RegistryEntry)pair.getFirst()).getKey().orElseThrow(() -> new JsonSyntaxException("Unknown pattern: " + pair.getFirst())).getValue().toString());
                jsonObject.addProperty("color", ((DyeColor)pair.getSecond()).getName());
                jsonArray.add(jsonObject);
            });
            jsonObject.add("patterns", jsonArray);
            jsonObject.addProperty("append", arg.append);
        }

        @Override
        public SetBannerPatternFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            ImmutableList.Builder builder = ImmutableList.builder();
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "patterns");
            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonObject jsonObject2 = JsonHelper.asObject(jsonArray.get(i), "pattern[" + i + "]");
                String string = JsonHelper.getString(jsonObject2, "pattern");
                Optional<RegistryEntry.Reference<BannerPattern>> optional = Registries.BANNER_PATTERN.getEntry(RegistryKey.of(RegistryKeys.BANNER_PATTERN, new Identifier(string)));
                if (optional.isEmpty()) {
                    throw new JsonSyntaxException("Unknown pattern: " + string);
                }
                String string2 = JsonHelper.getString(jsonObject2, "color");
                DyeColor lv = DyeColor.byName(string2, null);
                if (lv == null) {
                    throw new JsonSyntaxException("Unknown color: " + string2);
                }
                builder.add(Pair.of((RegistryEntry)optional.get(), lv));
            }
            boolean bl = JsonHelper.getBoolean(jsonObject, "append");
            return new SetBannerPatternFunction(args, (List<Pair<RegistryEntry<BannerPattern>, DyeColor>>)((Object)builder.build()), bl);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

