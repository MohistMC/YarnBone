/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot;

import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;

public interface LootChoice {
    public int getWeight(float var1);

    public void generateLoot(Consumer<ItemStack> var1, LootContext var2);
}

