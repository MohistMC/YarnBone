/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import net.minecraft.network.PacketByteBuf;

public class SplitterHandler
extends ByteToMessageDecoder {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objects) {
        buf.markReaderIndex();
        byte[] bs = new byte[3];
        for (int i = 0; i < bs.length; ++i) {
            if (!buf.isReadable()) {
                buf.resetReaderIndex();
                return;
            }
            bs[i] = buf.readByte();
            if (bs[i] < 0) continue;
            PacketByteBuf lv = new PacketByteBuf(Unpooled.wrappedBuffer(bs));
            try {
                int j = lv.readVarInt();
                if (buf.readableBytes() < j) {
                    buf.resetReaderIndex();
                    return;
                }
                objects.add(buf.readBytes(j));
                return;
            }
            finally {
                lv.release();
            }
        }
        throw new CorruptedFrameException("length wider than 21-bit");
    }
}

