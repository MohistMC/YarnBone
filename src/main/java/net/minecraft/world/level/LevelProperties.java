/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.level;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.WorldGenSettings;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LevelProperties
implements ServerWorldProperties,
SaveProperties {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final String PLAYER_KEY = "Player";
    protected static final String WORLD_GEN_SETTINGS_KEY = "WorldGenSettings";
    private LevelInfo levelInfo;
    private final GeneratorOptions generatorOptions;
    private final SpecialProperty specialProperty;
    private final Lifecycle lifecycle;
    private int spawnX;
    private int spawnY;
    private int spawnZ;
    private float spawnAngle;
    private long time;
    private long timeOfDay;
    @Nullable
    private final DataFixer dataFixer;
    private final int dataVersion;
    private boolean playerDataLoaded;
    @Nullable
    private NbtCompound playerData;
    private final int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private boolean initialized;
    private boolean difficultyLocked;
    private WorldBorder.Properties worldBorder;
    private NbtCompound dragonFight;
    @Nullable
    private NbtCompound customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    @Nullable
    private UUID wanderingTraderId;
    private final Set<String> serverBrands;
    private boolean modded;
    private final Timer<MinecraftServer> scheduledEvents;

    private LevelProperties(@Nullable DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, boolean modded, int spawnX, int spawnY, int spawnZ, float spawnAngle, long time, long timeOfDay, int version, int clearWeatherTime, int rainTime, boolean raining, int thunderTime, boolean thundering, boolean initialized, boolean difficultyLocked, WorldBorder.Properties worldBorder, int wanderingTraderSpawnDelay, int wanderingTraderSpawnChance, @Nullable UUID wanderingTraderId, Set<String> serverBrands, Timer<MinecraftServer> scheduledEvents, @Nullable NbtCompound customBossEvents, NbtCompound dragonFight, LevelInfo levelInfo, GeneratorOptions generatorOptions, SpecialProperty specialProperty, Lifecycle lifecycle) {
        this.dataFixer = dataFixer;
        this.modded = modded;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.spawnAngle = spawnAngle;
        this.time = time;
        this.timeOfDay = timeOfDay;
        this.version = version;
        this.clearWeatherTime = clearWeatherTime;
        this.rainTime = rainTime;
        this.raining = raining;
        this.thunderTime = thunderTime;
        this.thundering = thundering;
        this.initialized = initialized;
        this.difficultyLocked = difficultyLocked;
        this.worldBorder = worldBorder;
        this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
        this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
        this.wanderingTraderId = wanderingTraderId;
        this.serverBrands = serverBrands;
        this.playerData = playerData;
        this.dataVersion = dataVersion;
        this.scheduledEvents = scheduledEvents;
        this.customBossEvents = customBossEvents;
        this.dragonFight = dragonFight;
        this.levelInfo = levelInfo;
        this.generatorOptions = generatorOptions;
        this.specialProperty = specialProperty;
        this.lifecycle = lifecycle;
    }

    public LevelProperties(LevelInfo levelInfo, GeneratorOptions generatorOptions, SpecialProperty arg3, Lifecycle lifecycle) {
        this(null, SharedConstants.getGameVersion().getSaveVersion().getId(), null, false, 0, 0, 0, 0.0f, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_BORDER, 0, 0, null, Sets.newLinkedHashSet(), new Timer<MinecraftServer>(TimerCallbackSerializer.INSTANCE), null, new NbtCompound(), levelInfo.withCopiedGameRules(), generatorOptions, arg3, lifecycle);
    }

    public static <T> LevelProperties readProperties(Dynamic<T> dynamic2, DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle) {
        long l = dynamic2.get("Time").asLong(0L);
        NbtCompound lv = (NbtCompound)dynamic2.get("DragonFight").result().orElseGet(() -> dynamic2.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap()).convert(NbtOps.INSTANCE).getValue();
        return new LevelProperties(dataFixer, dataVersion, playerData, dynamic2.get("WasModded").asBoolean(false), dynamic2.get("SpawnX").asInt(0), dynamic2.get("SpawnY").asInt(0), dynamic2.get("SpawnZ").asInt(0), dynamic2.get("SpawnAngle").asFloat(0.0f), l, dynamic2.get("DayTime").asLong(l), saveVersionInfo.getLevelFormatVersion(), dynamic2.get("clearWeatherTime").asInt(0), dynamic2.get("rainTime").asInt(0), dynamic2.get("raining").asBoolean(false), dynamic2.get("thunderTime").asInt(0), dynamic2.get("thundering").asBoolean(false), dynamic2.get("initialized").asBoolean(true), dynamic2.get("DifficultyLocked").asBoolean(false), WorldBorder.Properties.fromDynamic(dynamic2, WorldBorder.DEFAULT_BORDER), dynamic2.get("WanderingTraderSpawnDelay").asInt(0), dynamic2.get("WanderingTraderSpawnChance").asInt(0), dynamic2.get("WanderingTraderId").read(Uuids.INT_STREAM_CODEC).result().orElse(null), dynamic2.get("ServerBrands").asStream().flatMap(dynamic -> dynamic.asString().result().stream()).collect(Collectors.toCollection(Sets::newLinkedHashSet)), new Timer<MinecraftServer>(TimerCallbackSerializer.INSTANCE, dynamic2.get("ScheduledEvents").asStream()), (NbtCompound)dynamic2.get("CustomBossEvents").orElseEmptyMap().getValue(), lv, levelInfo, generatorOptions, specialProperty, lifecycle);
    }

    @Override
    public NbtCompound cloneWorldNbt(DynamicRegistryManager registryManager, @Nullable NbtCompound playerNbt) {
        this.loadPlayerData();
        if (playerNbt == null) {
            playerNbt = this.playerData;
        }
        NbtCompound lv = new NbtCompound();
        this.updateProperties(registryManager, lv, playerNbt);
        return lv;
    }

    private void updateProperties(DynamicRegistryManager registryManager, NbtCompound levelNbt, @Nullable NbtCompound playerNbt) {
        NbtList lv = new NbtList();
        this.serverBrands.stream().map(NbtString::of).forEach(lv::add);
        levelNbt.put("ServerBrands", lv);
        levelNbt.putBoolean("WasModded", this.modded);
        NbtCompound lv2 = new NbtCompound();
        lv2.putString("Name", SharedConstants.getGameVersion().getName());
        lv2.putInt("Id", SharedConstants.getGameVersion().getSaveVersion().getId());
        lv2.putBoolean("Snapshot", !SharedConstants.getGameVersion().isStable());
        lv2.putString("Series", SharedConstants.getGameVersion().getSaveVersion().getSeries());
        levelNbt.put("Version", lv2);
        NbtHelper.putDataVersion(levelNbt);
        RegistryOps<NbtElement> dynamicOps = RegistryOps.of(NbtOps.INSTANCE, registryManager);
        WorldGenSettings.encode(dynamicOps, this.generatorOptions, registryManager).resultOrPartial(Util.addPrefix("WorldGenSettings: ", LOGGER::error)).ifPresent(arg2 -> levelNbt.put(WORLD_GEN_SETTINGS_KEY, (NbtElement)arg2));
        levelNbt.putInt("GameType", this.levelInfo.getGameMode().getId());
        levelNbt.putInt("SpawnX", this.spawnX);
        levelNbt.putInt("SpawnY", this.spawnY);
        levelNbt.putInt("SpawnZ", this.spawnZ);
        levelNbt.putFloat("SpawnAngle", this.spawnAngle);
        levelNbt.putLong("Time", this.time);
        levelNbt.putLong("DayTime", this.timeOfDay);
        levelNbt.putLong("LastPlayed", Util.getEpochTimeMs());
        levelNbt.putString("LevelName", this.levelInfo.getLevelName());
        levelNbt.putInt("version", 19133);
        levelNbt.putInt("clearWeatherTime", this.clearWeatherTime);
        levelNbt.putInt("rainTime", this.rainTime);
        levelNbt.putBoolean("raining", this.raining);
        levelNbt.putInt("thunderTime", this.thunderTime);
        levelNbt.putBoolean("thundering", this.thundering);
        levelNbt.putBoolean("hardcore", this.levelInfo.isHardcore());
        levelNbt.putBoolean("allowCommands", this.levelInfo.areCommandsAllowed());
        levelNbt.putBoolean("initialized", this.initialized);
        this.worldBorder.writeNbt(levelNbt);
        levelNbt.putByte("Difficulty", (byte)this.levelInfo.getDifficulty().getId());
        levelNbt.putBoolean("DifficultyLocked", this.difficultyLocked);
        levelNbt.put("GameRules", this.levelInfo.getGameRules().toNbt());
        levelNbt.put("DragonFight", this.dragonFight);
        if (playerNbt != null) {
            levelNbt.put(PLAYER_KEY, playerNbt);
        }
        DataResult<NbtElement> dataResult = DataConfiguration.CODEC.encodeStart(NbtOps.INSTANCE, this.levelInfo.getDataConfiguration());
        dataResult.get().ifLeft(dataConfiguration -> levelNbt.copyFrom((NbtCompound)dataConfiguration)).ifRight(result -> LOGGER.warn("Failed to encode configuration {}", (Object)result.message()));
        if (this.customBossEvents != null) {
            levelNbt.put("CustomBossEvents", this.customBossEvents);
        }
        levelNbt.put("ScheduledEvents", this.scheduledEvents.toNbt());
        levelNbt.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        levelNbt.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
        if (this.wanderingTraderId != null) {
            levelNbt.putUuid("WanderingTraderId", this.wanderingTraderId);
        }
    }

    @Override
    public int getSpawnX() {
        return this.spawnX;
    }

    @Override
    public int getSpawnY() {
        return this.spawnY;
    }

    @Override
    public int getSpawnZ() {
        return this.spawnZ;
    }

    @Override
    public float getSpawnAngle() {
        return this.spawnAngle;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public long getTimeOfDay() {
        return this.timeOfDay;
    }

    private void loadPlayerData() {
        if (this.playerDataLoaded || this.playerData == null) {
            return;
        }
        if (this.dataVersion < SharedConstants.getGameVersion().getSaveVersion().getId()) {
            if (this.dataFixer == null) {
                throw Util.throwOrPause(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
            }
            this.playerData = DataFixTypes.PLAYER.update(this.dataFixer, this.playerData, this.dataVersion);
        }
        this.playerDataLoaded = true;
    }

    @Override
    public NbtCompound getPlayerData() {
        this.loadPlayerData();
        return this.playerData;
    }

    @Override
    public void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    @Override
    public void setSpawnY(int spawnY) {
        this.spawnY = spawnY;
    }

    @Override
    public void setSpawnZ(int spawnZ) {
        this.spawnZ = spawnZ;
    }

    @Override
    public void setSpawnAngle(float spawnAngle) {
        this.spawnAngle = spawnAngle;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @Override
    public void setSpawnPos(BlockPos pos, float angle) {
        this.spawnX = pos.getX();
        this.spawnY = pos.getY();
        this.spawnZ = pos.getZ();
        this.spawnAngle = angle;
    }

    @Override
    public String getLevelName() {
        return this.levelInfo.getLevelName();
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public int getClearWeatherTime() {
        return this.clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int clearWeatherTime) {
        this.clearWeatherTime = clearWeatherTime;
    }

    @Override
    public boolean isThundering() {
        return this.thundering;
    }

    @Override
    public void setThundering(boolean thundering) {
        this.thundering = thundering;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int thunderTime) {
        this.thunderTime = thunderTime;
    }

    @Override
    public boolean isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    @Override
    public int getRainTime() {
        return this.rainTime;
    }

    @Override
    public void setRainTime(int rainTime) {
        this.rainTime = rainTime;
    }

    @Override
    public GameMode getGameMode() {
        return this.levelInfo.getGameMode();
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        this.levelInfo = this.levelInfo.withGameMode(gameMode);
    }

    @Override
    public boolean isHardcore() {
        return this.levelInfo.isHardcore();
    }

    @Override
    public boolean areCommandsAllowed() {
        return this.levelInfo.areCommandsAllowed();
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public GameRules getGameRules() {
        return this.levelInfo.getGameRules();
    }

    @Override
    public WorldBorder.Properties getWorldBorder() {
        return this.worldBorder;
    }

    @Override
    public void setWorldBorder(WorldBorder.Properties worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Difficulty getDifficulty() {
        return this.levelInfo.getDifficulty();
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.levelInfo = this.levelInfo.withDifficulty(difficulty);
    }

    @Override
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }

    @Override
    public void setDifficultyLocked(boolean difficultyLocked) {
        this.difficultyLocked = difficultyLocked;
    }

    @Override
    public Timer<MinecraftServer> getScheduledEvents() {
        return this.scheduledEvents;
    }

    @Override
    public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
        ServerWorldProperties.super.populateCrashReport(reportSection, world);
        SaveProperties.super.populateCrashReport(reportSection);
    }

    @Override
    public GeneratorOptions getGeneratorOptions() {
        return this.generatorOptions;
    }

    @Override
    public boolean isFlatWorld() {
        return this.specialProperty == SpecialProperty.FLAT;
    }

    @Override
    public boolean isDebugWorld() {
        return this.specialProperty == SpecialProperty.DEBUG;
    }

    @Override
    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    @Override
    public NbtCompound getDragonFight() {
        return this.dragonFight;
    }

    @Override
    public void setDragonFight(NbtCompound dragonFight) {
        this.dragonFight = dragonFight;
    }

    @Override
    public DataConfiguration getDataConfiguration() {
        return this.levelInfo.getDataConfiguration();
    }

    @Override
    public void updateLevelInfo(DataConfiguration dataConfiguration) {
        this.levelInfo = this.levelInfo.withDataConfiguration(dataConfiguration);
    }

    @Override
    @Nullable
    public NbtCompound getCustomBossEvents() {
        return this.customBossEvents;
    }

    @Override
    public void setCustomBossEvents(@Nullable NbtCompound customBossEvents) {
        this.customBossEvents = customBossEvents;
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return this.wanderingTraderSpawnDelay;
    }

    @Override
    public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay) {
        this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return this.wanderingTraderSpawnChance;
    }

    @Override
    public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance) {
        this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
    }

    @Override
    @Nullable
    public UUID getWanderingTraderId() {
        return this.wanderingTraderId;
    }

    @Override
    public void setWanderingTraderId(UUID wanderingTraderId) {
        this.wanderingTraderId = wanderingTraderId;
    }

    @Override
    public void addServerBrand(String brand, boolean modded) {
        this.serverBrands.add(brand);
        this.modded |= modded;
    }

    @Override
    public boolean isModded() {
        return this.modded;
    }

    @Override
    public Set<String> getServerBrands() {
        return ImmutableSet.copyOf(this.serverBrands);
    }

    @Override
    public ServerWorldProperties getMainWorldProperties() {
        return this;
    }

    @Override
    public LevelInfo getLevelInfo() {
        return this.levelInfo.withCopiedGameRules();
    }

    @Deprecated
    public static enum SpecialProperty {
        NONE,
        FLAT,
        DEBUG;

    }
}

