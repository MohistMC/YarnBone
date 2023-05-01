/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class LootTable {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final LootTable EMPTY = new LootTable(LootContextTypes.EMPTY, new LootPool[0], new LootFunction[0]);
    public static final LootContextType GENERIC = LootContextTypes.GENERIC;
    final LootContextType type;
    final LootPool[] pools;
    final LootFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunction;

    LootTable(LootContextType type, LootPool[] pools, LootFunction[] functions) {
        this.type = type;
        this.pools = pools;
        this.functions = functions;
        this.combinedFunction = LootFunctionTypes.join(functions);
    }

    public static Consumer<ItemStack> processStacks(LootContext context, Consumer<ItemStack> consumer) {
        return stack -> {
            if (!stack.isItemEnabled(context.getWorld().getEnabledFeatures())) {
                return;
            }
            if (stack.getCount() < stack.getMaxCount()) {
                consumer.accept((ItemStack)stack);
            } else {
                ItemStack lv;
                for (int i = stack.getCount(); i > 0; i -= lv.getCount()) {
                    lv = stack.copy();
                    lv.setCount(Math.min(stack.getMaxCount(), i));
                    consumer.accept(lv);
                }
            }
        };
    }

    public void generateUnprocessedLoot(LootContext context, Consumer<ItemStack> lootConsumer) {
        if (context.markActive(this)) {
            Consumer<ItemStack> consumer2 = LootFunction.apply(this.combinedFunction, lootConsumer, context);
            for (LootPool lv : this.pools) {
                lv.addGeneratedLoot(consumer2, context);
            }
            context.markInactive(this);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }
    }

    public void generateLoot(LootContext context, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(context, LootTable.processStacks(context, lootConsumer));
    }

    public ObjectArrayList<ItemStack> generateLoot(LootContext context) {
        ObjectArrayList<ItemStack> objectArrayList = new ObjectArrayList<ItemStack>();
        this.generateLoot(context, objectArrayList::add);
        return objectArrayList;
    }

    public LootContextType getType() {
        return this.type;
    }

    public void validate(LootTableReporter reporter) {
        int i;
        for (i = 0; i < this.pools.length; ++i) {
            this.pools[i].validate(reporter.makeChild(".pools[" + i + "]"));
        }
        for (i = 0; i < this.functions.length; ++i) {
            this.functions[i].validate(reporter.makeChild(".functions[" + i + "]"));
        }
    }

    public void supplyInventory(Inventory inventory, LootContext context) {
        ObjectArrayList<ItemStack> objectArrayList = this.generateLoot(context);
        Random lv = context.getRandom();
        List<Integer> list = this.getFreeSlots(inventory, lv);
        this.shuffle(objectArrayList, list.size(), lv);
        for (ItemStack lv2 : objectArrayList) {
            if (list.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }
            if (lv2.isEmpty()) {
                inventory.setStack(list.remove(list.size() - 1), ItemStack.EMPTY);
                continue;
            }
            inventory.setStack(list.remove(list.size() - 1), lv2);
        }
    }

    private void shuffle(ObjectArrayList<ItemStack> drops, int freeSlots, Random random) {
        ArrayList<ItemStack> list = Lists.newArrayList();
        ObjectIterator iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack lv = (ItemStack)iterator.next();
            if (lv.isEmpty()) {
                iterator.remove();
                continue;
            }
            if (lv.getCount() <= 1) continue;
            list.add(lv);
            iterator.remove();
        }
        while (freeSlots - drops.size() - list.size() > 0 && !list.isEmpty()) {
            ItemStack lv2 = (ItemStack)list.remove(MathHelper.nextInt(random, 0, list.size() - 1));
            int j = MathHelper.nextInt(random, 1, lv2.getCount() / 2);
            ItemStack lv3 = lv2.split(j);
            if (lv2.getCount() > 1 && random.nextBoolean()) {
                list.add(lv2);
            } else {
                drops.add(lv2);
            }
            if (lv3.getCount() > 1 && random.nextBoolean()) {
                list.add(lv3);
                continue;
            }
            drops.add(lv3);
        }
        drops.addAll((Collection<ItemStack>)list);
        Util.shuffle(drops, random);
    }

    private List<Integer> getFreeSlots(Inventory inventory, Random random) {
        ObjectArrayList<Integer> objectArrayList = new ObjectArrayList<Integer>();
        for (int i = 0; i < inventory.size(); ++i) {
            if (!inventory.getStack(i).isEmpty()) continue;
            objectArrayList.add(i);
        }
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    implements LootFunctionConsumingBuilder<Builder> {
        private final List<LootPool> pools = Lists.newArrayList();
        private final List<LootFunction> functions = Lists.newArrayList();
        private LootContextType type = GENERIC;

        public Builder pool(LootPool.Builder poolBuilder) {
            this.pools.add(poolBuilder.build());
            return this;
        }

        public Builder type(LootContextType context) {
            this.type = context;
            return this;
        }

        @Override
        public Builder apply(LootFunction.Builder arg) {
            this.functions.add(arg.build());
            return this;
        }

        @Override
        public Builder getThisFunctionConsumingBuilder() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.type, this.pools.toArray(new LootPool[0]), this.functions.toArray(new LootFunction[0]));
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
            return this.getThisFunctionConsumingBuilder();
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
            return this.apply(function);
        }
    }

    public static class Serializer
    implements JsonDeserializer<LootTable>,
    JsonSerializer<LootTable> {
        @Override
        public LootTable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = JsonHelper.asObject(jsonElement, "loot table");
            LootPool[] lvs = JsonHelper.deserialize(jsonObject, "pools", new LootPool[0], jsonDeserializationContext, LootPool[].class);
            LootContextType lv = null;
            if (jsonObject.has("type")) {
                String string = JsonHelper.getString(jsonObject, "type");
                lv = LootContextTypes.get(new Identifier(string));
            }
            LootFunction[] lvs2 = JsonHelper.deserialize(jsonObject, "functions", new LootFunction[0], jsonDeserializationContext, LootFunction[].class);
            return new LootTable(lv != null ? lv : LootContextTypes.GENERIC, lvs, lvs2);
        }

        @Override
        public JsonElement serialize(LootTable arg, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (arg.type != GENERIC) {
                Identifier lv = LootContextTypes.getId(arg.type);
                if (lv != null) {
                    jsonObject.addProperty("type", lv.toString());
                } else {
                    LOGGER.warn("Failed to find id for param set {}", (Object)arg.type);
                }
            }
            if (arg.pools.length > 0) {
                jsonObject.add("pools", jsonSerializationContext.serialize(arg.pools));
            }
            if (!ArrayUtils.isEmpty(arg.functions)) {
                jsonObject.add("functions", jsonSerializationContext.serialize(arg.functions));
            }
            return jsonObject;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object supplier, Type unused, JsonSerializationContext context) {
            return this.serialize((LootTable)supplier, unused, context);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement json, Type unused, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(json, unused, context);
        }
    }
}

