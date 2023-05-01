/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record MessageAcknowledgmentC2SPacket(int offset) implements Packet<ServerPlayPacketListener>
{
    public MessageAcknowledgmentC2SPacket(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.offset);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onMessageAcknowledgment(this);
    }
}

