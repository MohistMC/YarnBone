/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.Packet;

public record RemoveMessageS2CPacket(MessageSignatureData.Indexed messageSignature) implements Packet<ClientPlayPacketListener>
{
    public RemoveMessageS2CPacket(PacketByteBuf buf) {
        this(MessageSignatureData.Indexed.fromBuf(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        MessageSignatureData.Indexed.write(buf, this.messageSignature);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onRemoveMessage(this);
    }
}

