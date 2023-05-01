/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.event.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;

public class GameEventDispatchManager {
    private final ServerWorld world;

    public GameEventDispatchManager(ServerWorld world) {
        this.world = world;
    }

    public void dispatch(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {
        int i = event.getRange();
        BlockPos lv = BlockPos.ofFloored(emitterPos);
        int j = ChunkSectionPos.getSectionCoord(lv.getX() - i);
        int k = ChunkSectionPos.getSectionCoord(lv.getY() - i);
        int l = ChunkSectionPos.getSectionCoord(lv.getZ() - i);
        int m = ChunkSectionPos.getSectionCoord(lv.getX() + i);
        int n = ChunkSectionPos.getSectionCoord(lv.getY() + i);
        int o = ChunkSectionPos.getSectionCoord(lv.getZ() + i);
        ArrayList<GameEvent.Message> list = new ArrayList<GameEvent.Message>();
        GameEventDispatcher.DispatchCallback lv2 = (listener, listenerPos) -> {
            if (listener.getTriggerOrder() == GameEventListener.TriggerOrder.BY_DISTANCE) {
                list.add(new GameEvent.Message(event, emitterPos, emitter, listener, listenerPos));
            } else {
                listener.listen(this.world, event, emitter, emitterPos);
            }
        };
        boolean bl = false;
        for (int p = j; p <= m; ++p) {
            for (int q = l; q <= o; ++q) {
                WorldChunk lv3 = this.world.getChunkManager().getWorldChunk(p, q);
                if (lv3 == null) continue;
                for (int r = k; r <= n; ++r) {
                    bl |= ((Chunk)lv3).getGameEventDispatcher(r).dispatch(event, emitterPos, emitter, lv2);
                }
            }
        }
        if (!list.isEmpty()) {
            this.dispatchListenersByDistance(list);
        }
        if (bl) {
            DebugInfoSender.sendGameEvent(this.world, event, emitterPos);
        }
    }

    private void dispatchListenersByDistance(List<GameEvent.Message> messages) {
        Collections.sort(messages);
        for (GameEvent.Message lv : messages) {
            GameEventListener lv2 = lv.getListener();
            lv2.listen(this.world, lv.getEvent(), lv.getEmitter(), lv.getEmitterPos());
        }
    }
}

