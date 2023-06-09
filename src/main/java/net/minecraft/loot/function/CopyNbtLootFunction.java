/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.JsonHelper;

public class CopyNbtLootFunction
extends ConditionalLootFunction {
    final LootNbtProvider source;
    final List<Operation> operations;

    CopyNbtLootFunction(LootCondition[] conditions, LootNbtProvider source, List<Operation> operations) {
        super(conditions);
        this.source = source;
        this.operations = ImmutableList.copyOf(operations);
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.COPY_NBT;
    }

    static NbtPathArgumentType.NbtPath parseNbtPath(String nbtPath) {
        try {
            return new NbtPathArgumentType().parse(new StringReader(nbtPath));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            throw new IllegalArgumentException("Failed to parse path " + nbtPath, commandSyntaxException);
        }
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.source.getRequiredParameters();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        NbtElement lv = this.source.getNbt(context);
        if (lv != null) {
            this.operations.forEach(operation -> operation.execute(stack::getOrCreateNbt, lv));
        }
        return stack;
    }

    public static Builder builder(LootNbtProvider source) {
        return new Builder(source);
    }

    public static Builder builder(LootContext.EntityTarget target) {
        return new Builder(ContextLootNbtProvider.fromTarget(target));
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final LootNbtProvider source;
        private final List<Operation> operations = Lists.newArrayList();

        Builder(LootNbtProvider source) {
            this.source = source;
        }

        public Builder withOperation(String source, String target, Operator operator) {
            this.operations.add(new Operation(source, target, operator));
            return this;
        }

        public Builder withOperation(String source, String target) {
            return this.withOperation(source, target, Operator.REPLACE);
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new CopyNbtLootFunction(this.getConditions(), this.source, this.operations);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    static class Operation {
        private final String sourcePath;
        private final NbtPathArgumentType.NbtPath parsedSourcePath;
        private final String targetPath;
        private final NbtPathArgumentType.NbtPath parsedTargetPath;
        private final Operator operator;

        Operation(String sourcePath, String targetPath, Operator operator) {
            this.sourcePath = sourcePath;
            this.parsedSourcePath = CopyNbtLootFunction.parseNbtPath(sourcePath);
            this.targetPath = targetPath;
            this.parsedTargetPath = CopyNbtLootFunction.parseNbtPath(targetPath);
            this.operator = operator;
        }

        public void execute(Supplier<NbtElement> itemNbtGetter, NbtElement sourceEntityNbt) {
            try {
                List<NbtElement> list = this.parsedSourcePath.get(sourceEntityNbt);
                if (!list.isEmpty()) {
                    this.operator.merge(itemNbtGetter.get(), this.parsedTargetPath, list);
                }
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }

        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("source", this.sourcePath);
            jsonObject.addProperty("target", this.targetPath);
            jsonObject.addProperty("op", this.operator.name);
            return jsonObject;
        }

        public static Operation fromJson(JsonObject json) {
            String string = JsonHelper.getString(json, "source");
            String string2 = JsonHelper.getString(json, "target");
            Operator lv = Operator.get(JsonHelper.getString(json, "op"));
            return new Operation(string, string2, lv);
        }
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<CopyNbtLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, CopyNbtLootFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, arg, jsonSerializationContext);
            jsonObject.add("source", jsonSerializationContext.serialize(arg.source));
            JsonArray jsonArray = new JsonArray();
            arg.operations.stream().map(Operation::toJson).forEach(jsonArray::add);
            jsonObject.add("ops", jsonArray);
        }

        @Override
        public CopyNbtLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            LootNbtProvider lv = JsonHelper.deserialize(jsonObject, "source", jsonDeserializationContext, LootNbtProvider.class);
            ArrayList<Operation> list = Lists.newArrayList();
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "ops");
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject2 = JsonHelper.asObject(jsonElement, "op");
                list.add(Operation.fromJson(jsonObject2));
            }
            return new CopyNbtLootFunction(args, lv, list);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static enum Operator {
        REPLACE("replace"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                targetPath.put(itemNbt, Iterables.getLast(sourceNbts));
            }
        }
        ,
        APPEND("append"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                List<NbtElement> list2 = targetPath.getOrInit(itemNbt, NbtList::new);
                list2.forEach(foundNbt -> {
                    if (foundNbt instanceof NbtList) {
                        sourceNbts.forEach(sourceNbt -> ((NbtList)foundNbt).add(sourceNbt.copy()));
                    }
                });
            }
        }
        ,
        MERGE("merge"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                List<NbtElement> list2 = targetPath.getOrInit(itemNbt, NbtCompound::new);
                list2.forEach(foundNbt -> {
                    if (foundNbt instanceof NbtCompound) {
                        sourceNbts.forEach(sourceNbt -> {
                            if (sourceNbt instanceof NbtCompound) {
                                ((NbtCompound)foundNbt).copyFrom((NbtCompound)sourceNbt);
                            }
                        });
                    }
                });
            }
        };

        final String name;

        public abstract void merge(NbtElement var1, NbtPathArgumentType.NbtPath var2, List<NbtElement> var3) throws CommandSyntaxException;

        Operator(String name) {
            this.name = name;
        }

        public static Operator get(String name) {
            for (Operator lv : Operator.values()) {
                if (!lv.name.equals(name)) continue;
                return lv;
            }
            throw new IllegalArgumentException("Invalid merge strategy" + name);
        }
    }
}

