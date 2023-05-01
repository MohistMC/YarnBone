/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource;

import net.minecraft.resource.ResourceManager;

public interface LifecycledResourceManager
extends ResourceManager,
AutoCloseable {
    @Override
    public void close();
}

