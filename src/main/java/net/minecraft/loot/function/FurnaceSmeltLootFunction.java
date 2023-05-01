/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import org.slf4j.Logger;

public class FurnaceSmeltLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogUtils.getLogger();

    FurnaceSmeltLootFunction(LootCondition[] args) {
        super(args);
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.FURNACE_SMELT;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        ItemStack lv;
        if (stack.isEmpty()) {
            return stack;
        }
        Optional<SmeltingRecipe> optional = context.getWorld().getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SimpleInventory(stack), context.getWorld());
        if (optional.isPresent() && !(lv = optional.get().getOutput(context.getWorld().getRegistryManager())).isEmpty()) {
            ItemStack lv2 = lv.copy();
            lv2.setCount(stack.getCount());
            return lv2;
        }
        LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", (Object)stack);
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder() {
        return FurnaceSmeltLootFunction.builder(FurnaceSmeltLootFunction::new);
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<FurnaceSmeltLootFunction> {
        @Override
        public FurnaceSmeltLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            return new FurnaceSmeltLootFunction(args);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

