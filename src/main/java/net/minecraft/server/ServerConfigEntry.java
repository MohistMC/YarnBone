/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public abstract class ServerConfigEntry<T> {
    @Nullable
    private final T key;

    public ServerConfigEntry(@Nullable T key) {
        this.key = key;
    }

    @Nullable
    T getKey() {
        return this.key;
    }

    boolean isInvalid() {
        return false;
    }

    protected abstract void write(JsonObject var1);
}

