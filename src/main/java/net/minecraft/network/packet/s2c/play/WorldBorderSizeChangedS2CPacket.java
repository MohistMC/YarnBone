/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderSizeChangedS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final double sizeLerpTarget;

    public WorldBorderSizeChangedS2CPacket(WorldBorder worldBorder) {
        this.sizeLerpTarget = worldBorder.getSizeLerpTarget();
    }

    public WorldBorderSizeChangedS2CPacket(PacketByteBuf buf) {
        this.sizeLerpTarget = buf.readDouble();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(this.sizeLerpTarget);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onWorldBorderSizeChanged(this);
    }

    public double getSizeLerpTarget() {
        return this.sizeLerpTarget;
    }
}

