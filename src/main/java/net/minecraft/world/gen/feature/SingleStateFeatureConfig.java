/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;

public class SingleStateFeatureConfig
implements FeatureConfig {
    public static final Codec<SingleStateFeatureConfig> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("state")).xmap(SingleStateFeatureConfig::new, config -> config.state).codec();
    public final BlockState state;

    public SingleStateFeatureConfig(BlockState state) {
        this.state = state;
    }
}

