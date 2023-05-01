/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.integrated;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.DatapackFailureScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class IntegratedServerLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftClient client;
    private final LevelStorage storage;

    public IntegratedServerLoader(MinecraftClient client, LevelStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    public void start(Screen parent, String levelName) {
        this.start(parent, levelName, false, true);
    }

    public void createAndStart(String levelName, LevelInfo levelInfo, GeneratorOptions dynamicRegistryManager, Function<DynamicRegistryManager, DimensionOptionsRegistryHolder> dimensionsRegistrySupplier) {
        LevelStorage.Session lv = this.createSession(levelName);
        if (lv == null) {
            return;
        }
        ResourcePackManager lv2 = VanillaDataPackProvider.createManager(lv);
        DataConfiguration lv3 = levelInfo.getDataConfiguration();
        try {
            SaveLoading.DataPacks lv4 = new SaveLoading.DataPacks(lv2, lv3, false, false);
            SaveLoader lv5 = this.load(lv4, context -> {
                DimensionOptionsRegistryHolder.DimensionsConfig lv = ((DimensionOptionsRegistryHolder)dimensionsRegistrySupplier.apply(context.worldGenRegistryManager())).toConfig(context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION));
                return new SaveLoading.LoadContext<LevelProperties>(new LevelProperties(levelInfo, dynamicRegistryManager, lv.specialWorldProperty(), lv.getLifecycle()), lv.toDynamicRegistryManager());
            }, SaveLoader::new);
            this.client.startIntegratedServer(levelName, lv, lv2, lv5, true);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", exception);
            IntegratedServerLoader.close(lv, levelName);
        }
    }

    @Nullable
    private LevelStorage.Session createSession(String levelName) {
        try {
            return this.storage.createSession(levelName);
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to read level {} data", (Object)levelName, (Object)iOException);
            SystemToast.addWorldAccessFailureToast(this.client, levelName);
            this.client.setScreen(null);
            return null;
        }
    }

    public void start(LevelStorage.Session session, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistryManager, SaveProperties saveProperties) {
        ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
        LifecycledResourceManager lv2 = new SaveLoading.DataPacks(lv, saveProperties.getDataConfiguration(), false, false).load().getSecond();
        this.client.startIntegratedServer(session.getDirectoryName(), session, lv, new SaveLoader(lv2, dataPackContents, dynamicRegistryManager, saveProperties), true);
    }

    private SaveLoader createSaveLoader(LevelStorage.Session session, boolean safeMode, ResourcePackManager dataPackManager) throws Exception {
        SaveLoading.DataPacks lv = this.createDataPackConfig(session, safeMode, dataPackManager);
        return this.load(lv, context -> {
            RegistryOps<NbtElement> dynamicOps = RegistryOps.of(NbtOps.INSTANCE, context.worldGenRegistryManager());
            Registry<DimensionOptions> lv = context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION);
            Pair<SaveProperties, DimensionOptionsRegistryHolder.DimensionsConfig> pair = session.readLevelProperties(dynamicOps, context.dataConfiguration(), lv, context.worldGenRegistryManager().getRegistryLifecycle());
            if (pair == null) {
                throw new IllegalStateException("Failed to load world");
            }
            return new SaveLoading.LoadContext<SaveProperties>(pair.getFirst(), pair.getSecond().toDynamicRegistryManager());
        }, SaveLoader::new);
    }

    public Pair<LevelInfo, GeneratorOptionsHolder> loadForRecreation(LevelStorage.Session session) throws Exception {
        @Environment(value=EnvType.CLIENT)
        record CurrentSettings(LevelInfo levelInfo, GeneratorOptions options, Registry<DimensionOptions> existingDimensionRegistry) {
        }
        ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
        SaveLoading.DataPacks lv2 = this.createDataPackConfig(session, false, lv);
        return this.load(lv2, context -> {
            RegistryOps<NbtElement> dynamicOps = RegistryOps.of(NbtOps.INSTANCE, context.worldGenRegistryManager());
            Registry<DimensionOptions> lv = new SimpleRegistry<DimensionOptions>(RegistryKeys.DIMENSION, Lifecycle.stable()).freeze();
            Pair<SaveProperties, DimensionOptionsRegistryHolder.DimensionsConfig> pair = session.readLevelProperties(dynamicOps, context.dataConfiguration(), lv, context.worldGenRegistryManager().getRegistryLifecycle());
            if (pair == null) {
                throw new IllegalStateException("Failed to load world");
            }
            return new SaveLoading.LoadContext<CurrentSettings>(new CurrentSettings(pair.getFirst().getLevelInfo(), pair.getFirst().getGeneratorOptions(), pair.getSecond().dimensions()), context.dimensionsRegistryManager());
        }, (resourceManager, dataPackContents, combinedRegistryManager, currentSettings) -> {
            resourceManager.close();
            return Pair.of(currentSettings.levelInfo, new GeneratorOptionsHolder(currentSettings.options, new DimensionOptionsRegistryHolder(currentSettings.existingDimensionRegistry), combinedRegistryManager, dataPackContents, currentSettings.levelInfo.getDataConfiguration()));
        });
    }

    private SaveLoading.DataPacks createDataPackConfig(LevelStorage.Session session, boolean safeMode, ResourcePackManager dataPackManager) {
        DataConfiguration lv = session.getDataPackSettings();
        if (lv == null) {
            throw new IllegalStateException("Failed to load data pack config");
        }
        return new SaveLoading.DataPacks(dataPackManager, lv, safeMode, false);
    }

    public SaveLoader createSaveLoader(LevelStorage.Session session, boolean safeMode) throws Exception {
        ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
        return this.createSaveLoader(session, safeMode, lv);
    }

    private <D, R> R load(SaveLoading.DataPacks dataPacks, SaveLoading.LoadContextSupplier<D> loadContextSupplier, SaveLoading.SaveApplierFactory<D, R> saveApplierFactory) throws Exception {
        SaveLoading.ServerConfig lv = new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.INTEGRATED, 2);
        CompletableFuture<R> completableFuture = SaveLoading.load(lv, loadContextSupplier, saveApplierFactory, Util.getMainWorkerExecutor(), this.client);
        this.client.runTasks(completableFuture::isDone);
        return completableFuture.get();
    }

    private void start(Screen parent, String levelName, boolean safeMode, boolean canShowBackupPrompt) {
        boolean bl4;
        SaveLoader lv3;
        LevelStorage.Session lv = this.createSession(levelName);
        if (lv == null) {
            return;
        }
        ResourcePackManager lv2 = VanillaDataPackProvider.createManager(lv);
        try {
            lv3 = this.createSaveLoader(lv, safeMode, lv2);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", exception);
            if (!safeMode) {
                this.client.setScreen(new DatapackFailureScreen(() -> this.start(parent, levelName, true, canShowBackupPrompt)));
            } else {
                this.client.setScreen(new NoticeScreen(() -> this.client.setScreen(null), Text.translatable("datapackFailure.safeMode.failed.title"), Text.translatable("datapackFailure.safeMode.failed.description"), ScreenTexts.TO_TITLE, true));
            }
            IntegratedServerLoader.close(lv, levelName);
            return;
        }
        SaveProperties lv4 = lv3.saveProperties();
        boolean bl3 = lv4.getGeneratorOptions().isLegacyCustomizedType();
        boolean bl = bl4 = lv4.getLifecycle() != Lifecycle.stable();
        if (canShowBackupPrompt && (bl3 || bl4)) {
            this.showBackupPromptScreen(parent, levelName, bl3, () -> this.start(parent, levelName, safeMode, false));
            lv3.close();
            IntegratedServerLoader.close(lv, levelName);
            return;
        }
        ((CompletableFuture)((CompletableFuture)((CompletableFuture)this.client.getServerResourcePackProvider().loadServerPack(lv).thenApply(void_ -> true)).exceptionallyComposeAsync(throwable -> {
            LOGGER.warn("Failed to load pack: ", (Throwable)throwable);
            return this.showPackLoadFailureScreen();
        }, (Executor)this.client)).thenAcceptAsync(proceed -> {
            if (proceed.booleanValue()) {
                this.client.startIntegratedServer(levelName, lv, lv2, lv3, false);
            } else {
                lv3.close();
                IntegratedServerLoader.close(lv, levelName);
                this.client.getServerResourcePackProvider().clear().thenRunAsync(() -> this.client.setScreen(parent), this.client);
            }
        }, (Executor)this.client)).exceptionally(throwable -> {
            this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Load world"));
            return null;
        });
    }

    private CompletableFuture<Boolean> showPackLoadFailureScreen() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<Boolean>();
        this.client.setScreen(new ConfirmScreen(completableFuture::complete, Text.translatable("multiplayer.texturePrompt.failure.line1"), Text.translatable("multiplayer.texturePrompt.failure.line2"), ScreenTexts.PROCEED, ScreenTexts.CANCEL));
        return completableFuture;
    }

    private static void close(LevelStorage.Session session, String levelName) {
        try {
            session.close();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to unlock access to level {}", (Object)levelName, (Object)iOException);
        }
    }

    private void showBackupPromptScreen(Screen parent, String levelName, boolean customized, Runnable callback) {
        MutableText lv2;
        MutableText lv;
        if (customized) {
            lv = Text.translatable("selectWorld.backupQuestion.customized");
            lv2 = Text.translatable("selectWorld.backupWarning.customized");
        } else {
            lv = Text.translatable("selectWorld.backupQuestion.experimental");
            lv2 = Text.translatable("selectWorld.backupWarning.experimental");
        }
        this.client.setScreen(new BackupPromptScreen(parent, (backup, eraseCache) -> {
            if (backup) {
                EditWorldScreen.onBackupConfirm(this.storage, levelName);
            }
            callback.run();
        }, lv, lv2, false));
    }

    public static void tryLoad(MinecraftClient client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean bypassWarnings) {
        BooleanConsumer booleanConsumer = confirmed -> {
            if (confirmed) {
                loader.run();
            } else {
                client.setScreen(parent);
            }
        };
        if (bypassWarnings || lifecycle == Lifecycle.stable()) {
            loader.run();
        } else if (lifecycle == Lifecycle.experimental()) {
            client.setScreen(new ConfirmScreen(booleanConsumer, Text.translatable("selectWorld.warning.experimental.title"), Text.translatable("selectWorld.warning.experimental.question")));
        } else {
            client.setScreen(new ConfirmScreen(booleanConsumer, Text.translatable("selectWorld.warning.deprecated.title"), Text.translatable("selectWorld.warning.deprecated.question")));
        }
    }
}

