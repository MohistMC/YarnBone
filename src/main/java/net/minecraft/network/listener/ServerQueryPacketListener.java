/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;

public interface ServerQueryPacketListener
extends ServerPacketListener {
    public void onPing(QueryPingC2SPacket var1);

    public void onRequest(QueryRequestC2SPacket var1);
}

