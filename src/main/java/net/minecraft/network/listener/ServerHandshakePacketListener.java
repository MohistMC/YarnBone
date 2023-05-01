/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;

public interface ServerHandshakePacketListener
extends ServerPacketListener {
    public void onHandshake(HandshakeC2SPacket var1);
}

