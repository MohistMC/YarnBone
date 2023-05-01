/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.data.client;

import com.google.gson.JsonElement;
import java.util.function.Supplier;
import net.minecraft.block.Block;

public interface BlockStateSupplier
extends Supplier<JsonElement> {
    public Block getBlock();
}

