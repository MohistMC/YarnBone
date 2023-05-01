/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.heightprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import org.slf4j.Logger;

public class BiasedToBottomHeightProvider
extends HeightProvider {
    public static final Codec<BiasedToBottomHeightProvider> BIASED_TO_BOTTOM_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)YOffset.OFFSET_CODEC.fieldOf("min_inclusive")).forGetter(provider -> provider.minOffset), ((MapCodec)YOffset.OFFSET_CODEC.fieldOf("max_inclusive")).forGetter(provider -> provider.maxOffset), Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter(provider -> provider.inner)).apply((Applicative<BiasedToBottomHeightProvider, ?>)instance, BiasedToBottomHeightProvider::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final YOffset minOffset;
    private final YOffset maxOffset;
    private final int inner;

    private BiasedToBottomHeightProvider(YOffset minOffset, YOffset maxOffset, int inner) {
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.inner = inner;
    }

    public static BiasedToBottomHeightProvider create(YOffset minOffset, YOffset maxOffset, int inner) {
        return new BiasedToBottomHeightProvider(minOffset, maxOffset, inner);
    }

    @Override
    public int get(Random random, HeightContext context) {
        int i = this.minOffset.getY(context);
        int j = this.maxOffset.getY(context);
        if (j - i - this.inner + 1 <= 0) {
            LOGGER.warn("Empty height range: {}", (Object)this);
            return i;
        }
        int k = random.nextInt(j - i - this.inner + 1);
        return random.nextInt(k + this.inner) + i;
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.BIASED_TO_BOTTOM;
    }

    public String toString() {
        return "biased[" + this.minOffset + "-" + this.maxOffset + " inner: " + this.inner + "]";
    }
}
