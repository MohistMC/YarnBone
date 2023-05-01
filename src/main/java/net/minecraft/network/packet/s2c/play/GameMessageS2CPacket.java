/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public record GameMessageS2CPacket(Text content, boolean overlay) implements Packet<ClientPlayPacketListener>
{
    public GameMessageS2CPacket(PacketByteBuf buf) {
        this(buf.readText(), buf.readBoolean());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeText(this.content);
        buf.writeBoolean(this.overlay);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onGameMessage(this);
    }

    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}

