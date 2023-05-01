/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.text.Text;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.updater.WorldUpdater;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] args) {
        SharedConstants.createGameVersion();
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec optionSpec8 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("singleplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec11 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec12 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpec14 = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec<Path> optionSpec15 = optionParser.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec<String> optionSpec16 = optionParser.nonOptions();
        try {
            SaveLoader lv9;
            boolean bl;
            OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has(optionSpec8)) {
                optionParser.printHelpOn(System.err);
                return;
            }
            Path path = optionSet.valueOf(optionSpec15);
            if (path != null) {
                Main.writePidFile(path);
            }
            CrashReport.initCrashReport();
            if (optionSet.has(optionSpec14)) {
                FlightProfiler.INSTANCE.start(InstanceType.SERVER);
            }
            Bootstrap.initialize();
            Bootstrap.logMissing();
            Util.startTimerHack();
            Path path2 = Paths.get("server.properties", new String[0]);
            ServerPropertiesLoader lv = new ServerPropertiesLoader(path2);
            lv.store();
            Path path3 = Paths.get("eula.txt", new String[0]);
            EulaReader lv2 = new EulaReader(path3);
            if (optionSet.has(optionSpec2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path2.toAbsolutePath(), (Object)path3.toAbsolutePath());
                return;
            }
            if (!lv2.isEulaAgreedTo()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File(optionSet.valueOf(optionSpec10));
            ApiServices lv3 = ApiServices.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String string = Optional.ofNullable(optionSet.valueOf(optionSpec11)).orElse(lv.getPropertiesHandler().levelName);
            LevelStorage lv4 = LevelStorage.create(file.toPath());
            LevelStorage.Session lv5 = lv4.createSession(string);
            LevelSummary lv6 = lv5.getLevelSummary();
            if (lv6 != null) {
                if (lv6.requiresConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!lv6.isVersionAvailable()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            }
            if (bl = optionSet.has(optionSpec7)) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            ResourcePackManager lv7 = VanillaDataPackProvider.createManager(lv5.getDirectory(WorldSavePath.DATAPACKS));
            try {
                SaveLoading.ServerConfig lv8 = Main.createServerConfig(lv.getPropertiesHandler(), lv5, bl, lv7);
                lv9 = (SaveLoader)Util.waitAndApply(applyExecutor -> SaveLoading.load(lv8, context -> {
                    DimensionOptionsRegistryHolder lv4;
                    GeneratorOptions lv3;
                    LevelInfo lv2;
                    Registry<DimensionOptions> lv = context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION);
                    RegistryOps<NbtElement> dynamicOps = RegistryOps.of(NbtOps.INSTANCE, context.worldGenRegistryManager());
                    Pair<SaveProperties, DimensionOptionsRegistryHolder.DimensionsConfig> pair = lv5.readLevelProperties(dynamicOps, context.dataConfiguration(), lv, context.worldGenRegistryManager().getRegistryLifecycle());
                    if (pair != null) {
                        return new SaveLoading.LoadContext<SaveProperties>(pair.getFirst(), pair.getSecond().toDynamicRegistryManager());
                    }
                    if (optionSet.has(optionSpec3)) {
                        lv2 = MinecraftServer.DEMO_LEVEL_INFO;
                        lv3 = GeneratorOptions.DEMO_OPTIONS;
                        lv4 = WorldPresets.createDemoOptions(context.worldGenRegistryManager());
                    } else {
                        ServerPropertiesHandler lv5 = lv.getPropertiesHandler();
                        lv2 = new LevelInfo(lv5.levelName, lv5.gameMode, lv5.hardcore, lv5.difficulty, false, new GameRules(), context.dataConfiguration());
                        lv3 = optionSet.has(optionSpec4) ? lv5.generatorOptions.withBonusChest(true) : lv5.generatorOptions;
                        lv4 = lv5.createDimensionsRegistryHolder(context.worldGenRegistryManager());
                    }
                    DimensionOptionsRegistryHolder.DimensionsConfig lv6 = lv4.toConfig(lv);
                    Lifecycle lifecycle = lv6.getLifecycle().add(context.worldGenRegistryManager().getRegistryLifecycle());
                    return new SaveLoading.LoadContext<LevelProperties>(new LevelProperties(lv2, lv3, lv6.specialWorldProperty(), lifecycle), lv6.toDynamicRegistryManager());
                }, SaveLoader::new, Util.getMainWorkerExecutor(), applyExecutor)).get();
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", exception);
                return;
            }
            DynamicRegistryManager.Immutable lv10 = lv9.combinedDynamicRegistries().getCombinedRegistryManager();
            if (optionSet.has(optionSpec5)) {
                Main.forceUpgradeWorld(lv5, Schemas.getFixer(), optionSet.has(optionSpec6), () -> true, lv10.get(RegistryKeys.DIMENSION));
            }
            SaveProperties lv11 = lv9.saveProperties();
            lv5.backupLevelDataFile(lv10, lv11);
            final MinecraftDedicatedServer lv12 = MinecraftServer.startServer(thread -> {
                boolean bl;
                MinecraftDedicatedServer lv = new MinecraftDedicatedServer((Thread)thread, lv5, lv7, lv9, lv, Schemas.getFixer(), lv3, WorldGenerationProgressLogger::new);
                lv.setHostProfile(optionSet.has(optionSpec9) ? new GameProfile(null, (String)optionSet.valueOf(optionSpec9)) : null);
                lv.setServerPort((Integer)optionSet.valueOf(optionSpec12));
                lv.setDemo(optionSet.has(optionSpec3));
                lv.setServerId((String)optionSet.valueOf(optionSpec13));
                boolean bl2 = bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec16).contains("nogui");
                if (bl && !GraphicsEnvironment.isHeadless()) {
                    lv.createGui();
                }
                return lv;
            });
            Thread thread2 = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    lv12.stop(true);
                }
            };
            thread2.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread2);
        }
        catch (Exception exception2) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", exception2);
        }
    }

    private static void writePidFile(Path path) {
        try {
            long l = ProcessHandle.current().pid();
            Files.writeString(path, (CharSequence)Long.toString(l), new OpenOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    private static SaveLoading.ServerConfig createServerConfig(ServerPropertiesHandler serverPropertiesHandler, LevelStorage.Session session, boolean safeMode, ResourcePackManager dataPackManager) {
        DataConfiguration lv2;
        boolean bl2;
        DataConfiguration lv = session.getDataPackSettings();
        if (lv != null) {
            bl2 = false;
            lv2 = lv;
        } else {
            bl2 = true;
            lv2 = new DataConfiguration(serverPropertiesHandler.dataPackSettings, FeatureFlags.DEFAULT_ENABLED_FEATURES);
        }
        SaveLoading.DataPacks lv3 = new SaveLoading.DataPacks(dataPackManager, lv2, safeMode, bl2);
        return new SaveLoading.ServerConfig(lv3, CommandManager.RegistrationEnvironment.DEDICATED, serverPropertiesHandler.functionPermissionLevel);
    }

    private static void forceUpgradeWorld(LevelStorage.Session session, DataFixer dataFixer, boolean eraseCache, BooleanSupplier continueCheck, Registry<DimensionOptions> dimensionOptionsRegistry) {
        LOGGER.info("Forcing world upgrade!");
        WorldUpdater lv = new WorldUpdater(session, dataFixer, dimensionOptionsRegistry, eraseCache);
        Text lv2 = null;
        while (!lv.isDone()) {
            int i;
            Text lv3 = lv.getStatus();
            if (lv2 != lv3) {
                lv2 = lv3;
                LOGGER.info(lv.getStatus().getString());
            }
            if ((i = lv.getTotalChunkCount()) > 0) {
                int j = lv.getUpgradedChunkCount() + lv.getSkippedChunkCount();
                LOGGER.info("{}% completed ({} / {} chunks)...", MathHelper.floor((float)j / (float)i * 100.0f), j, i);
            }
            if (!continueCheck.getAsBoolean()) {
                lv.cancel();
                continue;
            }
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {}
        }
    }
}

