/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.provider.nbt;

import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.JsonSerializer;

public class LootNbtProviderType
extends JsonSerializableType<LootNbtProvider> {
    public LootNbtProviderType(JsonSerializer<? extends LootNbtProvider> arg) {
        super(arg);
    }
}

