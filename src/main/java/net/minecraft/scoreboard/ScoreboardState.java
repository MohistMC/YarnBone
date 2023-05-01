/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.scoreboard;

import java.util.Collection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

public class ScoreboardState
extends PersistentState {
    public static final String SCOREBOARD_KEY = "scoreboard";
    private final Scoreboard scoreboard;

    public ScoreboardState(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public ScoreboardState readNbt(NbtCompound nbt) {
        this.readObjectivesNbt(nbt.getList("Objectives", NbtElement.COMPOUND_TYPE));
        this.scoreboard.readNbt(nbt.getList("PlayerScores", NbtElement.COMPOUND_TYPE));
        if (nbt.contains("DisplaySlots", NbtElement.COMPOUND_TYPE)) {
            this.readDisplaySlotsNbt(nbt.getCompound("DisplaySlots"));
        }
        if (nbt.contains("Teams", NbtElement.LIST_TYPE)) {
            this.readTeamsNbt(nbt.getList("Teams", NbtElement.COMPOUND_TYPE));
        }
        return this;
    }

    private void readTeamsNbt(NbtList nbt) {
        for (int i = 0; i < nbt.size(); ++i) {
            AbstractTeam.CollisionRule lv6;
            AbstractTeam.VisibilityRule lv5;
            MutableText lv4;
            NbtCompound lv = nbt.getCompound(i);
            String string = lv.getString("Name");
            Team lv2 = this.scoreboard.addTeam(string);
            MutableText lv3 = Text.Serializer.fromJson(lv.getString("DisplayName"));
            if (lv3 != null) {
                lv2.setDisplayName(lv3);
            }
            if (lv.contains("TeamColor", NbtElement.STRING_TYPE)) {
                lv2.setColor(Formatting.byName(lv.getString("TeamColor")));
            }
            if (lv.contains("AllowFriendlyFire", NbtElement.NUMBER_TYPE)) {
                lv2.setFriendlyFireAllowed(lv.getBoolean("AllowFriendlyFire"));
            }
            if (lv.contains("SeeFriendlyInvisibles", NbtElement.NUMBER_TYPE)) {
                lv2.setShowFriendlyInvisibles(lv.getBoolean("SeeFriendlyInvisibles"));
            }
            if (lv.contains("MemberNamePrefix", NbtElement.STRING_TYPE) && (lv4 = Text.Serializer.fromJson(lv.getString("MemberNamePrefix"))) != null) {
                lv2.setPrefix(lv4);
            }
            if (lv.contains("MemberNameSuffix", NbtElement.STRING_TYPE) && (lv4 = Text.Serializer.fromJson(lv.getString("MemberNameSuffix"))) != null) {
                lv2.setSuffix(lv4);
            }
            if (lv.contains("NameTagVisibility", NbtElement.STRING_TYPE) && (lv5 = AbstractTeam.VisibilityRule.getRule(lv.getString("NameTagVisibility"))) != null) {
                lv2.setNameTagVisibilityRule(lv5);
            }
            if (lv.contains("DeathMessageVisibility", NbtElement.STRING_TYPE) && (lv5 = AbstractTeam.VisibilityRule.getRule(lv.getString("DeathMessageVisibility"))) != null) {
                lv2.setDeathMessageVisibilityRule(lv5);
            }
            if (lv.contains("CollisionRule", NbtElement.STRING_TYPE) && (lv6 = AbstractTeam.CollisionRule.getRule(lv.getString("CollisionRule"))) != null) {
                lv2.setCollisionRule(lv6);
            }
            this.readTeamPlayersNbt(lv2, lv.getList("Players", NbtElement.STRING_TYPE));
        }
    }

    private void readTeamPlayersNbt(Team team, NbtList nbt) {
        for (int i = 0; i < nbt.size(); ++i) {
            this.scoreboard.addPlayerToTeam(nbt.getString(i), team);
        }
    }

    private void readDisplaySlotsNbt(NbtCompound nbt) {
        for (int i = 0; i < 19; ++i) {
            if (!nbt.contains("slot_" + i, NbtElement.STRING_TYPE)) continue;
            String string = nbt.getString("slot_" + i);
            ScoreboardObjective lv = this.scoreboard.getNullableObjective(string);
            this.scoreboard.setObjectiveSlot(i, lv);
        }
    }

    private void readObjectivesNbt(NbtList nbt) {
        for (int i = 0; i < nbt.size(); ++i) {
            NbtCompound lv = nbt.getCompound(i);
            ScoreboardCriterion.getOrCreateStatCriterion(lv.getString("CriteriaName")).ifPresent(criterion -> {
                String string = lv.getString("Name");
                MutableText lv = Text.Serializer.fromJson(lv.getString("DisplayName"));
                ScoreboardCriterion.RenderType lv2 = ScoreboardCriterion.RenderType.getType(lv.getString("RenderType"));
                this.scoreboard.addObjective(string, (ScoreboardCriterion)criterion, lv, lv2);
            });
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("Objectives", this.objectivesToNbt());
        nbt.put("PlayerScores", this.scoreboard.toNbt());
        nbt.put("Teams", this.teamsToNbt());
        this.writeDisplaySlotsNbt(nbt);
        return nbt;
    }

    private NbtList teamsToNbt() {
        NbtList lv = new NbtList();
        Collection<Team> collection = this.scoreboard.getTeams();
        for (Team lv2 : collection) {
            NbtCompound lv3 = new NbtCompound();
            lv3.putString("Name", lv2.getName());
            lv3.putString("DisplayName", Text.Serializer.toJson(lv2.getDisplayName()));
            if (lv2.getColor().getColorIndex() >= 0) {
                lv3.putString("TeamColor", lv2.getColor().getName());
            }
            lv3.putBoolean("AllowFriendlyFire", lv2.isFriendlyFireAllowed());
            lv3.putBoolean("SeeFriendlyInvisibles", lv2.shouldShowFriendlyInvisibles());
            lv3.putString("MemberNamePrefix", Text.Serializer.toJson(lv2.getPrefix()));
            lv3.putString("MemberNameSuffix", Text.Serializer.toJson(lv2.getSuffix()));
            lv3.putString("NameTagVisibility", lv2.getNameTagVisibilityRule().name);
            lv3.putString("DeathMessageVisibility", lv2.getDeathMessageVisibilityRule().name);
            lv3.putString("CollisionRule", lv2.getCollisionRule().name);
            NbtList lv4 = new NbtList();
            for (String string : lv2.getPlayerList()) {
                lv4.add(NbtString.of(string));
            }
            lv3.put("Players", lv4);
            lv.add(lv3);
        }
        return lv;
    }

    private void writeDisplaySlotsNbt(NbtCompound nbt) {
        NbtCompound lv = new NbtCompound();
        boolean bl = false;
        for (int i = 0; i < 19; ++i) {
            ScoreboardObjective lv2 = this.scoreboard.getObjectiveForSlot(i);
            if (lv2 == null) continue;
            lv.putString("slot_" + i, lv2.getName());
            bl = true;
        }
        if (bl) {
            nbt.put("DisplaySlots", lv);
        }
    }

    private NbtList objectivesToNbt() {
        NbtList lv = new NbtList();
        Collection<ScoreboardObjective> collection = this.scoreboard.getObjectives();
        for (ScoreboardObjective lv2 : collection) {
            if (lv2.getCriterion() == null) continue;
            NbtCompound lv3 = new NbtCompound();
            lv3.putString("Name", lv2.getName());
            lv3.putString("CriteriaName", lv2.getCriterion().getName());
            lv3.putString("DisplayName", Text.Serializer.toJson(lv2.getDisplayName()));
            lv3.putString("RenderType", lv2.getRenderType().getName());
            lv.add(lv3);
        }
        return lv;
    }
}

