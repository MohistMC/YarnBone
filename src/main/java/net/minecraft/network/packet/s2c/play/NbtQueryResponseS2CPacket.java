/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public class NbtQueryResponseS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int transactionId;
    @Nullable
    private final NbtCompound nbt;

    public NbtQueryResponseS2CPacket(int transactionId, @Nullable NbtCompound nbt) {
        this.transactionId = transactionId;
        this.nbt = nbt;
    }

    public NbtQueryResponseS2CPacket(PacketByteBuf buf) {
        this.transactionId = buf.readVarInt();
        this.nbt = buf.readNbt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.transactionId);
        buf.writeNbt(this.nbt);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onNbtQueryResponse(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    public NbtCompound getNbt() {
        return this.nbt;
    }

    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}

