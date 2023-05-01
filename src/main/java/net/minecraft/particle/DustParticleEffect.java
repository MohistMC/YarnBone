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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class DustParticleEffect
extends AbstractDustParticleEffect {
    public static final Vector3f RED = Vec3d.unpackRgb(0xFF0000).toVector3f();
    public static final DustParticleEffect DEFAULT = new DustParticleEffect(RED, 1.0f);
    public static final Codec<DustParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.VECTOR_3F.fieldOf("color")).forGetter(effect -> effect.color), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(effect -> Float.valueOf(effect.scale))).apply((Applicative<DustParticleEffect, ?>)instance, DustParticleEffect::new));
    public static final ParticleEffect.Factory<DustParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<DustParticleEffect>(){

        @Override
        public DustParticleEffect read(ParticleType<DustParticleEffect> arg, StringReader stringReader) throws CommandSyntaxException {
            Vector3f vector3f = AbstractDustParticleEffect.readColor(stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            return new DustParticleEffect(vector3f, f);
        }

        @Override
        public DustParticleEffect read(ParticleType<DustParticleEffect> arg, PacketByteBuf arg2) {
            return new DustParticleEffect(AbstractDustParticleEffect.readColor(arg2), arg2.readFloat());
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

    public DustParticleEffect(Vector3f vector3f, float f) {
        super(vector3f, f);
    }

    public ParticleType<DustParticleEffect> getType() {
        return ParticleTypes.DUST;
    }
}

