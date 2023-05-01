/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplays;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PlayerAdvancementTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)AdvancementProgress.class), new AdvancementProgress.Serializer()).registerTypeAdapter((Type)((Object)Identifier.class), new Identifier.Serializer()).setPrettyPrinting().create();
    private static final TypeToken<Map<Identifier, AdvancementProgress>> JSON_TYPE = new TypeToken<Map<Identifier, AdvancementProgress>>(){};
    private final DataFixer dataFixer;
    private final PlayerManager playerManager;
    private final Path filePath;
    private final Map<Advancement, AdvancementProgress> progress = new LinkedHashMap<Advancement, AdvancementProgress>();
    private final Set<Advancement> visibleAdvancements = new HashSet<Advancement>();
    private final Set<Advancement> progressUpdates = new HashSet<Advancement>();
    private final Set<Advancement> updatedRoots = new HashSet<Advancement>();
    private ServerPlayerEntity owner;
    @Nullable
    private Advancement currentDisplayTab;
    private boolean dirty = true;

    public PlayerAdvancementTracker(DataFixer dataFixer, PlayerManager playerManager, ServerAdvancementLoader advancementLoader, Path filePath, ServerPlayerEntity owner) {
        this.dataFixer = dataFixer;
        this.playerManager = playerManager;
        this.filePath = filePath;
        this.owner = owner;
        this.load(advancementLoader);
    }

    public void setOwner(ServerPlayerEntity owner) {
        this.owner = owner;
    }

    public void clearCriteria() {
        for (Criterion<?> lv : Criteria.getCriteria()) {
            lv.endTracking(this);
        }
    }

    public void reload(ServerAdvancementLoader advancementLoader) {
        this.clearCriteria();
        this.progress.clear();
        this.visibleAdvancements.clear();
        this.updatedRoots.clear();
        this.progressUpdates.clear();
        this.dirty = true;
        this.currentDisplayTab = null;
        this.load(advancementLoader);
    }

    private void beginTrackingAllAdvancements(ServerAdvancementLoader advancementLoader) {
        for (Advancement lv : advancementLoader.getAdvancements()) {
            this.beginTracking(lv);
        }
    }

    private void rewardEmptyAdvancements(ServerAdvancementLoader advancementLoader) {
        for (Advancement lv : advancementLoader.getAdvancements()) {
            if (!lv.getCriteria().isEmpty()) continue;
            this.grantCriterion(lv, "");
            lv.getRewards().apply(this.owner);
        }
    }

    private void load(ServerAdvancementLoader advancementLoader) {
        if (Files.isRegularFile(this.filePath, new LinkOption[0])) {
            try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(this.filePath, StandardCharsets.UTF_8));){
                jsonReader.setLenient(false);
                Dynamic<JsonElement> dynamic = new Dynamic<JsonElement>(JsonOps.INSTANCE, Streams.parse(jsonReader));
                int i = dynamic.get("DataVersion").asInt(1343);
                dynamic = dynamic.remove("DataVersion");
                dynamic = DataFixTypes.ADVANCEMENTS.update(this.dataFixer, dynamic, i);
                Map<Identifier, AdvancementProgress> map = GSON.getAdapter(JSON_TYPE).fromJsonTree(dynamic.getValue());
                if (map == null) {
                    throw new JsonParseException("Found null for advancements");
                }
                map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
                    Advancement lv = advancementLoader.get((Identifier)entry.getKey());
                    if (lv == null) {
                        LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", entry.getKey(), (Object)this.filePath);
                        return;
                    }
                    this.initProgress(lv, (AdvancementProgress)entry.getValue());
                    this.progressUpdates.add(lv);
                    this.onStatusUpdate(lv);
                });
            }
            catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse player advancements in {}", (Object)this.filePath, (Object)jsonParseException);
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't access player advancements in {}", (Object)this.filePath, (Object)iOException);
            }
        }
        this.rewardEmptyAdvancements(advancementLoader);
        this.beginTrackingAllAdvancements(advancementLoader);
    }

    public void save() {
        LinkedHashMap<Identifier, AdvancementProgress> map = new LinkedHashMap<Identifier, AdvancementProgress>();
        for (Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            AdvancementProgress lv = entry.getValue();
            if (!lv.isAnyObtained()) continue;
            map.put(entry.getKey().getId(), lv);
        }
        JsonElement jsonElement = GSON.toJsonTree(map);
        jsonElement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
        try {
            PathUtil.createDirectories(this.filePath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(this.filePath, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(jsonElement, (Appendable)writer);
            }
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't save player advancements to {}", (Object)this.filePath, (Object)iOException);
        }
    }

    public boolean grantCriterion(Advancement advancement, String criterionName) {
        boolean bl = false;
        AdvancementProgress lv = this.getProgress(advancement);
        boolean bl2 = lv.isDone();
        if (lv.obtain(criterionName)) {
            this.endTrackingCompleted(advancement);
            this.progressUpdates.add(advancement);
            bl = true;
            if (!bl2 && lv.isDone()) {
                advancement.getRewards().apply(this.owner);
                if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && this.owner.world.getGameRules().getBoolean(GameRules.ANNOUNCE_ADVANCEMENTS)) {
                    this.playerManager.broadcast(Text.translatable("chat.type.advancement." + advancement.getDisplay().getFrame().getId(), this.owner.getDisplayName(), advancement.toHoverableText()), false);
                }
            }
        }
        if (!bl2 && lv.isDone()) {
            this.onStatusUpdate(advancement);
        }
        return bl;
    }

    public boolean revokeCriterion(Advancement advancement, String criterionName) {
        boolean bl = false;
        AdvancementProgress lv = this.getProgress(advancement);
        boolean bl2 = lv.isDone();
        if (lv.reset(criterionName)) {
            this.beginTracking(advancement);
            this.progressUpdates.add(advancement);
            bl = true;
        }
        if (bl2 && !lv.isDone()) {
            this.onStatusUpdate(advancement);
        }
        return bl;
    }

    private void onStatusUpdate(Advancement advancement) {
        this.updatedRoots.add(advancement.getRoot());
    }

    private void beginTracking(Advancement advancement) {
        AdvancementProgress lv = this.getProgress(advancement);
        if (lv.isDone()) {
            return;
        }
        for (Map.Entry<String, AdvancementCriterion> entry : advancement.getCriteria().entrySet()) {
            Criterion<CriterionConditions> lv4;
            CriterionConditions lv3;
            CriterionProgress lv2 = lv.getCriterionProgress(entry.getKey());
            if (lv2 == null || lv2.isObtained() || (lv3 = entry.getValue().getConditions()) == null || (lv4 = Criteria.getById(lv3.getId())) == null) continue;
            lv4.beginTrackingCondition(this, new Criterion.ConditionsContainer<CriterionConditions>(lv3, advancement, entry.getKey()));
        }
    }

    private void endTrackingCompleted(Advancement advancement) {
        AdvancementProgress lv = this.getProgress(advancement);
        for (Map.Entry<String, AdvancementCriterion> entry : advancement.getCriteria().entrySet()) {
            Criterion<CriterionConditions> lv4;
            CriterionConditions lv3;
            CriterionProgress lv2 = lv.getCriterionProgress(entry.getKey());
            if (lv2 == null || !lv2.isObtained() && !lv.isDone() || (lv3 = entry.getValue().getConditions()) == null || (lv4 = Criteria.getById(lv3.getId())) == null) continue;
            lv4.endTrackingCondition(this, new Criterion.ConditionsContainer<CriterionConditions>(lv3, advancement, entry.getKey()));
        }
    }

    public void sendUpdate(ServerPlayerEntity player) {
        if (this.dirty || !this.updatedRoots.isEmpty() || !this.progressUpdates.isEmpty()) {
            HashMap<Identifier, AdvancementProgress> map = new HashMap<Identifier, AdvancementProgress>();
            HashSet<Advancement> set = new HashSet<Advancement>();
            HashSet<Identifier> set2 = new HashSet<Identifier>();
            for (Advancement lv : this.updatedRoots) {
                this.calculateDisplay(lv, set, set2);
            }
            this.updatedRoots.clear();
            for (Advancement lv : this.progressUpdates) {
                if (!this.visibleAdvancements.contains(lv)) continue;
                map.put(lv.getId(), this.progress.get(lv));
            }
            this.progressUpdates.clear();
            if (!(map.isEmpty() && set.isEmpty() && set2.isEmpty())) {
                player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(this.dirty, set, set2, map));
            }
        }
        this.dirty = false;
    }

    public void setDisplayTab(@Nullable Advancement advancement) {
        Advancement lv = this.currentDisplayTab;
        this.currentDisplayTab = advancement != null && advancement.getParent() == null && advancement.getDisplay() != null ? advancement : null;
        if (lv != this.currentDisplayTab) {
            this.owner.networkHandler.sendPacket(new SelectAdvancementTabS2CPacket(this.currentDisplayTab == null ? null : this.currentDisplayTab.getId()));
        }
    }

    public AdvancementProgress getProgress(Advancement advancement) {
        AdvancementProgress lv = this.progress.get(advancement);
        if (lv == null) {
            lv = new AdvancementProgress();
            this.initProgress(advancement, lv);
        }
        return lv;
    }

    private void initProgress(Advancement advancement, AdvancementProgress progress) {
        progress.init(advancement.getCriteria(), advancement.getRequirements());
        this.progress.put(advancement, progress);
    }

    private void calculateDisplay(Advancement root, Set<Advancement> added, Set<Identifier> removed) {
        AdvancementDisplays.calculateDisplay(root, advancement -> this.getProgress((Advancement)advancement).isDone(), (advancement, displayed) -> {
            if (displayed) {
                if (this.visibleAdvancements.add(advancement)) {
                    added.add(advancement);
                    if (this.progress.containsKey(advancement)) {
                        this.progressUpdates.add(advancement);
                    }
                }
            } else if (this.visibleAdvancements.remove(advancement)) {
                removed.add(advancement.getId());
            }
        });
    }
}

