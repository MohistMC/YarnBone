/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.metadata.ResourceMetadata;
import org.jetbrains.annotations.Nullable;

public class Resource {
    private final ResourcePack pack;
    private final InputSupplier<InputStream> inputSupplier;
    private final InputSupplier<ResourceMetadata> metadataSupplier;
    @Nullable
    private ResourceMetadata metadata;

    public Resource(ResourcePack pack, InputSupplier<InputStream> inputSupplier, InputSupplier<ResourceMetadata> metadataSupplier) {
        this.pack = pack;
        this.inputSupplier = inputSupplier;
        this.metadataSupplier = metadataSupplier;
    }

    public Resource(ResourcePack pack, InputSupplier<InputStream> inputSupplier) {
        this.pack = pack;
        this.inputSupplier = inputSupplier;
        this.metadataSupplier = ResourceMetadata.NONE_SUPPLIER;
        this.metadata = ResourceMetadata.NONE;
    }

    public ResourcePack getPack() {
        return this.pack;
    }

    public String getResourcePackName() {
        return this.pack.getName();
    }

    public boolean isAlwaysStable() {
        return this.pack.isAlwaysStable();
    }

    public InputStream getInputStream() throws IOException {
        return this.inputSupplier.get();
    }

    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }

    public ResourceMetadata getMetadata() throws IOException {
        if (this.metadata == null) {
            this.metadata = this.metadataSupplier.get();
        }
        return this.metadata;
    }
}

