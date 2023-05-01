/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record PlayerSessionC2SPacket(PublicPlayerSession.Serialized chatSession) implements Packet<ServerPlayPacketListener>
{
    public PlayerSessionC2SPacket(PacketByteBuf buf) {
        this(PublicPlayerSession.Serialized.fromBuf(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        PublicPlayerSession.Serialized.write(buf, this.chatSession);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onPlayerSession(this);
    }
}

