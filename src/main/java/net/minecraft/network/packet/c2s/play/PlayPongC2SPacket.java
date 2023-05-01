/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class PlayPongC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final int parameter;

    public PlayPongC2SPacket(int parameter) {
        this.parameter = parameter;
    }

    public PlayPongC2SPacket(PacketByteBuf buf) {
        this.parameter = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.parameter);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onPong(this);
    }

    public int getParameter() {
        return this.parameter;
    }
}

