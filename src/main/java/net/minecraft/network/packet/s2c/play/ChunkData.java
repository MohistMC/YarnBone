/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class ChunkData {
    private static final int MAX_SECTIONS_DATA_SIZE = 0x200000;
    private final NbtCompound heightmap;
    private final byte[] sectionsData;
    private final List<BlockEntityData> blockEntities;

    public ChunkData(WorldChunk chunk) {
        this.heightmap = new NbtCompound();
        for (Map.Entry<Heightmap.Type, Heightmap> entry : chunk.getHeightmaps()) {
            if (!entry.getKey().shouldSendToClient()) continue;
            this.heightmap.put(entry.getKey().getName(), new NbtLongArray(entry.getValue().asLongArray()));
        }
        this.sectionsData = new byte[ChunkData.getSectionsPacketSize(chunk)];
        ChunkData.writeSections(new PacketByteBuf(this.getWritableSectionsDataBuf()), chunk);
        this.blockEntities = Lists.newArrayList();
        for (Map.Entry<Object, Object> entry : chunk.getBlockEntities().entrySet()) {
            this.blockEntities.add(BlockEntityData.of((BlockEntity)entry.getValue()));
        }
    }

    public ChunkData(PacketByteBuf buf, int x, int z) {
        this.heightmap = buf.readNbt();
        if (this.heightmap == null) {
            throw new RuntimeException("Can't read heightmap in packet for [" + x + ", " + z + "]");
        }
        int k = buf.readVarInt();
        if (k > 0x200000) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.sectionsData = new byte[k];
        buf.readBytes(this.sectionsData);
        this.blockEntities = buf.readList(BlockEntityData::new);
    }

    public void write(PacketByteBuf buf) {
        buf.writeNbt(this.heightmap);
        buf.writeVarInt(this.sectionsData.length);
        buf.writeBytes(this.sectionsData);
        buf.writeCollection(this.blockEntities, (buf2, entry) -> entry.write((PacketByteBuf)buf2));
    }

    private static int getSectionsPacketSize(WorldChunk chunk) {
        int i = 0;
        for (ChunkSection lv : chunk.getSectionArray()) {
            i += lv.getPacketSize();
        }
        return i;
    }

    private ByteBuf getWritableSectionsDataBuf() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(this.sectionsData);
        byteBuf.writerIndex(0);
        return byteBuf;
    }

    public static void writeSections(PacketByteBuf buf, WorldChunk chunk) {
        for (ChunkSection lv : chunk.getSectionArray()) {
            lv.toPacket(buf);
        }
    }

    public Consumer<BlockEntityVisitor> getBlockEntities(int x, int z) {
        return visitor -> this.iterateBlockEntities((BlockEntityVisitor)visitor, x, z);
    }

    private void iterateBlockEntities(BlockEntityVisitor consumer, int x, int z) {
        int k = 16 * x;
        int l = 16 * z;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (BlockEntityData lv2 : this.blockEntities) {
            int m = k + ChunkSectionPos.getLocalCoord(lv2.localXz >> 4);
            int n = l + ChunkSectionPos.getLocalCoord(lv2.localXz);
            lv.set(m, lv2.y, n);
            consumer.accept(lv, lv2.type, lv2.nbt);
        }
    }

    public PacketByteBuf getSectionsDataBuf() {
        return new PacketByteBuf(Unpooled.wrappedBuffer(this.sectionsData));
    }

    public NbtCompound getHeightmap() {
        return this.heightmap;
    }

    static class BlockEntityData {
        final int localXz;
        final int y;
        final BlockEntityType<?> type;
        @Nullable
        final NbtCompound nbt;

        private BlockEntityData(int localXz, int y, BlockEntityType<?> type, @Nullable NbtCompound nbt) {
            this.localXz = localXz;
            this.y = y;
            this.type = type;
            this.nbt = nbt;
        }

        private BlockEntityData(PacketByteBuf buf) {
            this.localXz = buf.readByte();
            this.y = buf.readShort();
            this.type = buf.readRegistryValue(Registries.BLOCK_ENTITY_TYPE);
            this.nbt = buf.readNbt();
        }

        void write(PacketByteBuf buf) {
            buf.writeByte(this.localXz);
            buf.writeShort(this.y);
            buf.writeRegistryValue(Registries.BLOCK_ENTITY_TYPE, this.type);
            buf.writeNbt(this.nbt);
        }

        static BlockEntityData of(BlockEntity blockEntity) {
            NbtCompound lv = blockEntity.toInitialChunkDataNbt();
            BlockPos lv2 = blockEntity.getPos();
            int i = ChunkSectionPos.getLocalCoord(lv2.getX()) << 4 | ChunkSectionPos.getLocalCoord(lv2.getZ());
            return new BlockEntityData(i, lv2.getY(), blockEntity.getType(), lv.isEmpty() ? null : lv);
        }
    }

    @FunctionalInterface
    public static interface BlockEntityVisitor {
        public void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable NbtCompound var3);
    }
}

