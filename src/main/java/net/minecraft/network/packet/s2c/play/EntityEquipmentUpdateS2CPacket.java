/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class EntityEquipmentUpdateS2CPacket
implements Packet<ClientPlayPacketListener> {
    private static final byte field_33342 = -128;
    private final int id;
    private final List<Pair<EquipmentSlot, ItemStack>> equipmentList;

    public EntityEquipmentUpdateS2CPacket(int id, List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
        this.id = id;
        this.equipmentList = equipmentList;
    }

    public EntityEquipmentUpdateS2CPacket(PacketByteBuf buf) {
        byte i;
        this.id = buf.readVarInt();
        EquipmentSlot[] lvs = EquipmentSlot.values();
        this.equipmentList = Lists.newArrayList();
        do {
            i = buf.readByte();
            EquipmentSlot lv = lvs[i & 0x7F];
            ItemStack lv2 = buf.readItemStack();
            this.equipmentList.add(Pair.of(lv, lv2));
        } while ((i & 0xFFFFFF80) != 0);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.id);
        int i = this.equipmentList.size();
        for (int j = 0; j < i; ++j) {
            Pair<EquipmentSlot, ItemStack> pair = this.equipmentList.get(j);
            EquipmentSlot lv = pair.getFirst();
            boolean bl = j != i - 1;
            int k = lv.ordinal();
            buf.writeByte(bl ? k | 0xFFFFFF80 : k);
            buf.writeItemStack(pair.getSecond());
        }
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onEntityEquipmentUpdate(this);
    }

    public int getId() {
        return this.id;
    }

    public List<Pair<EquipmentSlot, ItemStack>> getEquipmentList() {
        return this.equipmentList;
    }
}

