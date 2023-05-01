/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.c2s.play;

import java.time.Instant;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public record ChatMessageC2SPacket(String chatMessage, Instant timestamp, long salt, @Nullable MessageSignatureData signature, LastSeenMessageList.Acknowledgment acknowledgment) implements Packet<ServerPlayPacketListener>
{
    public ChatMessageC2SPacket(PacketByteBuf buf) {
        this(buf.readString(256), buf.readInstant(), buf.readLong(), (MessageSignatureData)buf.readNullable(MessageSignatureData::fromBuf), new LastSeenMessageList.Acknowledgment(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.chatMessage, 256);
        buf.writeInstant(this.timestamp);
        buf.writeLong(this.salt);
        buf.writeNullable(this.signature, MessageSignatureData::write);
        this.acknowledgment.write(buf);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onChatMessage(this);
    }
}

