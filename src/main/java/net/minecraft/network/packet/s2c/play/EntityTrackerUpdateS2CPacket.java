/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record EntityTrackerUpdateS2CPacket(int id, List<DataTracker.SerializedEntry<?>> trackedValues) implements Packet<ClientPlayPacketListener>
{
    public static final int MARKER_ID = 255;

    public EntityTrackerUpdateS2CPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), EntityTrackerUpdateS2CPacket.read(buf));
    }

    private static void write(List<DataTracker.SerializedEntry<?>> trackedValues, PacketByteBuf buf) {
        for (DataTracker.SerializedEntry<?> lv : trackedValues) {
            lv.write(buf);
        }
        buf.writeByte(255);
    }

    private static List<DataTracker.SerializedEntry<?>> read(PacketByteBuf buf) {
        short i;
        ArrayList list = new ArrayList();
        while ((i = buf.readUnsignedByte()) != 255) {
            list.add(DataTracker.SerializedEntry.fromBuf(buf, i));
        }
        return list;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.id);
        EntityTrackerUpdateS2CPacket.write(this.trackedValues, buf);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onEntityTrackerUpdate(this);
    }
}

