/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource;

import java.util.function.Consumer;
import net.minecraft.resource.ResourcePackProfile;

public interface ResourcePackProvider {
    public void register(Consumer<ResourcePackProfile> var1);
}

