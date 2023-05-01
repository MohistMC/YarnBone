/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.GeodeCrackConfig;
import net.minecraft.world.gen.feature.GeodeLayerConfig;
import net.minecraft.world.gen.feature.GeodeLayerThicknessConfig;

public class GeodeFeatureConfig
implements FeatureConfig {
    public static final Codec<Double> RANGE = Codec.doubleRange(0.0, 1.0);
    public static final Codec<GeodeFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)GeodeLayerConfig.CODEC.fieldOf("blocks")).forGetter(config -> config.layerConfig), ((MapCodec)GeodeLayerThicknessConfig.CODEC.fieldOf("layers")).forGetter(config -> config.layerThicknessConfig), ((MapCodec)GeodeCrackConfig.CODEC.fieldOf("crack")).forGetter(config -> config.crackConfig), ((MapCodec)RANGE.fieldOf("use_potential_placements_chance")).orElse(0.35).forGetter(config -> config.usePotentialPlacementsChance), ((MapCodec)RANGE.fieldOf("use_alternate_layer0_chance")).orElse(0.0).forGetter(config -> config.useAlternateLayer0Chance), ((MapCodec)Codec.BOOL.fieldOf("placements_require_layer0_alternate")).orElse(true).forGetter(config -> config.placementsRequireLayer0Alternate), ((MapCodec)IntProvider.createValidatingCodec(1, 20).fieldOf("outer_wall_distance")).orElse(UniformIntProvider.create(4, 5)).forGetter(config -> config.outerWallDistance), ((MapCodec)IntProvider.createValidatingCodec(1, 20).fieldOf("distribution_points")).orElse(UniformIntProvider.create(3, 4)).forGetter(config -> config.distributionPoints), ((MapCodec)IntProvider.createValidatingCodec(0, 10).fieldOf("point_offset")).orElse(UniformIntProvider.create(1, 2)).forGetter(config -> config.pointOffset), ((MapCodec)Codec.INT.fieldOf("min_gen_offset")).orElse(-16).forGetter(config -> config.minGenOffset), ((MapCodec)Codec.INT.fieldOf("max_gen_offset")).orElse(16).forGetter(config -> config.maxGenOffset), ((MapCodec)RANGE.fieldOf("noise_multiplier")).orElse(0.05).forGetter(config -> config.noiseMultiplier), ((MapCodec)Codec.INT.fieldOf("invalid_blocks_threshold")).forGetter(config -> config.invalidBlocksThreshold)).apply((Applicative<GeodeFeatureConfig, ?>)instance, GeodeFeatureConfig::new));
    public final GeodeLayerConfig layerConfig;
    public final GeodeLayerThicknessConfig layerThicknessConfig;
    public final GeodeCrackConfig crackConfig;
    public final double usePotentialPlacementsChance;
    public final double useAlternateLayer0Chance;
    public final boolean placementsRequireLayer0Alternate;
    public final IntProvider outerWallDistance;
    public final IntProvider distributionPoints;
    public final IntProvider pointOffset;
    public final int minGenOffset;
    public final int maxGenOffset;
    public final double noiseMultiplier;
    public final int invalidBlocksThreshold;

    public GeodeFeatureConfig(GeodeLayerConfig layerConfig, GeodeLayerThicknessConfig layerThicknessConfig, GeodeCrackConfig crackConfig, double usePotentialPlacementsChance, double useAlternateLayer0Chance, boolean placementsRequireLayer0Alternate, IntProvider outerWallDistance, IntProvider distributionPoints, IntProvider pointOffset, int maxDistributionPoints, int minPointOffset, double noiseMultiplier, int maxGenOffset) {
        this.layerConfig = layerConfig;
        this.layerThicknessConfig = layerThicknessConfig;
        this.crackConfig = crackConfig;
        this.usePotentialPlacementsChance = usePotentialPlacementsChance;
        this.useAlternateLayer0Chance = useAlternateLayer0Chance;
        this.placementsRequireLayer0Alternate = placementsRequireLayer0Alternate;
        this.outerWallDistance = outerWallDistance;
        this.distributionPoints = distributionPoints;
        this.pointOffset = pointOffset;
        this.minGenOffset = maxDistributionPoints;
        this.maxGenOffset = minPointOffset;
        this.noiseMultiplier = noiseMultiplier;
        this.invalidBlocksThreshold = maxGenOffset;
    }
}

