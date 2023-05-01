/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

public record FeaturesS2CPacket(Set<Identifier> features) implements Packet<ClientPlayPacketListener>
{
    public FeaturesS2CPacket(PacketByteBuf buf) {
        this(buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeCollection(this.features, PacketByteBuf::writeIdentifier);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onFeatures(this);
    }
}

