/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.query;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.Packet;

public class QueryPingC2SPacket
implements Packet<ServerQueryPacketListener> {
    private final long startTime;

    public QueryPingC2SPacket(long startTime) {
        this.startTime = startTime;
    }

    public QueryPingC2SPacket(PacketByteBuf buf) {
        this.startTime = buf.readLong();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(this.startTime);
    }

    @Override
    public void apply(ServerQueryPacketListener arg) {
        arg.onPing(this);
    }

    public long getStartTime() {
        return this.startTime;
    }
}

