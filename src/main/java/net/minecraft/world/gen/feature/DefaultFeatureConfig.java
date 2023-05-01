/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DefaultFeatureConfig
implements FeatureConfig {
    public static final Codec<DefaultFeatureConfig> CODEC = Codec.unit(() -> INSTANCE);
    public static final DefaultFeatureConfig INSTANCE = new DefaultFeatureConfig();
}

