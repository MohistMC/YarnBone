/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.world;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EntityTrackingListener {
    public ServerPlayerEntity getPlayer();

    public void sendPacket(Packet<?> var1);
}

