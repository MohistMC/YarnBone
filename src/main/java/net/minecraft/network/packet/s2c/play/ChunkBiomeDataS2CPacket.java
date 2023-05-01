/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public record ChunkBiomeDataS2CPacket(List<Serialized> chunkBiomeData) implements Packet<ClientPlayPacketListener>
{
    private static final int MAX_SIZE = 0x200000;

    public ChunkBiomeDataS2CPacket(PacketByteBuf buf) {
        this(buf.readList(Serialized::new));
    }

    public static ChunkBiomeDataS2CPacket create(List<WorldChunk> chunks) {
        return new ChunkBiomeDataS2CPacket(chunks.stream().map(Serialized::new).toList());
    }

    @Override
    public void write(PacketByteBuf buf2) {
        buf2.writeCollection(this.chunkBiomeData, (buf, data) -> data.write((PacketByteBuf)buf));
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onChunkBiomeData(this);
    }

    public record Serialized(ChunkPos pos, byte[] buffer) {
        public Serialized(WorldChunk chunk) {
            this(chunk.getPos(), new byte[Serialized.getTotalPacketSize(chunk)]);
            Serialized.write(new PacketByteBuf(this.toWritingBuf()), chunk);
        }

        public Serialized(PacketByteBuf buf) {
            this(buf.readChunkPos(), buf.readByteArray(0x200000));
        }

        private static int getTotalPacketSize(WorldChunk chunk) {
            int i = 0;
            for (ChunkSection lv : chunk.getSectionArray()) {
                i += lv.getBiomeContainer().getPacketSize();
            }
            return i;
        }

        public PacketByteBuf toReadingBuf() {
            return new PacketByteBuf(Unpooled.wrappedBuffer(this.buffer));
        }

        private ByteBuf toWritingBuf() {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(this.buffer);
            byteBuf.writerIndex(0);
            return byteBuf;
        }

        public static void write(PacketByteBuf buf, WorldChunk chunk) {
            for (ChunkSection lv : chunk.getSectionArray()) {
                lv.getBiomeContainer().writePacket(buf);
            }
        }

        public void write(PacketByteBuf buf) {
            buf.writeChunkPos(this.pos);
            buf.writeByteArray(this.buffer);
        }
    }
}

