/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public class DisconnectS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final Text reason;

    public DisconnectS2CPacket(Text reason) {
        this.reason = reason;
    }

    public DisconnectS2CPacket(PacketByteBuf buf) {
        this.reason = buf.readText();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeText(this.reason);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onDisconnect(this);
    }

    public Text getReason() {
        return this.reason;
    }
}

