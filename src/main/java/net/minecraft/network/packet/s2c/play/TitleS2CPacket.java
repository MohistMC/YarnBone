/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public class TitleS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final Text title;

    public TitleS2CPacket(Text title) {
        this.title = title;
    }

    public TitleS2CPacket(PacketByteBuf buf) {
        this.title = buf.readText();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeText(this.title);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onTitle(this);
    }

    public Text getTitle() {
        return this.title;
    }
}

