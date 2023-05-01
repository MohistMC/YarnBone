/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.entity;

import net.minecraft.server.world.ChunkHolder;

public enum EntityTrackingStatus {
    HIDDEN(false, false),
    TRACKED(true, false),
    TICKING(true, true);

    private final boolean tracked;
    private final boolean tick;

    private EntityTrackingStatus(boolean tracked, boolean tick) {
        this.tracked = tracked;
        this.tick = tick;
    }

    public boolean shouldTick() {
        return this.tick;
    }

    public boolean shouldTrack() {
        return this.tracked;
    }

    public static EntityTrackingStatus fromLevelType(ChunkHolder.LevelType levelType) {
        if (levelType.isAfter(ChunkHolder.LevelType.ENTITY_TICKING)) {
            return TICKING;
        }
        if (levelType.isAfter(ChunkHolder.LevelType.BORDER)) {
            return TRACKED;
        }
        return HIDDEN;
    }
}

