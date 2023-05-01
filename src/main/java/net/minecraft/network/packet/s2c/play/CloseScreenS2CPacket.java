/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class CloseScreenS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int syncId;

    public CloseScreenS2CPacket(int syncId) {
        this.syncId = syncId;
    }

    public CloseScreenS2CPacket(PacketByteBuf buf) {
        this.syncId = buf.readUnsignedByte();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(this.syncId);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onCloseScreen(this);
    }

    public int getSyncId() {
        return this.syncId;
    }
}

