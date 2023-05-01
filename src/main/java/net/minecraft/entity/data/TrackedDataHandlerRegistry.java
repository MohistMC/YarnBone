/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.data;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerData;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TrackedDataHandlerRegistry {
    private static final Int2ObjectBiMap<TrackedDataHandler<?>> DATA_HANDLERS = Int2ObjectBiMap.create(16);
    public static final TrackedDataHandler<Byte> BYTE = TrackedDataHandler.of((buf, byte_) -> buf.writeByte(byte_.byteValue()), PacketByteBuf::readByte);
    public static final TrackedDataHandler<Integer> INTEGER = TrackedDataHandler.of(PacketByteBuf::writeVarInt, PacketByteBuf::readVarInt);
    public static final TrackedDataHandler<Long> LONG = TrackedDataHandler.of(PacketByteBuf::writeVarLong, PacketByteBuf::readVarLong);
    public static final TrackedDataHandler<Float> FLOAT = TrackedDataHandler.of(PacketByteBuf::writeFloat, PacketByteBuf::readFloat);
    public static final TrackedDataHandler<String> STRING = TrackedDataHandler.of(PacketByteBuf::writeString, PacketByteBuf::readString);
    public static final TrackedDataHandler<Text> TEXT_COMPONENT = TrackedDataHandler.of(PacketByteBuf::writeText, PacketByteBuf::readText);
    public static final TrackedDataHandler<Optional<Text>> OPTIONAL_TEXT_COMPONENT = TrackedDataHandler.ofOptional(PacketByteBuf::writeText, PacketByteBuf::readText);
    public static final TrackedDataHandler<ItemStack> ITEM_STACK = new TrackedDataHandler<ItemStack>(){

        @Override
        public void write(PacketByteBuf arg, ItemStack arg2) {
            arg.writeItemStack(arg2);
        }

        @Override
        public ItemStack read(PacketByteBuf arg) {
            return arg.readItemStack();
        }

        @Override
        public ItemStack copy(ItemStack arg) {
            return arg.copy();
        }

        @Override
        public /* synthetic */ Object read(PacketByteBuf buf) {
            return this.read(buf);
        }
    };
    public static final TrackedDataHandler<BlockState> BLOCK_STATE = TrackedDataHandler.of(Block.STATE_IDS);
    public static final TrackedDataHandler<Optional<BlockState>> OPTIONAL_BLOCK_STATE = new TrackedDataHandler.ImmutableHandler<Optional<BlockState>>(){

        @Override
        public void write(PacketByteBuf arg, Optional<BlockState> optional) {
            if (optional.isPresent()) {
                arg.writeVarInt(Block.getRawIdFromState(optional.get()));
            } else {
                arg.writeVarInt(0);
            }
        }

        @Override
        public Optional<BlockState> read(PacketByteBuf arg) {
            int i = arg.readVarInt();
            if (i == 0) {
                return Optional.empty();
            }
            return Optional.of(Block.getStateFromRawId(i));
        }

        @Override
        public /* synthetic */ Object read(PacketByteBuf buf) {
            return this.read(buf);
        }
    };
    public static final TrackedDataHandler<Boolean> BOOLEAN = TrackedDataHandler.of(PacketByteBuf::writeBoolean, PacketByteBuf::readBoolean);
    public static final TrackedDataHandler<ParticleEffect> PARTICLE = new TrackedDataHandler.ImmutableHandler<ParticleEffect>(){

        @Override
        public void write(PacketByteBuf arg, ParticleEffect arg2) {
            arg.writeRegistryValue(Registries.PARTICLE_TYPE, arg2.getType());
            arg2.write(arg);
        }

        @Override
        public ParticleEffect read(PacketByteBuf arg) {
            return this.read(arg, arg.readRegistryValue(Registries.PARTICLE_TYPE));
        }

        private <T extends ParticleEffect> T read(PacketByteBuf buf, ParticleType<T> type) {
            return type.getParametersFactory().read(type, buf);
        }

        @Override
        public /* synthetic */ Object read(PacketByteBuf buf) {
            return this.read(buf);
        }
    };
    public static final TrackedDataHandler<EulerAngle> ROTATION = new TrackedDataHandler.ImmutableHandler<EulerAngle>(){

        @Override
        public void write(PacketByteBuf arg, EulerAngle arg2) {
            arg.writeFloat(arg2.getPitch());
            arg.writeFloat(arg2.getYaw());
            arg.writeFloat(arg2.getRoll());
        }

        @Override
        public EulerAngle read(PacketByteBuf arg) {
            return new EulerAngle(arg.readFloat(), arg.readFloat(), arg.readFloat());
        }

        @Override
        public /* synthetic */ Object read(PacketByteBuf buf) {
            return this.read(buf);
        }
    };
    public static final TrackedDataHandler<BlockPos> BLOCK_POS = TrackedDataHandler.of(PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
    public static final TrackedDataHandler<Optional<BlockPos>> OPTIONAL_BLOCK_POS = TrackedDataHandler.ofOptional(PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
    public static final TrackedDataHandler<Direction> FACING = TrackedDataHandler.ofEnum(Direction.class);
    public static final TrackedDataHandler<Optional<UUID>> OPTIONAL_UUID = TrackedDataHandler.ofOptional(PacketByteBuf::writeUuid, PacketByteBuf::readUuid);
    public static final TrackedDataHandler<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = TrackedDataHandler.ofOptional(PacketByteBuf::writeGlobalPos, PacketByteBuf::readGlobalPos);
    public static final TrackedDataHandler<NbtCompound> NBT_COMPOUND = new TrackedDataHandler<NbtCompound>(){

        @Override
        public void write(PacketByteBuf arg, NbtCompound arg2) {
            arg.writeNbt(arg2);
        }

        @Override
        public NbtCompound read(PacketByteBuf arg) {
            return arg.readNbt();
        }

        @Override
        public NbtCompound copy(NbtCompound arg) {
            return arg.copy();
        }

        @Override
        public /* synthetic */ Object read(PacketByteBuf buf) {
            return this.read(buf);
        }
    };
    public static final TrackedDataHandler<VillagerData> VILLAGER_DATA = new TrackedDataHandler.ImmutableHandler<VillagerData>(){

        @Override
        public void write(PacketByteBuf arg, VillagerData arg2) {
            arg.writeRegistryValue(Registries.VILLAGER_TYPE, arg2.getType());
            arg.writeRegistryValue(Registries.VILLAGER_PROFESSION, arg2.getProfession());
            arg.writeVarInt(arg2.getLevel());
        }

        @Override
        public VillagerData read(PacketByteBuf arg) {
            return new VillagerData(arg.readRegistryValue(Registries.VILLAGER_TYPE), arg.readRegistryValue(Registries.VILLAGER_PROFESSION), arg.readVarInt());
        }

        @Override
        public /* synthetic */ Object read(PacketByteBuf buf) {
            return this.read(buf);
        }
    };
    public static final TrackedDataHandler<OptionalInt> OPTIONAL_INT = new TrackedDataHandler.ImmutableHandler<OptionalInt>(){

        @Override
        public void write(PacketByteBuf arg, OptionalInt optionalInt) {
            arg.writeVarInt(optionalInt.orElse(-1) + 1);
        }

        @Override
        public OptionalInt read(PacketByteBuf arg) {
            int i = arg.readVarInt();
            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
        }

        @Override
        public /* synthetic */ Object read(PacketByteBuf buf) {
            return this.read(buf);
        }
    };
    public static final TrackedDataHandler<EntityPose> ENTITY_POSE = TrackedDataHandler.ofEnum(EntityPose.class);
    public static final TrackedDataHandler<CatVariant> CAT_VARIANT = TrackedDataHandler.of(Registries.CAT_VARIANT);
    public static final TrackedDataHandler<FrogVariant> FROG_VARIANT = TrackedDataHandler.of(Registries.FROG_VARIANT);
    public static final TrackedDataHandler<RegistryEntry<PaintingVariant>> PAINTING_VARIANT = TrackedDataHandler.of(Registries.PAINTING_VARIANT.getIndexedEntries());
    public static final TrackedDataHandler<SnifferEntity.State> SNIFFER_STATE = TrackedDataHandler.ofEnum(SnifferEntity.State.class);
    public static final TrackedDataHandler<Vector3f> VECTOR3F = TrackedDataHandler.of(PacketByteBuf::writeVector3f, PacketByteBuf::readVector3f);
    public static final TrackedDataHandler<Quaternionf> QUATERNIONF = TrackedDataHandler.of(PacketByteBuf::writeQuaternionf, PacketByteBuf::readQuaternionf);

    public static void register(TrackedDataHandler<?> handler) {
        DATA_HANDLERS.add(handler);
    }

    @Nullable
    public static TrackedDataHandler<?> get(int id) {
        return DATA_HANDLERS.get(id);
    }

    public static int getId(TrackedDataHandler<?> handler) {
        return DATA_HANDLERS.getRawId(handler);
    }

    private TrackedDataHandlerRegistry() {
    }

    static {
        TrackedDataHandlerRegistry.register(BYTE);
        TrackedDataHandlerRegistry.register(INTEGER);
        TrackedDataHandlerRegistry.register(LONG);
        TrackedDataHandlerRegistry.register(FLOAT);
        TrackedDataHandlerRegistry.register(STRING);
        TrackedDataHandlerRegistry.register(TEXT_COMPONENT);
        TrackedDataHandlerRegistry.register(OPTIONAL_TEXT_COMPONENT);
        TrackedDataHandlerRegistry.register(ITEM_STACK);
        TrackedDataHandlerRegistry.register(BOOLEAN);
        TrackedDataHandlerRegistry.register(ROTATION);
        TrackedDataHandlerRegistry.register(BLOCK_POS);
        TrackedDataHandlerRegistry.register(OPTIONAL_BLOCK_POS);
        TrackedDataHandlerRegistry.register(FACING);
        TrackedDataHandlerRegistry.register(OPTIONAL_UUID);
        TrackedDataHandlerRegistry.register(BLOCK_STATE);
        TrackedDataHandlerRegistry.register(OPTIONAL_BLOCK_STATE);
        TrackedDataHandlerRegistry.register(NBT_COMPOUND);
        TrackedDataHandlerRegistry.register(PARTICLE);
        TrackedDataHandlerRegistry.register(VILLAGER_DATA);
        TrackedDataHandlerRegistry.register(OPTIONAL_INT);
        TrackedDataHandlerRegistry.register(ENTITY_POSE);
        TrackedDataHandlerRegistry.register(CAT_VARIANT);
        TrackedDataHandlerRegistry.register(FROG_VARIANT);
        TrackedDataHandlerRegistry.register(OPTIONAL_GLOBAL_POS);
        TrackedDataHandlerRegistry.register(PAINTING_VARIANT);
        TrackedDataHandlerRegistry.register(SNIFFER_STATE);
        TrackedDataHandlerRegistry.register(VECTOR3F);
        TrackedDataHandlerRegistry.register(QUATERNIONF);
    }
}

