/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;

public class BlockState
extends AbstractBlock.AbstractBlockState {
    public static final Codec<BlockState> CODEC = BlockState.createCodec(Registries.BLOCK.getCodec(), Block::getDefaultState).stable();

    public BlockState(Block arg, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
        super(arg, immutableMap, mapCodec);
    }

    @Override
    protected BlockState asBlockState() {
        return this;
    }
}

