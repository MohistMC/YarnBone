/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldAccess;

public interface ServerWorldAccess
extends WorldAccess {
    public ServerWorld toServerWorld();

    default public void spawnEntityAndPassengers(Entity entity) {
        entity.streamSelfAndPassengers().forEach(this::spawnEntity);
    }
}

