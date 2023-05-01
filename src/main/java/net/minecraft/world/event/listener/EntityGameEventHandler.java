/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.event.listener;

import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class EntityGameEventHandler<T extends GameEventListener> {
    private T listener;
    @Nullable
    private ChunkSectionPos sectionPos;

    public EntityGameEventHandler(T listener) {
        this.listener = listener;
    }

    public void onEntitySetPosCallback(ServerWorld world) {
        this.onEntitySetPos(world);
    }

    public void setListener(T listener, @Nullable World world) {
        Object lv = this.listener;
        if (lv == listener) {
            return;
        }
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            EntityGameEventHandler.updateDispatcher(lv2, this.sectionPos, dispatcher -> dispatcher.removeListener((GameEventListener)lv));
            EntityGameEventHandler.updateDispatcher(lv2, this.sectionPos, dispatcher -> dispatcher.addListener((GameEventListener)listener));
        }
        this.listener = listener;
    }

    public T getListener() {
        return this.listener;
    }

    public void onEntityRemoval(ServerWorld world) {
        EntityGameEventHandler.updateDispatcher(world, this.sectionPos, dispatcher -> dispatcher.removeListener((GameEventListener)this.listener));
    }

    public void onEntitySetPos(ServerWorld world) {
        this.listener.getPositionSource().getPos(world).map(ChunkSectionPos::from).ifPresent(sectionPos -> {
            if (this.sectionPos == null || !this.sectionPos.equals(sectionPos)) {
                EntityGameEventHandler.updateDispatcher(world, this.sectionPos, dispatcher -> dispatcher.removeListener((GameEventListener)this.listener));
                this.sectionPos = sectionPos;
                EntityGameEventHandler.updateDispatcher(world, this.sectionPos, dispatcher -> dispatcher.addListener((GameEventListener)this.listener));
            }
        });
    }

    private static void updateDispatcher(WorldView world, @Nullable ChunkSectionPos sectionPos, Consumer<GameEventDispatcher> dispatcherConsumer) {
        if (sectionPos == null) {
            return;
        }
        Chunk lv = world.getChunk(sectionPos.getSectionX(), sectionPos.getSectionZ(), ChunkStatus.FULL, false);
        if (lv != null) {
            dispatcherConsumer.accept(lv.getGameEventDispatcher(sectionPos.getSectionY()));
        }
    }
}

