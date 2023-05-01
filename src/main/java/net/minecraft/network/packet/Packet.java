/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;

public interface Packet<T extends PacketListener> {
    public void write(PacketByteBuf var1);

    public void apply(T var1);

    default public boolean isWritingErrorSkippable() {
        return false;
    }
}

