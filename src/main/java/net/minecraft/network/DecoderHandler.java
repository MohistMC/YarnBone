/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class DecoderHandler
extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NetworkSide side;

    public DecoderHandler(NetworkSide side) {
        this.side = side;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objects) throws Exception {
        int i = buf.readableBytes();
        if (i == 0) {
            return;
        }
        PacketByteBuf lv = new PacketByteBuf(buf);
        int j = lv.readVarInt();
        Packet<?> lv2 = ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get().getPacketHandler(this.side, j, lv);
        if (lv2 == null) {
            throw new IOException("Bad packet id " + j);
        }
        int k = ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get().getId();
        FlightProfiler.INSTANCE.onPacketReceived(k, j, ctx.channel().remoteAddress(), i);
        if (lv.readableBytes() > 0) {
            throw new IOException("Packet " + ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get().getId() + "/" + j + " (" + lv2.getClass().getSimpleName() + ") was larger than I expected, found " + lv.readableBytes() + " bytes extra whilst reading packet " + j);
        }
        objects.add(lv2);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(ClientConnection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get(), j, lv2.getClass().getName());
        }
    }
}

