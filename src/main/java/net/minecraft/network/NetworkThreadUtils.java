/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ThreadExecutor;
import org.slf4j.Logger;

public class NetworkThreadUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ServerWorld world) throws OffThreadException {
        NetworkThreadUtils.forceMainThread(packet, listener, world.getServer());
    }

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ThreadExecutor<?> engine) throws OffThreadException {
        if (!engine.isOnThread()) {
            engine.executeSync(() -> {
                if (listener.isConnectionOpen()) {
                    try {
                        packet.apply(listener);
                    }
                    catch (Exception exception) {
                        if (listener.shouldCrashOnException()) {
                            throw exception;
                        }
                        LOGGER.error("Failed to handle packet {}, suppressing error", (Object)packet, (Object)exception);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)packet);
                }
            });
            throw OffThreadException.INSTANCE;
        }
    }
}

