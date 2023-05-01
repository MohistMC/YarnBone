/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.profiling.jfr;

import net.minecraft.server.MinecraftServer;

public enum InstanceType {
    CLIENT("client"),
    SERVER("server");

    private final String name;

    private InstanceType(String name) {
        this.name = name;
    }

    public static InstanceType get(MinecraftServer server) {
        return server.isDedicated() ? SERVER : CLIENT;
    }

    public String getName() {
        return this.name;
    }
}

