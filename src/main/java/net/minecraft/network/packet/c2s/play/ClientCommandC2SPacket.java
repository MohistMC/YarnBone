/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class ClientCommandC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final int entityId;
    private final Mode mode;
    private final int mountJumpHeight;

    public ClientCommandC2SPacket(Entity entity, Mode mode) {
        this(entity, mode, 0);
    }

    public ClientCommandC2SPacket(Entity entity, Mode mode, int mountJumpHeight) {
        this.entityId = entity.getId();
        this.mode = mode;
        this.mountJumpHeight = mountJumpHeight;
    }

    public ClientCommandC2SPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.mode = buf.readEnumConstant(Mode.class);
        this.mountJumpHeight = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeEnumConstant(this.mode);
        buf.writeVarInt(this.mountJumpHeight);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onClientCommand(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Mode getMode() {
        return this.mode;
    }

    public int getMountJumpHeight() {
        return this.mountJumpHeight;
    }

    public static enum Mode {
        PRESS_SHIFT_KEY,
        RELEASE_SHIFT_KEY,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        START_RIDING_JUMP,
        STOP_RIDING_JUMP,
        OPEN_INVENTORY,
        START_FALL_FLYING;

    }
}

