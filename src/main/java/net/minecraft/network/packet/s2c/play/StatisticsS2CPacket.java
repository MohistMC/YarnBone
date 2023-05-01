/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;

public class StatisticsS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final Object2IntMap<Stat<?>> stats;

    public StatisticsS2CPacket(Object2IntMap<Stat<?>> stats) {
        this.stats = stats;
    }

    public StatisticsS2CPacket(PacketByteBuf buf2) {
        this.stats = buf2.readMap(Object2IntOpenHashMap::new, buf -> {
            StatType<?> lv = buf.readRegistryValue(Registries.STAT_TYPE);
            return StatisticsS2CPacket.getOrCreateStat(buf2, lv);
        }, PacketByteBuf::readVarInt);
    }

    private static <T> Stat<T> getOrCreateStat(PacketByteBuf buf, StatType<T> statType) {
        return statType.getOrCreateStat(buf.readRegistryValue(statType.getRegistry()));
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onStatistics(this);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeMap(this.stats, StatisticsS2CPacket::write, PacketByteBuf::writeVarInt);
    }

    private static <T> void write(PacketByteBuf buf, Stat<T> stat) {
        buf.writeRegistryValue(Registries.STAT_TYPE, stat.getType());
        buf.writeRegistryValue(stat.getType().getRegistry(), stat.getValue());
    }

    public Map<Stat<?>, Integer> getStatMap() {
        return this.stats;
    }
}

