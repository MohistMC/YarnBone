/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.event.listener;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;

public interface GameEventListener {
    public PositionSource getPositionSource();

    public int getRange();

    public boolean listen(ServerWorld var1, GameEvent var2, GameEvent.Emitter var3, Vec3d var4);

    default public TriggerOrder getTriggerOrder() {
        return TriggerOrder.UNSPECIFIED;
    }

    public static enum TriggerOrder {
        UNSPECIFIED,
        BY_DISTANCE;

    }
}

