/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndIslandFeature
extends Feature<DefaultFeatureConfig> {
    public EndIslandFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        Random lv2 = context.getRandom();
        BlockPos lv3 = context.getOrigin();
        float f = (float)lv2.nextInt(3) + 4.0f;
        int i = 0;
        while (f > 0.5f) {
            for (int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
                for (int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
                    if (!((float)(j * j + k * k) <= (f + 1.0f) * (f + 1.0f))) continue;
                    this.setBlockState(lv, lv3.add(j, i, k), Blocks.END_STONE.getDefaultState());
                }
            }
            f -= (float)lv2.nextInt(2) + 0.5f;
            --i;
        }
        return true;
    }
}

