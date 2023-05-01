/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class ResourcePackStatusC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final Status status;

    public ResourcePackStatusC2SPacket(Status status) {
        this.status = status;
    }

    public ResourcePackStatusC2SPacket(PacketByteBuf buf) {
        this.status = buf.readEnumConstant(Status.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.status);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onResourcePackStatus(this);
    }

    public Status getStatus() {
        return this.status;
    }

    public static enum Status {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;

    }
}

