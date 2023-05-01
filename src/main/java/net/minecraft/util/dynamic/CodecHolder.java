/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.dynamic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record CodecHolder<A>(Codec<A> codec) {
    public static <A> CodecHolder<A> of(Codec<A> codec) {
        return new CodecHolder<A>(codec);
    }

    public static <A> CodecHolder<A> of(MapCodec<A> mapCodec) {
        return new CodecHolder<A>(mapCodec.codec());
    }
}

