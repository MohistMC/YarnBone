/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SelectAdvancementTabS2CPacket
implements Packet<ClientPlayPacketListener> {
    @Nullable
    private final Identifier tabId;

    public SelectAdvancementTabS2CPacket(@Nullable Identifier tabId) {
        this.tabId = tabId;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onSelectAdvancementTab(this);
    }

    public SelectAdvancementTabS2CPacket(PacketByteBuf buf) {
        this.tabId = (Identifier)buf.readNullable(PacketByteBuf::readIdentifier);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeNullable(this.tabId, PacketByteBuf::writeIdentifier);
    }

    @Nullable
    public Identifier getTabId() {
        return this.tabId;
    }
}

