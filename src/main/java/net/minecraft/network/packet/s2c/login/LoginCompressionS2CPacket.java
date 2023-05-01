/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.Packet;

public class LoginCompressionS2CPacket
implements Packet<ClientLoginPacketListener> {
    private final int compressionThreshold;

    public LoginCompressionS2CPacket(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }

    public LoginCompressionS2CPacket(PacketByteBuf buf) {
        this.compressionThreshold = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.compressionThreshold);
    }

    @Override
    public void apply(ClientLoginPacketListener arg) {
        arg.onCompression(this);
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}

