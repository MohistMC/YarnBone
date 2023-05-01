/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry.tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class TagManagerLoader
implements ResourceReloader {
    private static final Map<RegistryKey<? extends Registry<?>>, String> DIRECTORIES = Map.of(RegistryKeys.BLOCK, "tags/blocks", RegistryKeys.ENTITY_TYPE, "tags/entity_types", RegistryKeys.FLUID, "tags/fluids", RegistryKeys.GAME_EVENT, "tags/game_events", RegistryKeys.ITEM, "tags/items");
    private final DynamicRegistryManager registryManager;
    private List<RegistryTags<?>> registryTags = List.of();

    public TagManagerLoader(DynamicRegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    public List<RegistryTags<?>> getRegistryTags() {
        return this.registryTags;
    }

    public static String getPath(RegistryKey<? extends Registry<?>> registry) {
        String string = DIRECTORIES.get(registry);
        if (string != null) {
            return string;
        }
        return "tags/" + registry.getValue().getPath();
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        List<CompletableFuture> list = this.registryManager.streamAllRegistries().map(registry -> this.buildRequiredGroup(manager, prepareExecutor, (DynamicRegistryManager.Entry)registry)).toList();
        return ((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new)).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(void_ -> {
            this.registryTags = list.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList());
        }, applyExecutor);
    }

    private <T> CompletableFuture<RegistryTags<T>> buildRequiredGroup(ResourceManager resourceManager, Executor prepareExecutor, DynamicRegistryManager.Entry<T> requirement) {
        RegistryKey lv = requirement.key();
        Registry lv2 = requirement.value();
        TagGroupLoader lv3 = new TagGroupLoader(id -> lv2.getEntry(RegistryKey.of(lv, id)), TagManagerLoader.getPath(lv));
        return CompletableFuture.supplyAsync(() -> new RegistryTags(lv, lv3.load(resourceManager)), prepareExecutor);
    }

    public record RegistryTags<T>(RegistryKey<? extends Registry<T>> key, Map<Identifier, Collection<RegistryEntry<T>>> tags) {
    }
}

