/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PacketByteBuf
extends ByteBuf {
    private static final int MAX_VAR_INT_LENGTH = 5;
    private static final int MAX_VAR_LONG_LENGTH = 10;
    public static final int MAX_READ_NBT_SIZE = 0x200000;
    private final ByteBuf parent;
    public static final short DEFAULT_MAX_STRING_LENGTH = Short.MAX_VALUE;
    public static final int MAX_TEXT_LENGTH = 262144;
    private static final int field_39381 = 256;
    private static final int field_39382 = 256;
    private static final int field_39383 = 512;
    private static final Gson GSON = new Gson();

    public PacketByteBuf(ByteBuf parent) {
        this.parent = parent;
    }

    public static int getVarIntLength(int value) {
        for (int j = 1; j < MAX_VAR_INT_LENGTH; ++j) {
            if ((value & -1 << j * 7) != 0) continue;
            return j;
        }
        return MAX_VAR_INT_LENGTH;
    }

    public static int getVarLongLength(long value) {
        for (int i = 1; i < MAX_VAR_LONG_LENGTH; ++i) {
            if ((value & -1L << i * 7) != 0L) continue;
            return i;
        }
        return MAX_VAR_LONG_LENGTH;
    }

    @Deprecated
    public <T> T decode(DynamicOps<NbtElement> ops, Codec<T> codec) {
        NbtCompound lv = this.readUnlimitedNbt();
        return (T)Util.getResult(codec.parse(ops, lv), error -> new DecoderException("Failed to decode: " + error + " " + lv));
    }

    @Deprecated
    public <T> void encode(DynamicOps<NbtElement> ops, Codec<T> codec, T value) {
        NbtElement lv = Util.getResult(codec.encodeStart(ops, (NbtElement)value), error -> new EncoderException("Failed to encode: " + error + " " + value));
        this.writeNbt((NbtCompound)lv);
    }

    public <T> T decodeAsJson(Codec<T> codec) {
        JsonElement jsonElement = JsonHelper.deserialize(GSON, this.readString(), JsonElement.class);
        DataResult dataResult = codec.parse(JsonOps.INSTANCE, jsonElement);
        return (T)Util.getResult(dataResult, error -> new DecoderException("Failed to decode json: " + error));
    }

    public <T> void encodeAsJson(Codec<T> codec, T value) {
        DataResult<JsonElement> dataResult = codec.encodeStart(JsonOps.INSTANCE, (JsonElement)value);
        this.writeString(GSON.toJson(Util.getResult(dataResult, error -> new EncoderException("Failed to encode: " + error + " " + value))));
    }

    public <T> void writeRegistryValue(IndexedIterable<T> registry, T value) {
        int i = registry.getRawId(value);
        if (i == -1) {
            throw new IllegalArgumentException("Can't find id for '" + value + "' in map " + registry);
        }
        this.writeVarInt(i);
    }

    public <T> void writeRegistryEntry(IndexedIterable<RegistryEntry<T>> registryEntries, RegistryEntry<T> entry, PacketWriter<T> writer) {
        switch (entry.getType()) {
            case REFERENCE: {
                int i = registryEntries.getRawId(entry);
                if (i == -1) {
                    throw new IllegalArgumentException("Can't find id for '" + entry.value() + "' in map " + registryEntries);
                }
                this.writeVarInt(i + 1);
                break;
            }
            case DIRECT: {
                this.writeVarInt(0);
                writer.accept(this, entry.value());
            }
        }
    }

    @Nullable
    public <T> T readRegistryValue(IndexedIterable<T> registry) {
        int i = this.readVarInt();
        return registry.get(i);
    }

    public <T> RegistryEntry<T> readRegistryEntry(IndexedIterable<RegistryEntry<T>> registryEntries, PacketReader<T> reader) {
        int i = this.readVarInt();
        if (i == 0) {
            return RegistryEntry.of(reader.apply(this));
        }
        RegistryEntry<T> lv = registryEntries.get(i - 1);
        if (lv == null) {
            throw new IllegalArgumentException("Can't find element with id " + i);
        }
        return lv;
    }

    public static <T> IntFunction<T> getMaxValidator(IntFunction<T> applier, int max) {
        return value -> {
            if (value > max) {
                throw new DecoderException("Value " + value + " is larger than limit " + max);
            }
            return applier.apply(value);
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> collectionFactory, PacketReader<T> reader) {
        int i = this.readVarInt();
        Collection collection = (Collection)collectionFactory.apply(i);
        for (int j = 0; j < i; ++j) {
            collection.add(reader.apply(this));
        }
        return (C)collection;
    }

    public <T> void writeCollection(Collection<T> collection, PacketWriter<T> writer) {
        this.writeVarInt(collection.size());
        for (T object : collection) {
            writer.accept(this, object);
        }
    }

    public <T> List<T> readList(PacketReader<T> reader) {
        return this.readCollection(Lists::newArrayListWithCapacity, reader);
    }

    public IntList readIntList() {
        int i = this.readVarInt();
        IntArrayList intList = new IntArrayList();
        for (int j = 0; j < i; ++j) {
            intList.add(this.readVarInt());
        }
        return intList;
    }

    public void writeIntList(IntList list) {
        this.writeVarInt(list.size());
        list.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> mapFactory, PacketReader<K> keyReader, PacketReader<V> valueReader) {
        int i = this.readVarInt();
        Map map = (Map)mapFactory.apply(i);
        for (int j = 0; j < i; ++j) {
            Object object = keyReader.apply(this);
            Object object2 = valueReader.apply(this);
            map.put(object, object2);
        }
        return (M)map;
    }

    public <K, V> Map<K, V> readMap(PacketReader<K> keyReader, PacketReader<V> valueReader) {
        return this.readMap(Maps::newHashMapWithExpectedSize, keyReader, valueReader);
    }

    public <K, V> void writeMap(Map<K, V> map, PacketWriter<K> keyWriter, PacketWriter<V> valueWriter) {
        this.writeVarInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.accept(this, key);
            valueWriter.accept(this, value);
        });
    }

    public void forEachInCollection(Consumer<PacketByteBuf> consumer) {
        int i = this.readVarInt();
        for (int j = 0; j < i; ++j) {
            consumer.accept(this);
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> type) {
        Enum[] enums = (Enum[])type.getEnumConstants();
        BitSet bitSet = new BitSet(enums.length);
        for (int i = 0; i < enums.length; ++i) {
            bitSet.set(i, enumSet.contains(enums[i]));
        }
        this.writeBitSet(bitSet, enums.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> type) {
        Enum[] enums = (Enum[])type.getEnumConstants();
        BitSet bitSet = this.readBitSet(enums.length);
        EnumSet<Enum> enumSet = EnumSet.noneOf(type);
        for (int i = 0; i < enums.length; ++i) {
            if (!bitSet.get(i)) continue;
            enumSet.add(enums[i]);
        }
        return enumSet;
    }

    public <T> void writeOptional(Optional<T> value, PacketWriter<T> writer) {
        if (value.isPresent()) {
            this.writeBoolean(true);
            writer.accept(this, value.get());
        } else {
            this.writeBoolean(false);
        }
    }

    public <T> Optional<T> readOptional(PacketReader<T> reader) {
        if (this.readBoolean()) {
            return Optional.of(reader.apply(this));
        }
        return Optional.empty();
    }

    @Nullable
    public <T> T readNullable(PacketReader<T> reader) {
        if (this.readBoolean()) {
            return (T)reader.apply(this);
        }
        return null;
    }

    public <T> void writeNullable(@Nullable T value, PacketWriter<T> writer) {
        if (value != null) {
            this.writeBoolean(true);
            writer.accept(this, value);
        } else {
            this.writeBoolean(false);
        }
    }

    public <L, R> void writeEither(Either<L, R> either, PacketWriter<L> leftWriter, PacketWriter<R> rightWriter) {
        either.ifLeft(object -> {
            this.writeBoolean(true);
            leftWriter.accept(this, object);
        }).ifRight(object -> {
            this.writeBoolean(false);
            rightWriter.accept(this, object);
        });
    }

    public <L, R> Either<L, R> readEither(PacketReader<L> leftReader, PacketReader<R> rightReader) {
        if (this.readBoolean()) {
            return Either.left(leftReader.apply(this));
        }
        return Either.right(rightReader.apply(this));
    }

    public byte[] readByteArray() {
        return this.readByteArray(this.readableBytes());
    }

    public PacketByteBuf writeByteArray(byte[] array) {
        this.writeVarInt(array.length);
        this.writeBytes(array);
        return this;
    }

    public byte[] readByteArray(int maxSize) {
        int j = this.readVarInt();
        if (j > maxSize) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + maxSize);
        }
        byte[] bs = new byte[j];
        this.readBytes(bs);
        return bs;
    }

    public PacketByteBuf writeIntArray(int[] array) {
        this.writeVarInt(array.length);
        for (int i : array) {
            this.writeVarInt(i);
        }
        return this;
    }

    public int[] readIntArray() {
        return this.readIntArray(this.readableBytes());
    }

    public int[] readIntArray(int maxSize) {
        int j = this.readVarInt();
        if (j > maxSize) {
            throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + maxSize);
        }
        int[] is = new int[j];
        for (int k = 0; k < is.length; ++k) {
            is[k] = this.readVarInt();
        }
        return is;
    }

    public PacketByteBuf writeLongArray(long[] array) {
        this.writeVarInt(array.length);
        for (long l : array) {
            this.writeLong(l);
        }
        return this;
    }

    public long[] readLongArray() {
        return this.readLongArray(null);
    }

    public long[] readLongArray(@Nullable long[] toArray) {
        return this.readLongArray(toArray, this.readableBytes() / 8);
    }

    public long[] readLongArray(@Nullable long[] toArray, int maxSize) {
        int j = this.readVarInt();
        if (toArray == null || toArray.length != j) {
            if (j > maxSize) {
                throw new DecoderException("LongArray with size " + j + " is bigger than allowed " + maxSize);
            }
            toArray = new long[j];
        }
        for (int k = 0; k < toArray.length; ++k) {
            toArray[k] = this.readLong();
        }
        return toArray;
    }

    @VisibleForTesting
    public byte[] getWrittenBytes() {
        int i = this.writerIndex();
        byte[] bs = new byte[i];
        this.getBytes(0, bs);
        return bs;
    }

    public BlockPos readBlockPos() {
        return BlockPos.fromLong(this.readLong());
    }

    public PacketByteBuf writeBlockPos(BlockPos pos) {
        this.writeLong(pos.asLong());
        return this;
    }

    public ChunkPos readChunkPos() {
        return new ChunkPos(this.readLong());
    }

    public PacketByteBuf writeChunkPos(ChunkPos pos) {
        this.writeLong(pos.toLong());
        return this;
    }

    public ChunkSectionPos readChunkSectionPos() {
        return ChunkSectionPos.from(this.readLong());
    }

    public PacketByteBuf writeChunkSectionPos(ChunkSectionPos pos) {
        this.writeLong(pos.asLong());
        return this;
    }

    public GlobalPos readGlobalPos() {
        RegistryKey<World> lv = this.readRegistryKey(RegistryKeys.WORLD);
        BlockPos lv2 = this.readBlockPos();
        return GlobalPos.create(lv, lv2);
    }

    public void writeGlobalPos(GlobalPos pos) {
        this.writeRegistryKey(pos.getDimension());
        this.writeBlockPos(pos.getPos());
    }

    public Vector3f readVector3f() {
        return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
    }

    public void writeVector3f(Vector3f vector3f) {
        this.writeFloat(vector3f.x());
        this.writeFloat(vector3f.y());
        this.writeFloat(vector3f.z());
    }

    public Quaternionf readQuaternionf() {
        return new Quaternionf(this.readFloat(), this.readFloat(), this.readFloat(), this.readFloat());
    }

    public void writeQuaternionf(Quaternionf quaternionf) {
        this.writeFloat(quaternionf.x);
        this.writeFloat(quaternionf.y);
        this.writeFloat(quaternionf.z);
        this.writeFloat(quaternionf.w);
    }

    public Text readText() {
        MutableText lv = Text.Serializer.fromJson(this.readString(MAX_TEXT_LENGTH));
        if (lv == null) {
            throw new DecoderException("Received unexpected null component");
        }
        return lv;
    }

    public PacketByteBuf writeText(Text text) {
        return this.writeString(Text.Serializer.toJson(text), MAX_TEXT_LENGTH);
    }

    public <T extends Enum<T>> T readEnumConstant(Class<T> enumClass) {
        return (T)((Enum[])enumClass.getEnumConstants())[this.readVarInt()];
    }

    public PacketByteBuf writeEnumConstant(Enum<?> instance) {
        return this.writeVarInt(instance.ordinal());
    }

    public int readVarInt() {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = this.readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while ((b & 0x80) == 128);
        return i;
    }

    public long readVarLong() {
        byte b;
        long l = 0L;
        int i = 0;
        do {
            b = this.readByte();
            l |= (long)(b & 0x7F) << i++ * 7;
            if (i <= 10) continue;
            throw new RuntimeException("VarLong too big");
        } while ((b & 0x80) == 128);
        return l;
    }

    public PacketByteBuf writeUuid(UUID uuid) {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
        return this;
    }

    public UUID readUuid() {
        return new UUID(this.readLong(), this.readLong());
    }

    public PacketByteBuf writeVarInt(int value) {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                this.writeByte(value);
                return this;
            }
            this.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }

    public PacketByteBuf writeVarLong(long value) {
        while (true) {
            if ((value & 0xFFFFFFFFFFFFFF80L) == 0L) {
                this.writeByte((int)value);
                return this;
            }
            this.writeByte((int)(value & 0x7FL) | 0x80);
            value >>>= 7;
        }
    }

    public PacketByteBuf writeNbt(@Nullable NbtCompound compound) {
        if (compound == null) {
            this.writeByte(0);
        } else {
            try {
                NbtIo.write(compound, (DataOutput)new ByteBufOutputStream(this));
            }
            catch (IOException iOException) {
                throw new EncoderException(iOException);
            }
        }
        return this;
    }

    @Nullable
    public NbtCompound readNbt() {
        return this.readNbt(new NbtTagSizeTracker(0x200000L));
    }

    @Nullable
    public NbtCompound readUnlimitedNbt() {
        return this.readNbt(NbtTagSizeTracker.EMPTY);
    }

    @Nullable
    public NbtCompound readNbt(NbtTagSizeTracker sizeTracker) {
        int i = this.readerIndex();
        byte b = this.readByte();
        if (b == 0) {
            return null;
        }
        this.readerIndex(i);
        try {
            return NbtIo.read(new ByteBufInputStream(this), sizeTracker);
        }
        catch (IOException iOException) {
            throw new EncoderException(iOException);
        }
    }

    public PacketByteBuf writeItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            this.writeBoolean(false);
        } else {
            this.writeBoolean(true);
            Item lv = stack.getItem();
            this.writeRegistryValue(Registries.ITEM, lv);
            this.writeByte(stack.getCount());
            NbtCompound lv2 = null;
            if (lv.isDamageable() || lv.isNbtSynced()) {
                lv2 = stack.getNbt();
            }
            this.writeNbt(lv2);
        }
        return this;
    }

    public ItemStack readItemStack() {
        if (!this.readBoolean()) {
            return ItemStack.EMPTY;
        }
        Item lv = this.readRegistryValue(Registries.ITEM);
        byte i = this.readByte();
        ItemStack lv2 = new ItemStack(lv, (int)i);
        lv2.setNbt(this.readNbt());
        return lv2;
    }

    public String readString() {
        return this.readString(DEFAULT_MAX_STRING_LENGTH);
    }

    public String readString(int maxLength) {
        int j = PacketByteBuf.toEncodedStringLength(maxLength);
        int k = this.readVarInt();
        if (k > j) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
        }
        if (k < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        String string = this.toString(this.readerIndex(), k, StandardCharsets.UTF_8);
        this.readerIndex(this.readerIndex() + k);
        if (string.length() > maxLength) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + string.length() + " > " + maxLength + ")");
        }
        return string;
    }

    public PacketByteBuf writeString(String string) {
        return this.writeString(string, DEFAULT_MAX_STRING_LENGTH);
    }

    public PacketByteBuf writeString(String string, int maxLength) {
        int j;
        if (string.length() > maxLength) {
            throw new EncoderException("String too big (was " + string.length() + " characters, max " + maxLength + ")");
        }
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        if (bs.length > (j = PacketByteBuf.toEncodedStringLength(maxLength))) {
            throw new EncoderException("String too big (was " + bs.length + " bytes encoded, max " + j + ")");
        }
        this.writeVarInt(bs.length);
        this.writeBytes(bs);
        return this;
    }

    private static int toEncodedStringLength(int decodedLength) {
        return decodedLength * 3;
    }

    public Identifier readIdentifier() {
        return new Identifier(this.readString(DEFAULT_MAX_STRING_LENGTH));
    }

    public PacketByteBuf writeIdentifier(Identifier id) {
        this.writeString(id.toString());
        return this;
    }

    public <T> RegistryKey<T> readRegistryKey(RegistryKey<? extends Registry<T>> registryRef) {
        Identifier lv = this.readIdentifier();
        return RegistryKey.of(registryRef, lv);
    }

    public void writeRegistryKey(RegistryKey<?> key) {
        this.writeIdentifier(key.getValue());
    }

    public Date readDate() {
        return new Date(this.readLong());
    }

    public PacketByteBuf writeDate(Date date) {
        this.writeLong(date.getTime());
        return this;
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant instant) {
        this.writeLong(instant.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return NetworkEncryptionUtils.decodeEncodedRsaPublicKey(this.readByteArray(512));
        }
        catch (NetworkEncryptionException lv) {
            throw new DecoderException("Malformed public key bytes", lv);
        }
    }

    public PacketByteBuf writePublicKey(PublicKey publicKey) {
        this.writeByteArray(publicKey.getEncoded());
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        BlockPos lv = this.readBlockPos();
        Direction lv2 = this.readEnumConstant(Direction.class);
        float f = this.readFloat();
        float g = this.readFloat();
        float h = this.readFloat();
        boolean bl = this.readBoolean();
        return new BlockHitResult(new Vec3d((double)lv.getX() + (double)f, (double)lv.getY() + (double)g, (double)lv.getZ() + (double)h), lv2, lv, bl);
    }

    public void writeBlockHitResult(BlockHitResult hitResult) {
        BlockPos lv = hitResult.getBlockPos();
        this.writeBlockPos(lv);
        this.writeEnumConstant(hitResult.getSide());
        Vec3d lv2 = hitResult.getPos();
        this.writeFloat((float)(lv2.x - (double)lv.getX()));
        this.writeFloat((float)(lv2.y - (double)lv.getY()));
        this.writeFloat((float)(lv2.z - (double)lv.getZ()));
        this.writeBoolean(hitResult.isInsideBlock());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet bitSet) {
        this.writeLongArray(bitSet.toLongArray());
    }

    public BitSet readBitSet(int size) {
        byte[] bs = new byte[MathHelper.ceilDiv(size, 8)];
        this.readBytes(bs);
        return BitSet.valueOf(bs);
    }

    public void writeBitSet(BitSet bitSet, int size) {
        if (bitSet.length() > size) {
            throw new EncoderException("BitSet is larger than expected size (" + bitSet.length() + ">" + size + ")");
        }
        byte[] bs = bitSet.toByteArray();
        this.writeBytes(Arrays.copyOf(bs, MathHelper.ceilDiv(size, 8)));
    }

    public GameProfile readGameProfile() {
        UUID uUID = this.readUuid();
        String string = this.readString(16);
        GameProfile gameProfile = new GameProfile(uUID, string);
        gameProfile.getProperties().putAll(this.readPropertyMap());
        return gameProfile;
    }

    public void writeGameProfile(GameProfile gameProfile) {
        this.writeUuid(gameProfile.getId());
        this.writeString(gameProfile.getName());
        this.writePropertyMap(gameProfile.getProperties());
    }

    public PropertyMap readPropertyMap() {
        PropertyMap propertyMap = new PropertyMap();
        this.forEachInCollection(buf -> {
            Property property = this.readProperty();
            propertyMap.put(property.getName(), property);
        });
        return propertyMap;
    }

    public void writePropertyMap(PropertyMap propertyMap) {
        this.writeCollection(propertyMap.values(), PacketByteBuf::writeProperty);
    }

    public Property readProperty() {
        String string = this.readString();
        String string2 = this.readString();
        if (this.readBoolean()) {
            String string3 = this.readString();
            return new Property(string, string2, string3);
        }
        return new Property(string, string2);
    }

    public void writeProperty(Property property) {
        this.writeString(property.getName());
        this.writeString(property.getValue());
        if (property.hasSignature()) {
            this.writeBoolean(true);
            this.writeString(property.getSignature());
        } else {
            this.writeBoolean(false);
        }
    }

    @Override
    public int capacity() {
        return this.parent.capacity();
    }

    @Override
    public ByteBuf capacity(int capacity) {
        return this.parent.capacity(capacity);
    }

    @Override
    public int maxCapacity() {
        return this.parent.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.parent.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.parent.order();
    }

    @Override
    public ByteBuf order(ByteOrder byteOrder) {
        return this.parent.order(byteOrder);
    }

    @Override
    public ByteBuf unwrap() {
        return this.parent.unwrap();
    }

    @Override
    public boolean isDirect() {
        return this.parent.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.parent.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.parent.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.parent.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int index) {
        return this.parent.readerIndex(index);
    }

    @Override
    public int writerIndex() {
        return this.parent.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int index) {
        return this.parent.writerIndex(index);
    }

    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.parent.setIndex(readerIndex, writerIndex);
    }

    @Override
    public int readableBytes() {
        return this.parent.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.parent.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.parent.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.parent.isReadable();
    }

    @Override
    public boolean isReadable(int size) {
        return this.parent.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return this.parent.isWritable();
    }

    @Override
    public boolean isWritable(int size) {
        return this.parent.isWritable(size);
    }

    @Override
    public ByteBuf clear() {
        return this.parent.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.parent.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.parent.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.parent.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.parent.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.parent.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.parent.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int minBytes) {
        return this.parent.ensureWritable(minBytes);
    }

    @Override
    public int ensureWritable(int minBytes, boolean force) {
        return this.parent.ensureWritable(minBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return this.parent.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return this.parent.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return this.parent.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return this.parent.getShort(index);
    }

    @Override
    public short getShortLE(int index) {
        return this.parent.getShortLE(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return this.parent.getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return this.parent.getUnsignedShortLE(index);
    }

    @Override
    public int getMedium(int index) {
        return this.parent.getMedium(index);
    }

    @Override
    public int getMediumLE(int index) {
        return this.parent.getMediumLE(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return this.parent.getUnsignedMedium(index);
    }

    @Override
    public int getUnsignedMediumLE(int index) {
        return this.parent.getUnsignedMediumLE(index);
    }

    @Override
    public int getInt(int index) {
        return this.parent.getInt(index);
    }

    @Override
    public int getIntLE(int index) {
        return this.parent.getIntLE(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return this.parent.getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return this.parent.getUnsignedIntLE(index);
    }

    @Override
    public long getLong(int index) {
        return this.parent.getLong(index);
    }

    @Override
    public long getLongLE(int index) {
        return this.parent.getLongLE(index);
    }

    @Override
    public char getChar(int index) {
        return this.parent.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return this.parent.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return this.parent.getDouble(index);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf buf) {
        return this.parent.getBytes(index, buf);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf buf, int length) {
        return this.parent.getBytes(index, buf, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf buf, int outputIndex, int length) {
        return this.parent.getBytes(index, buf, outputIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] bytes) {
        return this.parent.getBytes(index, bytes);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] bytes, int outputIndex, int length) {
        return this.parent.getBytes(index, bytes, outputIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer buf) {
        return this.parent.getBytes(index, buf);
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream stream, int length) throws IOException {
        return this.parent.getBytes(index, stream, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel channel, int length) throws IOException {
        return this.parent.getBytes(index, channel, length);
    }

    @Override
    public int getBytes(int index, FileChannel channel, long pos, int length) throws IOException {
        return this.parent.getBytes(index, channel, pos, length);
    }

    @Override
    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return this.parent.getCharSequence(index, length, charset);
    }

    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        return this.parent.setBoolean(index, value);
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        return this.parent.setByte(index, value);
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        return this.parent.setShort(index, value);
    }

    @Override
    public ByteBuf setShortLE(int index, int value) {
        return this.parent.setShortLE(index, value);
    }

    @Override
    public ByteBuf setMedium(int index, int value) {
        return this.parent.setMedium(index, value);
    }

    @Override
    public ByteBuf setMediumLE(int index, int value) {
        return this.parent.setMediumLE(index, value);
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        return this.parent.setInt(index, value);
    }

    @Override
    public ByteBuf setIntLE(int index, int value) {
        return this.parent.setIntLE(index, value);
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        return this.parent.setLong(index, value);
    }

    @Override
    public ByteBuf setLongLE(int index, long value) {
        return this.parent.setLongLE(index, value);
    }

    @Override
    public ByteBuf setChar(int index, int value) {
        return this.parent.setChar(index, value);
    }

    @Override
    public ByteBuf setFloat(int index, float value) {
        return this.parent.setFloat(index, value);
    }

    @Override
    public ByteBuf setDouble(int index, double value) {
        return this.parent.setDouble(index, value);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf buf) {
        return this.parent.setBytes(index, buf);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf buf, int length) {
        return this.parent.setBytes(index, buf, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf buf, int sourceIndex, int length) {
        return this.parent.setBytes(index, buf, sourceIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] bytes) {
        return this.parent.setBytes(index, bytes);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] bytes, int sourceIndex, int length) {
        return this.parent.setBytes(index, bytes, sourceIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer buf) {
        return this.parent.setBytes(index, buf);
    }

    @Override
    public int setBytes(int index, InputStream stream, int length) throws IOException {
        return this.parent.setBytes(index, stream, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel channel, int length) throws IOException {
        return this.parent.setBytes(index, channel, length);
    }

    @Override
    public int setBytes(int index, FileChannel channel, long pos, int length) throws IOException {
        return this.parent.setBytes(index, channel, pos, length);
    }

    @Override
    public ByteBuf setZero(int index, int length) {
        return this.parent.setZero(index, length);
    }

    @Override
    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return this.parent.setCharSequence(index, sequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return this.parent.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.parent.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.parent.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.parent.readShort();
    }

    @Override
    public short readShortLE() {
        return this.parent.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.parent.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.parent.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.parent.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.parent.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.parent.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.parent.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.parent.readInt();
    }

    @Override
    public int readIntLE() {
        return this.parent.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.parent.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.parent.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.parent.readLong();
    }

    @Override
    public long readLongLE() {
        return this.parent.readLongLE();
    }

    @Override
    public char readChar() {
        return this.parent.readChar();
    }

    @Override
    public float readFloat() {
        return this.parent.readFloat();
    }

    @Override
    public double readDouble() {
        return this.parent.readDouble();
    }

    @Override
    public ByteBuf readBytes(int length) {
        return this.parent.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return this.parent.readSlice(length);
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        return this.parent.readRetainedSlice(length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf buf) {
        return this.parent.readBytes(buf);
    }

    @Override
    public ByteBuf readBytes(ByteBuf buf, int length) {
        return this.parent.readBytes(buf, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf buf, int outputIndex, int length) {
        return this.parent.readBytes(buf, outputIndex, length);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes) {
        return this.parent.readBytes(bytes);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes, int outputIndex, int length) {
        return this.parent.readBytes(bytes, outputIndex, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer buf) {
        return this.parent.readBytes(buf);
    }

    @Override
    public ByteBuf readBytes(OutputStream stream, int length) throws IOException {
        return this.parent.readBytes(stream, length);
    }

    @Override
    public int readBytes(GatheringByteChannel channel, int length) throws IOException {
        return this.parent.readBytes(channel, length);
    }

    @Override
    public CharSequence readCharSequence(int length, Charset charset) {
        return this.parent.readCharSequence(length, charset);
    }

    @Override
    public int readBytes(FileChannel channel, long pos, int length) throws IOException {
        return this.parent.readBytes(channel, pos, length);
    }

    @Override
    public ByteBuf skipBytes(int length) {
        return this.parent.skipBytes(length);
    }

    @Override
    public ByteBuf writeBoolean(boolean value) {
        return this.parent.writeBoolean(value);
    }

    @Override
    public ByteBuf writeByte(int value) {
        return this.parent.writeByte(value);
    }

    @Override
    public ByteBuf writeShort(int value) {
        return this.parent.writeShort(value);
    }

    @Override
    public ByteBuf writeShortLE(int value) {
        return this.parent.writeShortLE(value);
    }

    @Override
    public ByteBuf writeMedium(int value) {
        return this.parent.writeMedium(value);
    }

    @Override
    public ByteBuf writeMediumLE(int value) {
        return this.parent.writeMediumLE(value);
    }

    @Override
    public ByteBuf writeInt(int value) {
        return this.parent.writeInt(value);
    }

    @Override
    public ByteBuf writeIntLE(int value) {
        return this.parent.writeIntLE(value);
    }

    @Override
    public ByteBuf writeLong(long value) {
        return this.parent.writeLong(value);
    }

    @Override
    public ByteBuf writeLongLE(long value) {
        return this.parent.writeLongLE(value);
    }

    @Override
    public ByteBuf writeChar(int value) {
        return this.parent.writeChar(value);
    }

    @Override
    public ByteBuf writeFloat(float value) {
        return this.parent.writeFloat(value);
    }

    @Override
    public ByteBuf writeDouble(double value) {
        return this.parent.writeDouble(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf) {
        return this.parent.writeBytes(buf);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf, int length) {
        return this.parent.writeBytes(buf, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf, int sourceIndex, int length) {
        return this.parent.writeBytes(buf, sourceIndex, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes) {
        return this.parent.writeBytes(bytes);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes, int sourceIndex, int length) {
        return this.parent.writeBytes(bytes, sourceIndex, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer buf) {
        return this.parent.writeBytes(buf);
    }

    @Override
    public int writeBytes(InputStream stream, int length) throws IOException {
        return this.parent.writeBytes(stream, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel channel, int length) throws IOException {
        return this.parent.writeBytes(channel, length);
    }

    @Override
    public int writeBytes(FileChannel channel, long pos, int length) throws IOException {
        return this.parent.writeBytes(channel, pos, length);
    }

    @Override
    public ByteBuf writeZero(int length) {
        return this.parent.writeZero(length);
    }

    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return this.parent.writeCharSequence(sequence, charset);
    }

    @Override
    public int indexOf(int from, int to, byte value) {
        return this.parent.indexOf(from, to, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return this.parent.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return this.parent.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return this.parent.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(ByteProcessor byteProcessor) {
        return this.parent.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor byteProcessor) {
        return this.parent.forEachByte(index, length, byteProcessor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return this.parent.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor byteProcessor) {
        return this.parent.forEachByteDesc(index, length, byteProcessor);
    }

    @Override
    public ByteBuf copy() {
        return this.parent.copy();
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return this.parent.copy(index, length);
    }

    @Override
    public ByteBuf slice() {
        return this.parent.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.parent.retainedSlice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return this.parent.slice(index, length);
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return this.parent.retainedSlice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return this.parent.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.parent.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.parent.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.parent.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return this.parent.nioBuffer(index, length);
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.parent.internalNioBuffer(index, length);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.parent.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return this.parent.nioBuffers(index, length);
    }

    @Override
    public boolean hasArray() {
        return this.parent.hasArray();
    }

    @Override
    public byte[] array() {
        return this.parent.array();
    }

    @Override
    public int arrayOffset() {
        return this.parent.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.parent.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.parent.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return this.parent.toString(charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        return this.parent.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return this.parent.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.parent.equals(o);
    }

    @Override
    public int compareTo(ByteBuf byteBuf) {
        return this.parent.compareTo(byteBuf);
    }

    @Override
    public String toString() {
        return this.parent.toString();
    }

    @Override
    public ByteBuf retain(int i) {
        return this.parent.retain(i);
    }

    @Override
    public ByteBuf retain() {
        return this.parent.retain();
    }

    @Override
    public ByteBuf touch() {
        return this.parent.touch();
    }

    @Override
    public ByteBuf touch(Object object) {
        return this.parent.touch(object);
    }

    @Override
    public int refCnt() {
        return this.parent.refCnt();
    }

    @Override
    public boolean release() {
        return this.parent.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.parent.release(decrement);
    }

    @FunctionalInterface
    public static interface PacketWriter<T>
    extends BiConsumer<PacketByteBuf, T> {
        default public PacketWriter<Optional<T>> asOptional() {
            return (buf, value) -> buf.writeOptional(value, this);
        }
    }

    @FunctionalInterface
    public static interface PacketReader<T>
    extends Function<PacketByteBuf, T> {
        default public PacketReader<Optional<T>> asOptional() {
            return buf -> buf.readOptional(this);
        }
    }
}

