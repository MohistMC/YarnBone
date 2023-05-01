/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource;

public enum ResourceType {
    CLIENT_RESOURCES("assets"),
    SERVER_DATA("data");

    private final String directory;

    private ResourceType(String name) {
        this.directory = name;
    }

    public String getDirectory() {
        return this.directory;
    }
}

