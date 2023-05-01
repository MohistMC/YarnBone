/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;

public class SaveLoading {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <D, R> CompletableFuture<R> load(ServerConfig serverConfig, LoadContextSupplier<D> loadContextSupplier, SaveApplierFactory<D, R> saveApplierFactory, Executor prepareExecutor, Executor applyExecutor) {
        try {
            Pair<DataConfiguration, LifecycledResourceManager> pair = serverConfig.dataPacks.load();
            LifecycledResourceManager lv = pair.getSecond();
            CombinedDynamicRegistries<ServerDynamicRegistryType> lv2 = ServerDynamicRegistryType.createCombinedDynamicRegistries();
            CombinedDynamicRegistries<ServerDynamicRegistryType> lv3 = SaveLoading.withRegistriesLoaded(lv, lv2, ServerDynamicRegistryType.WORLDGEN, RegistryLoader.DYNAMIC_REGISTRIES);
            DynamicRegistryManager.Immutable lv4 = lv3.getPrecedingRegistryManagers(ServerDynamicRegistryType.DIMENSIONS);
            DynamicRegistryManager.Immutable lv5 = RegistryLoader.load(lv, lv4, RegistryLoader.DIMENSION_REGISTRIES);
            DataConfiguration lv6 = pair.getFirst();
            LoadContext<D> lv7 = loadContextSupplier.get(new LoadContextSupplierContext(lv, lv6, lv4, lv5));
            CombinedDynamicRegistries<ServerDynamicRegistryType> lv8 = lv3.with(ServerDynamicRegistryType.DIMENSIONS, lv7.dimensionsRegistryManager);
            DynamicRegistryManager.Immutable lv9 = lv8.getPrecedingRegistryManagers(ServerDynamicRegistryType.RELOADABLE);
            return ((CompletableFuture)DataPackContents.reload(lv, lv9, lv6.enabledFeatures(), serverConfig.commandEnvironment(), serverConfig.functionPermissionLevel(), prepareExecutor, applyExecutor).whenComplete((dataPackContents, throwable) -> {
                if (throwable != null) {
                    lv.close();
                }
            })).thenApplyAsync(dataPackContents -> {
                dataPackContents.refresh(lv9);
                return saveApplierFactory.create(lv, (DataPackContents)dataPackContents, lv8, arg5.extraData);
            }, applyExecutor);
        }
        catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private static DynamicRegistryManager.Immutable loadDynamicRegistryManager(ResourceManager resourceManager, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, ServerDynamicRegistryType type, List<RegistryLoader.Entry<?>> entries) {
        DynamicRegistryManager.Immutable lv = combinedDynamicRegistries.getPrecedingRegistryManagers(type);
        return RegistryLoader.load(resourceManager, lv, entries);
    }

    private static CombinedDynamicRegistries<ServerDynamicRegistryType> withRegistriesLoaded(ResourceManager resourceManager, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, ServerDynamicRegistryType type, List<RegistryLoader.Entry<?>> entries) {
        DynamicRegistryManager.Immutable lv = SaveLoading.loadDynamicRegistryManager(resourceManager, combinedDynamicRegistries, type, entries);
        return combinedDynamicRegistries.with(type, lv);
    }

    public record ServerConfig(DataPacks dataPacks, CommandManager.RegistrationEnvironment commandEnvironment, int functionPermissionLevel) {
    }

    public record DataPacks(ResourcePackManager manager, DataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
        public Pair<DataConfiguration, LifecycledResourceManager> load() {
            FeatureSet lv = this.initMode ? FeatureFlags.FEATURE_MANAGER.getFeatureSet() : this.initialDataConfig.enabledFeatures();
            DataConfiguration lv2 = MinecraftServer.loadDataPacks(this.manager, this.initialDataConfig.dataPacks(), this.safeMode, lv);
            if (!this.initMode) {
                lv2 = lv2.withFeaturesAdded(this.initialDataConfig.enabledFeatures());
            }
            List<ResourcePack> list = this.manager.createResourcePacks();
            LifecycledResourceManagerImpl lv3 = new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, list);
            return Pair.of(lv2, lv3);
        }
    }

    public record LoadContextSupplierContext(ResourceManager resourceManager, DataConfiguration dataConfiguration, DynamicRegistryManager.Immutable worldGenRegistryManager, DynamicRegistryManager.Immutable dimensionsRegistryManager) {
    }

    @FunctionalInterface
    public static interface LoadContextSupplier<D> {
        public LoadContext<D> get(LoadContextSupplierContext var1);
    }

    public record LoadContext<D>(D extraData, DynamicRegistryManager.Immutable dimensionsRegistryManager) {
    }

    @FunctionalInterface
    public static interface SaveApplierFactory<D, R> {
        public R create(LifecycledResourceManager var1, DataPackContents var2, CombinedDynamicRegistries<ServerDynamicRegistryType> var3, D var4);
    }
}

