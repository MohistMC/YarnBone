/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.PacketByteBuf;

@ChannelHandler.Sharable
public class SizePrepender
extends MessageToByteEncoder<ByteBuf> {
    private static final int MAX_PREPEND_LENGTH = 3;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
        int i = byteBuf.readableBytes();
        int j = PacketByteBuf.getVarIntLength(i);
        if (j > 3) {
            throw new IllegalArgumentException("unable to fit " + i + " into 3");
        }
        PacketByteBuf lv = new PacketByteBuf(byteBuf2);
        lv.ensureWritable(j + i);
        lv.writeVarInt(i);
        lv.writeBytes(byteBuf, byteBuf.readerIndex(), i);
    }

    @Override
    protected /* synthetic */ void encode(ChannelHandlerContext ctx, Object input, ByteBuf output) throws Exception {
        this.encode(ctx, (ByteBuf)input, output);
    }
}

