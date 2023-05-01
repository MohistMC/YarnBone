/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.particle;

import com.mojang.serialization.Codec;
import net.minecraft.particle.ParticleEffect;

public abstract class ParticleType<T extends ParticleEffect> {
    private final boolean alwaysShow;
    private final ParticleEffect.Factory<T> parametersFactory;

    protected ParticleType(boolean alwaysShow, ParticleEffect.Factory<T> parametersFactory) {
        this.alwaysShow = alwaysShow;
        this.parametersFactory = parametersFactory;
    }

    public boolean shouldAlwaysSpawn() {
        return this.alwaysShow;
    }

    public ParticleEffect.Factory<T> getParametersFactory() {
        return this.parametersFactory;
    }

    public abstract Codec<T> getCodec();
}

