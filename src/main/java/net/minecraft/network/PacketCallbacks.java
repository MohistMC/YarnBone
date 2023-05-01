/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network;

import java.util.function.Supplier;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public interface PacketCallbacks {
    public static PacketCallbacks always(final Runnable runnable) {
        return new PacketCallbacks(){

            @Override
            public void onSuccess() {
                runnable.run();
            }

            @Override
            @Nullable
            public Packet<?> getFailurePacket() {
                runnable.run();
                return null;
            }
        };
    }

    public static PacketCallbacks of(final Supplier<Packet<?>> failurePacket) {
        return new PacketCallbacks(){

            @Override
            @Nullable
            public Packet<?> getFailurePacket() {
                return (Packet)failurePacket.get();
            }
        };
    }

    default public void onSuccess() {
    }

    @Nullable
    default public Packet<?> getFailurePacket() {
        return null;
    }
}

