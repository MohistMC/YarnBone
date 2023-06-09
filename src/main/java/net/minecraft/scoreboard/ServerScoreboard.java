/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class ServerScoreboard
extends Scoreboard {
    private final MinecraftServer server;
    private final Set<ScoreboardObjective> objectives = Sets.newHashSet();
    private final List<Runnable> updateListeners = Lists.newArrayList();

    public ServerScoreboard(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void updateScore(ScoreboardPlayerScore score) {
        super.updateScore(score);
        if (this.objectives.contains(score.getObjective())) {
            this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.CHANGE, score.getObjective().getName(), score.getPlayerName(), score.getScore()));
        }
        this.runUpdateListeners();
    }

    @Override
    public void updatePlayerScore(String playerName) {
        super.updatePlayerScore(playerName);
        this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, null, playerName, 0));
        this.runUpdateListeners();
    }

    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        super.updatePlayerScore(playerName, objective);
        if (this.objectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, objective.getName(), playerName, 0));
        }
        this.runUpdateListeners();
    }

    @Override
    public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
        ScoreboardObjective lv = this.getObjectiveForSlot(slot);
        super.setObjectiveSlot(slot, objective);
        if (lv != objective && lv != null) {
            if (this.getSlot(lv) > 0) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.removeScoreboardObjective(lv);
            }
        }
        if (objective != null) {
            if (this.objectives.contains(objective)) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.addScoreboardObjective(objective);
            }
        }
        this.runUpdateListeners();
    }

    @Override
    public boolean addPlayerToTeam(String playerName, Team team) {
        if (super.addPlayerToTeam(playerName, team)) {
            this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.ADD));
            this.runUpdateListeners();
            return true;
        }
        return false;
    }

    @Override
    public void removePlayerFromTeam(String playerName, Team team) {
        super.removePlayerFromTeam(playerName, team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.REMOVE));
        this.runUpdateListeners();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        super.updateObjective(objective);
        this.runUpdateListeners();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        super.updateExistingObjective(objective);
        if (this.objectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE));
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
        if (this.objectives.contains(objective)) {
            this.removeScoreboardObjective(objective);
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        super.updateScoreboardTeamAndPlayers(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true));
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        super.updateScoreboardTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false));
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        super.updateRemovedTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team));
        this.runUpdateListeners();
    }

    public void addUpdateListener(Runnable listener) {
        this.updateListeners.add(listener);
    }

    protected void runUpdateListeners() {
        for (Runnable runnable : this.updateListeners) {
            runnable.run();
        }
    }

    public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
        ArrayList<Packet<?>> list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE));
        for (int i = 0; i < 19; ++i) {
            if (this.getObjectiveForSlot(i) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(i, objective));
        }
        for (ScoreboardPlayerScore lv : this.getAllPlayerScores(objective)) {
            list.add(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.CHANGE, lv.getObjective().getName(), lv.getPlayerName(), lv.getScore()));
        }
        return list;
    }

    public void addScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createChangePackets(objective);
        for (ServerPlayerEntity lv : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> lv2 : list) {
                lv.networkHandler.sendPacket(lv2);
            }
        }
        this.objectives.add(objective);
    }

    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        ArrayList<Packet<?>> list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE));
        for (int i = 0; i < 19; ++i) {
            if (this.getObjectiveForSlot(i) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(i, objective));
        }
        return list;
    }

    public void removeScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createRemovePackets(objective);
        for (ServerPlayerEntity lv : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> lv2 : list) {
                lv.networkHandler.sendPacket(lv2);
            }
        }
        this.objectives.remove(objective);
    }

    public int getSlot(ScoreboardObjective objective) {
        int i = 0;
        for (int j = 0; j < 19; ++j) {
            if (this.getObjectiveForSlot(j) != objective) continue;
            ++i;
        }
        return i;
    }

    public ScoreboardState createState() {
        ScoreboardState lv = new ScoreboardState(this);
        this.addUpdateListener(lv::markDirty);
        return lv;
    }

    public ScoreboardState stateFromNbt(NbtCompound nbt) {
        return this.createState().readNbt(nbt);
    }

    public static enum UpdateMode {
        CHANGE,
        REMOVE;

    }
}

