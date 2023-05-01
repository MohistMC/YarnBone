/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.Inflater;
import net.minecraft.network.PacketByteBuf;

public class PacketInflater
extends ByteToMessageDecoder {
    public static final int field_34057 = 0x200000;
    public static final int MAXIMUM_PACKET_SIZE = 0x800000;
    private final Inflater inflater;
    private int compressionThreshold;
    private boolean rejectsBadPackets;

    public PacketInflater(int compressionThreshold, boolean rejectsBadPackets) {
        this.compressionThreshold = compressionThreshold;
        this.rejectsBadPackets = rejectsBadPackets;
        this.inflater = new Inflater();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objects) throws Exception {
        if (buf.readableBytes() == 0) {
            return;
        }
        PacketByteBuf lv = new PacketByteBuf(buf);
        int i = lv.readVarInt();
        if (i == 0) {
            objects.add(lv.readBytes(lv.readableBytes()));
            return;
        }
        if (this.rejectsBadPackets) {
            if (i < this.compressionThreshold) {
                throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.compressionThreshold);
            }
            if (i > 0x800000) {
                throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 8388608");
            }
        }
        byte[] bs = new byte[lv.readableBytes()];
        lv.readBytes(bs);
        this.inflater.setInput(bs);
        byte[] cs = new byte[i];
        this.inflater.inflate(cs);
        objects.add(Unpooled.wrappedBuffer(cs));
        this.inflater.reset();
    }

    public void setCompressionThreshold(int compressionThreshold, boolean rejectsBadPackets) {
        this.compressionThreshold = compressionThreshold;
        this.rejectsBadPackets = rejectsBadPackets;
    }
}

