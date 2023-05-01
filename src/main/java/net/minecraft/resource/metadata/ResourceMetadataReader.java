/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource.metadata;

import com.google.gson.JsonObject;

public interface ResourceMetadataReader<T> {
    public String getKey();

    public T fromJson(JsonObject var1);
}

