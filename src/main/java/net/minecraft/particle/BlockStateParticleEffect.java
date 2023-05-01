/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

public class BlockStateParticleEffect
implements ParticleEffect {
    public static final ParticleEffect.Factory<BlockStateParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<BlockStateParticleEffect>(){

        @Override
        public BlockStateParticleEffect read(ParticleType<BlockStateParticleEffect> arg, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            return new BlockStateParticleEffect(arg, BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), stringReader, false).blockState());
        }

        @Override
        public BlockStateParticleEffect read(ParticleType<BlockStateParticleEffect> arg, PacketByteBuf arg2) {
            return new BlockStateParticleEffect(arg, arg2.readRegistryValue(Block.STATE_IDS));
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
    private final ParticleType<BlockStateParticleEffect> type;
    private final BlockState blockState;

    public static Codec<BlockStateParticleEffect> createCodec(ParticleType<BlockStateParticleEffect> type) {
        return BlockState.CODEC.xmap(state -> new BlockStateParticleEffect(type, (BlockState)state), effect -> effect.blockState);
    }

    public BlockStateParticleEffect(ParticleType<BlockStateParticleEffect> type, BlockState blockState) {
        this.type = type;
        this.blockState = blockState;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeRegistryValue(Block.STATE_IDS, this.blockState);
    }

    @Override
    public String asString() {
        return Registries.PARTICLE_TYPE.getId(this.getType()) + " " + BlockArgumentParser.stringifyBlockState(this.blockState);
    }

    public ParticleType<BlockStateParticleEffect> getType() {
        return this.type;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }
}

