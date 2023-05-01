/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.function.LootFunctionManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.slf4j.Logger;

public class DataPackContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> COMPLETED_UNIT = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final CommandRegistryAccess.EntryListCreationPolicySettable commandRegistryAccess;
    private final CommandManager commandManager;
    private final RecipeManager recipeManager = new RecipeManager();
    private final TagManagerLoader registryTagManager;
    private final LootConditionManager lootConditionManager = new LootConditionManager();
    private final LootManager lootManager = new LootManager(this.lootConditionManager);
    private final LootFunctionManager lootFunctionManager = new LootFunctionManager(this.lootConditionManager, this.lootManager);
    private final ServerAdvancementLoader serverAdvancementLoader = new ServerAdvancementLoader(this.lootConditionManager);
    private final FunctionLoader functionLoader;

    public DataPackContents(DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel) {
        this.registryTagManager = new TagManagerLoader(dynamicRegistryManager);
        this.commandRegistryAccess = CommandRegistryAccess.of(dynamicRegistryManager, enabledFeatures);
        this.commandManager = new CommandManager(environment, this.commandRegistryAccess);
        this.commandRegistryAccess.setEntryListCreationPolicy(CommandRegistryAccess.EntryListCreationPolicy.CREATE_NEW);
        this.functionLoader = new FunctionLoader(functionPermissionLevel, this.commandManager.getDispatcher());
    }

    public FunctionLoader getFunctionLoader() {
        return this.functionLoader;
    }

    public LootConditionManager getLootConditionManager() {
        return this.lootConditionManager;
    }

    public LootManager getLootManager() {
        return this.lootManager;
    }

    public LootFunctionManager getLootFunctionManager() {
        return this.lootFunctionManager;
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public ServerAdvancementLoader getServerAdvancementLoader() {
        return this.serverAdvancementLoader;
    }

    public List<ResourceReloader> getContents() {
        return List.of(this.registryTagManager, this.lootConditionManager, this.recipeManager, this.lootManager, this.lootFunctionManager, this.functionLoader, this.serverAdvancementLoader);
    }

    public static CompletableFuture<DataPackContents> reload(ResourceManager manager, DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor) {
        DataPackContents lv = new DataPackContents(dynamicRegistryManager, enabledFeatures, environment, functionPermissionLevel);
        return ((CompletableFuture)SimpleResourceReload.start(manager, lv.getContents(), prepareExecutor, applyExecutor, COMPLETED_UNIT, LOGGER.isDebugEnabled()).whenComplete().whenComplete((void_, throwable) -> arg.commandRegistryAccess.setEntryListCreationPolicy(CommandRegistryAccess.EntryListCreationPolicy.FAIL))).thenApply(void_ -> lv);
    }

    public void refresh(DynamicRegistryManager dynamicRegistryManager) {
        this.registryTagManager.getRegistryTags().forEach(tags -> DataPackContents.repopulateTags(dynamicRegistryManager, tags));
        Blocks.refreshShapeCache();
    }

    private static <T> void repopulateTags(DynamicRegistryManager dynamicRegistryManager, TagManagerLoader.RegistryTags<T> tags) {
        RegistryKey lv = tags.key();
        Map map = tags.tags().entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> TagKey.of(lv, (Identifier)entry.getKey()), entry -> List.copyOf((Collection)entry.getValue())));
        dynamicRegistryManager.get(lv).populateTags(map);
    }
}

