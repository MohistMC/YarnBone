/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetPotionLootFunction
extends ConditionalLootFunction {
    final Potion potion;

    SetPotionLootFunction(LootCondition[] conditions, Potion potion) {
        super(conditions);
        this.potion = potion;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_POTION;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        PotionUtil.setPotion(stack, this.potion);
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(Potion potion) {
        return SetPotionLootFunction.builder((LootCondition[] conditions) -> new SetPotionLootFunction((LootCondition[])conditions, potion));
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetPotionLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetPotionLootFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, arg, jsonSerializationContext);
            jsonObject.addProperty("id", Registries.POTION.getId(arg.potion).toString());
        }

        @Override
        public SetPotionLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            String string = JsonHelper.getString(jsonObject, "id");
            Potion lv = (Potion)Registries.POTION.getOrEmpty(Identifier.tryParse(string)).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + string + "'"));
            return new SetPotionLootFunction(args, lv);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

