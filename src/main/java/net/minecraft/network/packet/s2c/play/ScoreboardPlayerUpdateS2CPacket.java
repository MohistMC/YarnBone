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
import net.minecraft.scoreboard.ServerScoreboard;
import org.jetbrains.annotations.Nullable;

public class ScoreboardPlayerUpdateS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final String playerName;
    @Nullable
    private final String objectiveName;
    private final int score;
    private final ServerScoreboard.UpdateMode mode;

    public ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode mode, @Nullable String objectiveName, String playerName, int score) {
        if (mode != ServerScoreboard.UpdateMode.REMOVE && objectiveName == null) {
            throw new IllegalArgumentException("Need an objective name");
        }
        this.playerName = playerName;
        this.objectiveName = objectiveName;
        this.score = score;
        this.mode = mode;
    }

    public ScoreboardPlayerUpdateS2CPacket(PacketByteBuf buf) {
        this.playerName = buf.readString();
        this.mode = buf.readEnumConstant(ServerScoreboard.UpdateMode.class);
        String string = buf.readString();
        this.objectiveName = Objects.equals(string, "") ? null : string;
        this.score = this.mode != ServerScoreboard.UpdateMode.REMOVE ? buf.readVarInt() : 0;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.playerName);
        buf.writeEnumConstant(this.mode);
        buf.writeString(this.objectiveName == null ? "" : this.objectiveName);
        if (this.mode != ServerScoreboard.UpdateMode.REMOVE) {
            buf.writeVarInt(this.score);
        }
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onScoreboardPlayerUpdate(this);
    }

    public String getPlayerName() {
        return this.playerName;
    }

    @Nullable
    public String getObjectiveName() {
        return this.objectiveName;
    }

    public int getScore() {
        return this.score;
    }

    public ServerScoreboard.UpdateMode getUpdateMode() {
        return this.mode;
    }
}

