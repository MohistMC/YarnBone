/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

public abstract class BundlePacket<T extends PacketListener>
implements Packet<T> {
    private final Iterable<Packet<T>> packets;

    protected BundlePacket(Iterable<Packet<T>> packets) {
        this.packets = packets;
    }

    public final Iterable<Packet<T>> getPackets() {
        return this.packets;
    }

    @Override
    public final void write(PacketByteBuf buf) {
    }
}

