/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface ResourcePack
extends AutoCloseable {
    public static final String METADATA_PATH_SUFFIX = ".mcmeta";
    public static final String PACK_METADATA_NAME = "pack.mcmeta";

    @Nullable
    public InputSupplier<InputStream> openRoot(String ... var1);

    @Nullable
    public InputSupplier<InputStream> open(ResourceType var1, Identifier var2);

    public void findResources(ResourceType var1, String var2, String var3, ResultConsumer var4);

    public Set<String> getNamespaces(ResourceType var1);

    @Nullable
    public <T> T parseMetadata(ResourceMetadataReader<T> var1) throws IOException;

    public String getName();

    default public boolean isAlwaysStable() {
        return false;
    }

    @Override
    public void close();

    @FunctionalInterface
    public static interface ResultConsumer
    extends BiConsumer<Identifier, InputSupplier<InputStream>> {
    }
}

