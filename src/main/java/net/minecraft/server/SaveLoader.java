/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.DataPackContents;
import net.minecraft.world.SaveProperties;

public record SaveLoader(LifecycledResourceManager resourceManager, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, SaveProperties saveProperties) implements AutoCloseable
{
    @Override
    public void close() {
        this.resourceManager.close();
    }
}

