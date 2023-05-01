/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketBundleHandler;
import net.minecraft.network.packet.Packet;

public class PacketUnbundler
extends MessageToMessageEncoder<Packet<?>> {
    private final NetworkSide side;

    public PacketUnbundler(NetworkSide side) {
        this.side = side;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> arg, List<Object> list) throws Exception {
        PacketBundleHandler.BundlerGetter lv = channelHandlerContext.channel().attr(PacketBundleHandler.KEY).get();
        if (lv == null) {
            throw new EncoderException("Bundler not configured: " + arg);
        }
        lv.getBundler(this.side).forEachPacket(arg, list::add);
    }

    @Override
    protected /* synthetic */ void encode(ChannelHandlerContext context, Object packet, List packets) throws Exception {
        this.encode(context, (Packet)packet, (List<Object>)packets);
    }
}

