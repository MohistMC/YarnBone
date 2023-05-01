/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.query;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.Packet;

public class QueryRequestC2SPacket
implements Packet<ServerQueryPacketListener> {
    public QueryRequestC2SPacket() {
    }

    public QueryRequestC2SPacket(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public void apply(ServerQueryPacketListener arg) {
        arg.onRequest(this);
    }
}

