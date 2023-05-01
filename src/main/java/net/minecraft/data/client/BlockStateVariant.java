/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.data.client;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.data.client.VariantSetting;

public class BlockStateVariant
implements Supplier<JsonElement> {
    private final Map<VariantSetting<?>, VariantSetting.Value> properties = Maps.newLinkedHashMap();

    public <T> BlockStateVariant put(VariantSetting<T> key, T value) {
        VariantSetting.Value lv = this.properties.put(key, key.evaluate(value));
        if (lv != null) {
            throw new IllegalStateException("Replacing value of " + lv + " with " + value);
        }
        return this;
    }

    public static BlockStateVariant create() {
        return new BlockStateVariant();
    }

    public static BlockStateVariant union(BlockStateVariant first, BlockStateVariant second) {
        BlockStateVariant lv = new BlockStateVariant();
        lv.properties.putAll(first.properties);
        lv.properties.putAll(second.properties);
        return lv;
    }

    @Override
    public JsonElement get() {
        JsonObject jsonObject = new JsonObject();
        this.properties.values().forEach(value -> value.writeTo(jsonObject));
        return jsonObject;
    }

    public static JsonElement toJson(List<BlockStateVariant> variants) {
        if (variants.size() == 1) {
            return variants.get(0).get();
        }
        JsonArray jsonArray = new JsonArray();
        variants.forEach(variant -> jsonArray.add(variant.get()));
        return jsonArray;
    }

    @Override
    public /* synthetic */ Object get() {
        return this.get();
    }
}

