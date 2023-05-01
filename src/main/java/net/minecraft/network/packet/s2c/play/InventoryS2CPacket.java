/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.collection.DefaultedList;

public class InventoryS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int syncId;
    private final int revision;
    private final List<ItemStack> contents;
    private final ItemStack cursorStack;

    public InventoryS2CPacket(int syncId, int revision, DefaultedList<ItemStack> contents, ItemStack cursorStack) {
        this.syncId = syncId;
        this.revision = revision;
        this.contents = DefaultedList.ofSize(contents.size(), ItemStack.EMPTY);
        for (int k = 0; k < contents.size(); ++k) {
            this.contents.set(k, contents.get(k).copy());
        }
        this.cursorStack = cursorStack.copy();
    }

    public InventoryS2CPacket(PacketByteBuf buf) {
        this.syncId = buf.readUnsignedByte();
        this.revision = buf.readVarInt();
        this.contents = buf.readCollection(DefaultedList::ofSize, PacketByteBuf::readItemStack);
        this.cursorStack = buf.readItemStack();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(this.syncId);
        buf.writeVarInt(this.revision);
        buf.writeCollection(this.contents, PacketByteBuf::writeItemStack);
        buf.writeItemStack(this.cursorStack);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onInventory(this);
    }

    public int getSyncId() {
        return this.syncId;
    }

    public List<ItemStack> getContents() {
        return this.contents;
    }

    public ItemStack getCursorStack() {
        return this.cursorStack;
    }

    public int getRevision() {
        return this.revision;
    }
}

