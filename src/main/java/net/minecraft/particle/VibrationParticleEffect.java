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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;

public class VibrationParticleEffect
implements ParticleEffect {
    public static final Codec<VibrationParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)PositionSource.CODEC.fieldOf("destination")).forGetter(effect -> effect.destination), ((MapCodec)Codec.INT.fieldOf("arrival_in_ticks")).forGetter(effect -> effect.arrivalInTicks)).apply((Applicative<VibrationParticleEffect, ?>)instance, VibrationParticleEffect::new));
    public static final ParticleEffect.Factory<VibrationParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<VibrationParticleEffect>(){

        @Override
        public VibrationParticleEffect read(ParticleType<VibrationParticleEffect> arg, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            float f = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float g = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float h = (float)stringReader.readDouble();
            stringReader.expect(' ');
            int i = stringReader.readInt();
            BlockPos lv = BlockPos.ofFloored(f, g, h);
            return new VibrationParticleEffect(new BlockPositionSource(lv), i);
        }

        @Override
        public VibrationParticleEffect read(ParticleType<VibrationParticleEffect> arg, PacketByteBuf arg2) {
            PositionSource lv = PositionSourceType.read(arg2);
            int i = arg2.readVarInt();
            return new VibrationParticleEffect(lv, i);
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
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationParticleEffect(PositionSource destination, int arrivalInTicks) {
        this.destination = destination;
        this.arrivalInTicks = arrivalInTicks;
    }

    @Override
    public void write(PacketByteBuf buf) {
        PositionSourceType.write(this.destination, buf);
        buf.writeVarInt(this.arrivalInTicks);
    }

    @Override
    public String asString() {
        Vec3d lv = this.destination.getPos(null).get();
        double d = lv.getX();
        double e = lv.getY();
        double f = lv.getZ();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", Registries.PARTICLE_TYPE.getId(this.getType()), d, e, f, this.arrivalInTicks);
    }

    public ParticleType<VibrationParticleEffect> getType() {
        return ParticleTypes.VIBRATION;
    }

    public PositionSource getVibration() {
        return this.destination;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }
}

