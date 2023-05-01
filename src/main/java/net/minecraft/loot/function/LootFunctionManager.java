/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LootFunctionManager
extends JsonDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = LootGsons.getFunctionGsonBuilder().create();
    private final LootConditionManager lootConditionManager;
    private final LootManager lootManager;
    private Map<Identifier, LootFunction> functions = ImmutableMap.of();

    public LootFunctionManager(LootConditionManager lootConditionManager, LootManager lootManager) {
        super(GSON, "item_modifiers");
        this.lootConditionManager = lootConditionManager;
        this.lootManager = lootManager;
    }

    @Nullable
    public LootFunction get(Identifier id) {
        return this.functions.get(id);
    }

    public LootFunction getOrDefault(Identifier id, LootFunction fallback) {
        return this.functions.getOrDefault(id, fallback);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager arg, Profiler arg2) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        map.forEach((id, json) -> {
            try {
                if (json.isJsonArray()) {
                    LootFunction[] lvs = GSON.fromJson((JsonElement)json, LootFunction[].class);
                    builder.put(id, new AndFunction(lvs));
                } else {
                    LootFunction lv = GSON.fromJson((JsonElement)json, LootFunction.class);
                    builder.put(id, lv);
                }
            }
            catch (Exception exception) {
                LOGGER.error("Couldn't parse item modifier {}", id, (Object)exception);
            }
        });
        ImmutableMap<Identifier, LootFunction> map2 = builder.build();
        LootTableReporter lv = new LootTableReporter(LootContextTypes.GENERIC, this.lootConditionManager::get, this.lootManager::getTable);
        map2.forEach((id, function) -> function.validate(lv));
        lv.getMessages().forEach((name, message) -> LOGGER.warn("Found item modifier validation problem in {}: {}", name, message));
        this.functions = map2;
    }

    public Set<Identifier> getFunctionIds() {
        return Collections.unmodifiableSet(this.functions.keySet());
    }

    static class AndFunction
    implements LootFunction {
        protected final LootFunction[] functions;
        private final BiFunction<ItemStack, LootContext, ItemStack> applier;

        public AndFunction(LootFunction[] functions) {
            this.functions = functions;
            this.applier = LootFunctionTypes.join(functions);
        }

        @Override
        public ItemStack apply(ItemStack arg, LootContext arg2) {
            return this.applier.apply(arg, arg2);
        }

        @Override
        public LootFunctionType getType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public /* synthetic */ Object apply(Object stack, Object context) {
            return this.apply((ItemStack)stack, (LootContext)context);
        }
    }
}

