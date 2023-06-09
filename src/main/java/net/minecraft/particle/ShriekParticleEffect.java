/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;

public class ShriekParticleEffect
implements ParticleEffect {
    public static final Codec<ShriekParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("delay")).forGetter(particleEffect -> particleEffect.delay)).apply((Applicative<ShriekParticleEffect, ?>)instance, ShriekParticleEffect::new));
    public static final ParticleEffect.Factory<ShriekParticleEffect> FACTORY = new ParticleEffect.Factory<ShriekParticleEffect>(){

        @Override
        public ShriekParticleEffect read(ParticleType<ShriekParticleEffect> arg, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            int i = stringReader.readInt();
            return new ShriekParticleEffect(i);
        }

        @Override
        public ShriekParticleEffect read(ParticleType<ShriekParticleEffect> arg, PacketByteBuf arg2) {
            return new ShriekParticleEffect(arg2.readVarInt());
        }

        @Override
        public /* synthetic */ ParticleEffect read(ParticleType type, PacketByteBuf buf) {
            return this.read(type, buf);
        }

        @Override
        public /* synthetic */ ParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException {
            return this.read(type, reader);
        }
    };
    private final int delay;

    public ShriekParticleEffect(int delay) {
        this.delay = delay;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.delay);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %d", Registries.PARTICLE_TYPE.getId(this.getType()), this.delay);
    }

    public ParticleType<ShriekParticleEffect> getType() {
        return ParticleTypes.SHRIEK;
    }

    public int getDelay() {
        return this.delay;
    }
}

