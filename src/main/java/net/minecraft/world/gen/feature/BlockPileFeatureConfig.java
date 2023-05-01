/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class BlockPileFeatureConfig
implements FeatureConfig {
    public static final Codec<BlockPileFeatureConfig> CODEC = ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("state_provider")).xmap(BlockPileFeatureConfig::new, config -> config.stateProvider).codec();
    public final BlockStateProvider stateProvider;

    public BlockPileFeatureConfig(BlockStateProvider stateProvider) {
        this.stateProvider = stateProvider;
    }
}

