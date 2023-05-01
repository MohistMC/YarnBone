/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.encryption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;
import net.minecraft.network.encryption.PacketEncryptionManager;

public class PacketDecryptor
extends MessageToMessageDecoder<ByteBuf> {
    private final PacketEncryptionManager manager;

    public PacketDecryptor(Cipher cipher) {
        this.manager = new PacketEncryptionManager(cipher);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        list.add(this.manager.decrypt(channelHandlerContext, byteBuf));
    }

    @Override
    protected /* synthetic */ void decode(ChannelHandlerContext context, Object buf, List result) throws Exception {
        this.decode(context, (ByteBuf)buf, (List<Object>)result);
    }
}

