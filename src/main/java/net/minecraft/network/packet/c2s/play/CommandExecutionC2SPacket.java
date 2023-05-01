/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import java.time.Instant;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.Packet;

public record CommandExecutionC2SPacket(String command, Instant timestamp, long salt, ArgumentSignatureDataMap argumentSignatures, LastSeenMessageList.Acknowledgment acknowledgment) implements Packet<ServerPlayPacketListener>
{
    public CommandExecutionC2SPacket(PacketByteBuf buf) {
        this(buf.readString(256), buf.readInstant(), buf.readLong(), new ArgumentSignatureDataMap(buf), new LastSeenMessageList.Acknowledgment(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.command, 256);
        buf.writeInstant(this.timestamp);
        buf.writeLong(this.salt);
        this.argumentSignatures.write(buf);
        this.acknowledgment.write(buf);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onCommandExecution(this);
    }
}

