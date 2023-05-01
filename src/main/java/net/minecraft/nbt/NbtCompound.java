/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;

public class NbtCompound
implements NbtElement {
    public static final Codec<NbtCompound> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        NbtElement lv = dynamic.convert(NbtOps.INSTANCE).getValue();
        if (lv instanceof NbtCompound) {
            return DataResult.success((NbtCompound)lv);
        }
        return DataResult.error(() -> "Not a compound tag: " + lv);
    }, nbt -> new Dynamic<NbtCompound>(NbtOps.INSTANCE, (NbtCompound)nbt));
    private static final int SIZE = 48;
    private static final int field_41719 = 32;
    public static final NbtType<NbtCompound> TYPE = new NbtType.OfVariableSize<NbtCompound>(){

        @Override
        public NbtCompound read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
            byte b;
            arg.add(48L);
            if (i > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            }
            HashMap<String, NbtElement> map = Maps.newHashMap();
            while ((b = NbtCompound.readByte(dataInput, arg)) != 0) {
                String string = NbtCompound.readString(dataInput, arg);
                arg.add(28 + 2 * string.length());
                NbtElement lv = NbtCompound.read(NbtTypes.byId(b), string, dataInput, i + 1, arg);
                if (map.put(string, lv) != null) continue;
                arg.add(36L);
            }
            return new NbtCompound(map);
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
            byte b;
            block13: while ((b = input.readByte()) != 0) {
                NbtType<?> lv = NbtTypes.byId(b);
                switch (visitor.visitSubNbtType(lv)) {
                    case HALT: {
                        return NbtScanner.Result.HALT;
                    }
                    case BREAK: {
                        NbtString.skip(input);
                        lv.skip(input);
                        break block13;
                    }
                    case SKIP: {
                        NbtString.skip(input);
                        lv.skip(input);
                        continue block13;
                    }
                    default: {
                        String string = input.readUTF();
                        switch (visitor.startSubNbt(lv, string)) {
                            case HALT: {
                                return NbtScanner.Result.HALT;
                            }
                            case BREAK: {
                                lv.skip(input);
                                break block13;
                            }
                            case SKIP: {
                                lv.skip(input);
                                continue block13;
                            }
                        }
                        switch (lv.doAccept(input, visitor)) {
                            case HALT: {
                                return NbtScanner.Result.HALT;
                            }
                        }
                        continue block13;
                    }
                }
            }
            if (b != 0) {
                while ((b = input.readByte()) != 0) {
                    NbtString.skip(input);
                    NbtTypes.byId(b).skip(input);
                }
            }
            return visitor.endNested();
        }

        @Override
        public void skip(DataInput input) throws IOException {
            byte b;
            while ((b = input.readByte()) != 0) {
                NbtString.skip(input);
                NbtTypes.byId(b).skip(input);
            }
        }

        @Override
        public String getCrashReportName() {
            return "COMPOUND";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Compound";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
            return this.read(input, depth, tracker);
        }
    };
    private final Map<String, NbtElement> entries;

    protected NbtCompound(Map<String, NbtElement> entries) {
        this.entries = entries;
    }

    public NbtCompound() {
        this(Maps.newHashMap());
    }

    @Override
    public void write(DataOutput output) throws IOException {
        for (String string : this.entries.keySet()) {
            NbtElement lv = this.entries.get(string);
            NbtCompound.write(string, lv, output);
        }
        output.writeByte(0);
    }

    @Override
    public int getSizeInBytes() {
        int i = 48;
        for (Map.Entry<String, NbtElement> entry : this.entries.entrySet()) {
            i += 28 + 2 * entry.getKey().length();
            i += 36;
            i += entry.getValue().getSizeInBytes();
        }
        return i;
    }

    public Set<String> getKeys() {
        return this.entries.keySet();
    }

    @Override
    public byte getType() {
        return NbtElement.COMPOUND_TYPE;
    }

    public NbtType<NbtCompound> getNbtType() {
        return TYPE;
    }

    public int getSize() {
        return this.entries.size();
    }

    @Nullable
    public NbtElement put(String key, NbtElement element) {
        return this.entries.put(key, element);
    }

    public void putByte(String key, byte value) {
        this.entries.put(key, NbtByte.of(value));
    }

    public void putShort(String key, short value) {
        this.entries.put(key, NbtShort.of(value));
    }

    public void putInt(String key, int value) {
        this.entries.put(key, NbtInt.of(value));
    }

    public void putLong(String key, long value) {
        this.entries.put(key, NbtLong.of(value));
    }

    public void putUuid(String key, UUID value) {
        this.entries.put(key, NbtHelper.fromUuid(value));
    }

    public UUID getUuid(String key) {
        return NbtHelper.toUuid(this.get(key));
    }

    public boolean containsUuid(String key) {
        NbtElement lv = this.get(key);
        return lv != null && lv.getNbtType() == NbtIntArray.TYPE && ((NbtIntArray)lv).getIntArray().length == 4;
    }

    public void putFloat(String key, float value) {
        this.entries.put(key, NbtFloat.of(value));
    }

    public void putDouble(String key, double value) {
        this.entries.put(key, NbtDouble.of(value));
    }

    public void putString(String key, String value) {
        this.entries.put(key, NbtString.of(value));
    }

    public void putByteArray(String key, byte[] value) {
        this.entries.put(key, new NbtByteArray(value));
    }

    public void putByteArray(String key, List<Byte> value) {
        this.entries.put(key, new NbtByteArray(value));
    }

    public void putIntArray(String key, int[] value) {
        this.entries.put(key, new NbtIntArray(value));
    }

    public void putIntArray(String key, List<Integer> value) {
        this.entries.put(key, new NbtIntArray(value));
    }

    public void putLongArray(String key, long[] value) {
        this.entries.put(key, new NbtLongArray(value));
    }

    public void putLongArray(String key, List<Long> value) {
        this.entries.put(key, new NbtLongArray(value));
    }

    public void putBoolean(String key, boolean value) {
        this.entries.put(key, NbtByte.of(value));
    }

    @Nullable
    public NbtElement get(String key) {
        return this.entries.get(key);
    }

    public byte getType(String key) {
        NbtElement lv = this.entries.get(key);
        if (lv == null) {
            return NbtElement.END_TYPE;
        }
        return lv.getType();
    }

    public boolean contains(String key) {
        return this.entries.containsKey(key);
    }

    public boolean contains(String key, int type) {
        byte j = this.getType(key);
        if (j == type) {
            return true;
        }
        if (type == NbtElement.NUMBER_TYPE) {
            return j == NbtElement.BYTE_TYPE || j == NbtElement.SHORT_TYPE || j == NbtElement.INT_TYPE || j == NbtElement.LONG_TYPE || j == NbtElement.FLOAT_TYPE || j == NbtElement.DOUBLE_TYPE;
        }
        return false;
    }

    public byte getByte(String key) {
        try {
            if (this.contains(key, NbtElement.NUMBER_TYPE)) {
                return ((AbstractNbtNumber)this.entries.get(key)).byteValue();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public short getShort(String key) {
        try {
            if (this.contains(key, NbtElement.NUMBER_TYPE)) {
                return ((AbstractNbtNumber)this.entries.get(key)).shortValue();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public int getInt(String key) {
        try {
            if (this.contains(key, NbtElement.NUMBER_TYPE)) {
                return ((AbstractNbtNumber)this.entries.get(key)).intValue();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public long getLong(String key) {
        try {
            if (this.contains(key, NbtElement.NUMBER_TYPE)) {
                return ((AbstractNbtNumber)this.entries.get(key)).longValue();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0L;
    }

    public float getFloat(String key) {
        try {
            if (this.contains(key, NbtElement.NUMBER_TYPE)) {
                return ((AbstractNbtNumber)this.entries.get(key)).floatValue();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0.0f;
    }

    public double getDouble(String key) {
        try {
            if (this.contains(key, NbtElement.NUMBER_TYPE)) {
                return ((AbstractNbtNumber)this.entries.get(key)).doubleValue();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0.0;
    }

    public String getString(String key) {
        try {
            if (this.contains(key, NbtElement.STRING_TYPE)) {
                return this.entries.get(key).asString();
            }
        }
        catch (ClassCastException classCastException) {
            // empty catch block
        }
        return "";
    }

    public byte[] getByteArray(String key) {
        try {
            if (this.contains(key, NbtElement.BYTE_ARRAY_TYPE)) {
                return ((NbtByteArray)this.entries.get(key)).getByteArray();
            }
        }
        catch (ClassCastException classCastException) {
            throw new CrashException(this.createCrashReport(key, NbtByteArray.TYPE, classCastException));
        }
        return new byte[0];
    }

    public int[] getIntArray(String key) {
        try {
            if (this.contains(key, NbtElement.INT_ARRAY_TYPE)) {
                return ((NbtIntArray)this.entries.get(key)).getIntArray();
            }
        }
        catch (ClassCastException classCastException) {
            throw new CrashException(this.createCrashReport(key, NbtIntArray.TYPE, classCastException));
        }
        return new int[0];
    }

    public long[] getLongArray(String key) {
        try {
            if (this.contains(key, NbtElement.LONG_ARRAY_TYPE)) {
                return ((NbtLongArray)this.entries.get(key)).getLongArray();
            }
        }
        catch (ClassCastException classCastException) {
            throw new CrashException(this.createCrashReport(key, NbtLongArray.TYPE, classCastException));
        }
        return new long[0];
    }

    public NbtCompound getCompound(String key) {
        try {
            if (this.contains(key, NbtElement.COMPOUND_TYPE)) {
                return (NbtCompound)this.entries.get(key);
            }
        }
        catch (ClassCastException classCastException) {
            throw new CrashException(this.createCrashReport(key, TYPE, classCastException));
        }
        return new NbtCompound();
    }

    public NbtList getList(String key, int type) {
        try {
            if (this.getType(key) == NbtElement.LIST_TYPE) {
                NbtList lv = (NbtList)this.entries.get(key);
                if (lv.isEmpty() || lv.getHeldType() == type) {
                    return lv;
                }
                return new NbtList();
            }
        }
        catch (ClassCastException classCastException) {
            throw new CrashException(this.createCrashReport(key, NbtList.TYPE, classCastException));
        }
        return new NbtList();
    }

    public boolean getBoolean(String key) {
        return this.getByte(key) != 0;
    }

    public void remove(String key) {
        this.entries.remove(key);
    }

    @Override
    public String toString() {
        return this.asString();
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    private CrashReport createCrashReport(String key, NbtType<?> reader, ClassCastException exception) {
        CrashReport lv = CrashReport.create(exception, "Reading NBT data");
        CrashReportSection lv2 = lv.addElement("Corrupt NBT tag", 1);
        lv2.add("Tag type found", () -> this.entries.get(key).getNbtType().getCrashReportName());
        lv2.add("Tag type expected", reader::getCrashReportName);
        lv2.add("Tag name", key);
        return lv;
    }

    @Override
    public NbtCompound copy() {
        HashMap<String, NbtElement> map = Maps.newHashMap(Maps.transformValues(this.entries, NbtElement::copy));
        return new NbtCompound(map);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtCompound && Objects.equals(this.entries, ((NbtCompound)o).entries);
    }

    public int hashCode() {
        return this.entries.hashCode();
    }

    private static void write(String key, NbtElement element, DataOutput output) throws IOException {
        output.writeByte(element.getType());
        if (element.getType() == 0) {
            return;
        }
        output.writeUTF(key);
        element.write(output);
    }

    static byte readByte(DataInput input, NbtTagSizeTracker tracker) throws IOException {
        return input.readByte();
    }

    static String readString(DataInput input, NbtTagSizeTracker tracker) throws IOException {
        return input.readUTF();
    }

    static NbtElement read(NbtType<?> reader, String key, DataInput input, int depth, NbtTagSizeTracker tracker) {
        try {
            return reader.read(input, depth, tracker);
        }
        catch (IOException iOException) {
            CrashReport lv = CrashReport.create(iOException, "Loading NBT data");
            CrashReportSection lv2 = lv.addElement("NBT Tag");
            lv2.add("Tag name", key);
            lv2.add("Tag type", reader.getCrashReportName());
            throw new CrashException(lv);
        }
    }

    public NbtCompound copyFrom(NbtCompound source) {
        for (String string : source.entries.keySet()) {
            NbtElement lv = source.entries.get(string);
            if (lv.getType() == NbtElement.COMPOUND_TYPE) {
                if (this.contains(string, NbtElement.COMPOUND_TYPE)) {
                    NbtCompound lv2 = this.getCompound(string);
                    lv2.copyFrom((NbtCompound)lv);
                    continue;
                }
                this.put(string, lv.copy());
                continue;
            }
            this.put(string, lv.copy());
        }
        return this;
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitCompound(this);
    }

    protected Map<String, NbtElement> toMap() {
        return Collections.unmodifiableMap(this.entries);
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        block14: for (Map.Entry<String, NbtElement> entry : this.entries.entrySet()) {
            NbtElement lv = entry.getValue();
            NbtType<?> lv2 = lv.getNbtType();
            NbtScanner.NestedResult lv3 = visitor.visitSubNbtType(lv2);
            switch (lv3) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case BREAK: {
                    return visitor.endNested();
                }
                case SKIP: {
                    continue block14;
                }
            }
            lv3 = visitor.startSubNbt(lv2, entry.getKey());
            switch (lv3) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case BREAK: {
                    return visitor.endNested();
                }
                case SKIP: {
                    continue block14;
                }
            }
            NbtScanner.Result lv4 = lv.doAccept(visitor);
            switch (lv4) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case BREAK: {
                    return visitor.endNested();
                }
            }
        }
        return visitor.endNested();
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }
}

