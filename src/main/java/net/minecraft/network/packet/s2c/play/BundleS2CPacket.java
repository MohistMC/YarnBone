/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.Packet;

public class BundleS2CPacket
extends BundlePacket<ClientPlayPacketListener> {
    public BundleS2CPacket(Iterable<Packet<ClientPlayPacketListener>> iterable) {
        super(iterable);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onBundle(this);
    }
}

