/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;

public class NbtIo {
    public static NbtCompound readCompressed(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);){
            NbtCompound nbtCompound = NbtIo.readCompressed(inputStream);
            return nbtCompound;
        }
    }

    private static DataInputStream decompress(InputStream stream) throws IOException {
        return new DataInputStream(new FixedBufferInputStream(new GZIPInputStream(stream)));
    }

    public static NbtCompound readCompressed(InputStream stream) throws IOException {
        try (DataInputStream dataInputStream = NbtIo.decompress(stream);){
            NbtCompound nbtCompound = NbtIo.read(dataInputStream, NbtTagSizeTracker.EMPTY);
            return nbtCompound;
        }
    }

    public static void scanCompressed(File file, NbtScanner scanner) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);){
            NbtIo.scanCompressed(inputStream, scanner);
        }
    }

    public static void scanCompressed(InputStream stream, NbtScanner scanner) throws IOException {
        try (DataInputStream dataInputStream = NbtIo.decompress(stream);){
            NbtIo.scan(dataInputStream, scanner);
        }
    }

    public static void writeCompressed(NbtCompound nbt, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file);){
            NbtIo.writeCompressed(nbt, outputStream);
        }
    }

    public static void writeCompressed(NbtCompound nbt, OutputStream stream) throws IOException {
        try (DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(stream)));){
            NbtIo.write(nbt, (DataOutput)dataOutputStream);
        }
    }

    public static void write(NbtCompound nbt, File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);){
            NbtIo.write(nbt, (DataOutput)dataOutputStream);
        }
    }

    @Nullable
    public static NbtCompound read(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fileInputStream = new FileInputStream(file);){
            NbtCompound nbtCompound;
            try (DataInputStream dataInputStream = new DataInputStream(fileInputStream);){
                nbtCompound = NbtIo.read(dataInputStream, NbtTagSizeTracker.EMPTY);
            }
            return nbtCompound;
        }
    }

    public static NbtCompound read(DataInput input) throws IOException {
        return NbtIo.read(input, NbtTagSizeTracker.EMPTY);
    }

    public static NbtCompound read(DataInput input, NbtTagSizeTracker tracker) throws IOException {
        NbtElement lv = NbtIo.read(input, 0, tracker);
        if (lv instanceof NbtCompound) {
            return (NbtCompound)lv;
        }
        throw new IOException("Root tag must be a named compound tag");
    }

    public static void write(NbtCompound nbt, DataOutput output) throws IOException {
        NbtIo.write((NbtElement)nbt, output);
    }

    public static void scan(DataInput input, NbtScanner scanner) throws IOException {
        NbtType<?> lv = NbtTypes.byId(input.readByte());
        if (lv == NbtEnd.TYPE) {
            if (scanner.start(NbtEnd.TYPE) == NbtScanner.Result.CONTINUE) {
                scanner.visitEnd();
            }
            return;
        }
        switch (scanner.start(lv)) {
            case HALT: {
                break;
            }
            case BREAK: {
                NbtString.skip(input);
                lv.skip(input);
                break;
            }
            case CONTINUE: {
                NbtString.skip(input);
                lv.doAccept(input, scanner);
            }
        }
    }

    public static void write(NbtElement nbt, DataOutput output) throws IOException {
        output.writeByte(nbt.getType());
        if (nbt.getType() == 0) {
            return;
        }
        output.writeUTF("");
        nbt.write(output);
    }

    private static NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
        byte b = input.readByte();
        if (b == 0) {
            return NbtEnd.INSTANCE;
        }
        NbtString.skip(input);
        try {
            return NbtTypes.byId(b).read(input, depth, tracker);
        }
        catch (IOException iOException) {
            CrashReport lv = CrashReport.create(iOException, "Loading NBT data");
            CrashReportSection lv2 = lv.addElement("NBT Tag");
            lv2.add("Tag type", b);
            throw new CrashException(lv);
        }
    }
}

