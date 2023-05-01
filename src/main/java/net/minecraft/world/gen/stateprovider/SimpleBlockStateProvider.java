/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;

public class SimpleBlockStateProvider
extends BlockStateProvider {
    public static final Codec<SimpleBlockStateProvider> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("state")).xmap(SimpleBlockStateProvider::new, arg -> arg.state).codec();
    private final BlockState state;

    protected SimpleBlockStateProvider(BlockState state) {
        this.state = state;
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        return this.state;
    }
}

