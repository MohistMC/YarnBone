/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.placementmodifier.AbstractCountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class CountPlacementModifier
extends AbstractCountPlacementModifier {
    public static final Codec<CountPlacementModifier> MODIFIER_CODEC = ((MapCodec)IntProvider.createValidatingCodec(0, 256).fieldOf("count")).xmap(CountPlacementModifier::new, arg -> arg.count).codec();
    private final IntProvider count;

    private CountPlacementModifier(IntProvider count) {
        this.count = count;
    }

    public static CountPlacementModifier of(IntProvider count) {
        return new CountPlacementModifier(count);
    }

    public static CountPlacementModifier of(int count) {
        return CountPlacementModifier.of(ConstantIntProvider.create(count));
    }

    @Override
    protected int getCount(Random random, BlockPos pos) {
        return this.count.get(random);
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierType.COUNT;
    }
}

