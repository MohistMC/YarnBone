/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.loot.provider.nbt;

import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public interface LootNbtProvider {
    @Nullable
    public NbtElement getNbt(LootContext var1);

    public Set<LootContextParameter<?>> getRequiredParameters();

    public LootNbtProviderType getType();
}

