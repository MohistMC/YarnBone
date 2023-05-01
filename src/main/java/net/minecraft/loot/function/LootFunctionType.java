/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.function;

import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.JsonSerializer;

public class LootFunctionType
extends JsonSerializableType<LootFunction> {
    public LootFunctionType(JsonSerializer<? extends LootFunction> arg) {
        super(arg);
    }
}

