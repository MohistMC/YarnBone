/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record ChatMessageS2CPacket(UUID sender, int index, @Nullable MessageSignatureData signature, MessageBody.Serialized body, @Nullable Text unsignedContent, FilterMask filterMask, MessageType.Serialized serializedParameters) implements Packet<ClientPlayPacketListener>
{
    public ChatMessageS2CPacket(PacketByteBuf buf) {
        this(buf.readUuid(), buf.readVarInt(), (MessageSignatureData)buf.readNullable(MessageSignatureData::fromBuf), new MessageBody.Serialized(buf), (Text)buf.readNullable(PacketByteBuf::readText), FilterMask.readMask(buf), new MessageType.Serialized(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.sender);
        buf.writeVarInt(this.index);
        buf.writeNullable(this.signature, MessageSignatureData::write);
        this.body.write(buf);
        buf.writeNullable(this.unsignedContent, PacketByteBuf::writeText);
        FilterMask.writeMask(buf, this.filterMask);
        this.serializedParameters.write(buf);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onChatMessage(this);
    }

    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}

