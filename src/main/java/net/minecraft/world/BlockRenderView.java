/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;

public interface BlockRenderView
extends BlockView {
    public float getBrightness(Direction var1, boolean var2);

    public LightingProvider getLightingProvider();

    public int getColor(BlockPos var1, ColorResolver var2);

    default public int getLightLevel(LightType type, BlockPos pos) {
        return this.getLightingProvider().get(type).getLightLevel(pos);
    }

    default public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
        return this.getLightingProvider().getLight(pos, ambientDarkness);
    }

    default public boolean isSkyVisible(BlockPos pos) {
        return this.getLightLevel(LightType.SKY, pos) >= this.getMaxLightLevel();
    }
}

