/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Hand;

public class OpenWrittenBookS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final Hand hand;

    public OpenWrittenBookS2CPacket(Hand hand) {
        this.hand = hand;
    }

    public OpenWrittenBookS2CPacket(PacketByteBuf buf) {
        this.hand = buf.readEnumConstant(Hand.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.hand);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onOpenWrittenBook(this);
    }

    public Hand getHand() {
        return this.hand;
    }
}

