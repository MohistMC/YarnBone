/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.query;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.Packet;

public class QueryPongS2CPacket
implements Packet<ClientQueryPacketListener> {
    private final long startTime;

    public QueryPongS2CPacket(long startTime) {
        this.startTime = startTime;
    }

    public QueryPongS2CPacket(PacketByteBuf buf) {
        this.startTime = buf.readLong();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(this.startTime);
    }

    @Override
    public void apply(ClientQueryPacketListener arg) {
        arg.onPong(this);
    }

    public long getStartTime() {
        return this.startTime;
    }
}

