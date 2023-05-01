/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Hand;

public class HandSwingC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final Hand hand;

    public HandSwingC2SPacket(Hand hand) {
        this.hand = hand;
    }

    public HandSwingC2SPacket(PacketByteBuf buf) {
        this.hand = buf.readEnumConstant(Hand.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.hand);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onHandSwing(this);
    }

    public Hand getHand() {
        return this.hand;
    }
}

