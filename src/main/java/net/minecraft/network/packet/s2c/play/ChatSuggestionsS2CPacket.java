/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record ChatSuggestionsS2CPacket(Action action, List<String> entries) implements Packet<ClientPlayPacketListener>
{
    public ChatSuggestionsS2CPacket(PacketByteBuf buf) {
        this(buf.readEnumConstant(Action.class), buf.readList(PacketByteBuf::readString));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.action);
        buf.writeCollection(this.entries, PacketByteBuf::writeString);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onChatSuggestions(this);
    }

    public static enum Action {
        ADD,
        REMOVE,
        SET;

    }
}

