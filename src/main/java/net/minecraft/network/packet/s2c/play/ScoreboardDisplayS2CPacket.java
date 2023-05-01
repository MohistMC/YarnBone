/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import java.util.Objects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.Nullable;

public class ScoreboardDisplayS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int slot;
    private final String name;

    public ScoreboardDisplayS2CPacket(int slot, @Nullable ScoreboardObjective objective) {
        this.slot = slot;
        this.name = objective == null ? "" : objective.getName();
    }

    public ScoreboardDisplayS2CPacket(PacketByteBuf buf) {
        this.slot = buf.readByte();
        this.name = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(this.slot);
        buf.writeString(this.name);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onScoreboardDisplay(this);
    }

    public int getSlot() {
        return this.slot;
    }

    @Nullable
    public String getName() {
        return Objects.equals(this.name, "") ? null : this.name;
    }
}

