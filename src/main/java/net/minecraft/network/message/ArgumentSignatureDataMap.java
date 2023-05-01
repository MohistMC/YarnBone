/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.MessageSignatureData;
import org.jetbrains.annotations.Nullable;

public record ArgumentSignatureDataMap(List<Entry> entries) {
    public static final ArgumentSignatureDataMap EMPTY = new ArgumentSignatureDataMap(List.of());
    private static final int MAX_ARGUMENTS = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatureDataMap(PacketByteBuf buf) {
        this(buf.readCollection(PacketByteBuf.getMaxValidator(ArrayList::new, 8), Entry::new));
    }

    @Nullable
    public MessageSignatureData get(String argumentName) {
        for (Entry lv : this.entries) {
            if (!lv.name.equals(argumentName)) continue;
            return lv.signature;
        }
        return null;
    }

    public void write(PacketByteBuf buf) {
        buf.writeCollection(this.entries, (buf2, entry) -> entry.write((PacketByteBuf)buf2));
    }

    public static ArgumentSignatureDataMap sign(SignedArgumentList<?> arguments, ArgumentSigner signer) {
        List<Entry> list = arguments.arguments().stream().map(argument -> {
            MessageSignatureData lv = signer.sign(argument.value());
            if (lv != null) {
                return new Entry(argument.getNodeName(), lv);
            }
            return null;
        }).filter(Objects::nonNull).toList();
        return new ArgumentSignatureDataMap(list);
    }

    public record Entry(String name, MessageSignatureData signature) {
        public Entry(PacketByteBuf buf) {
            this(buf.readString(16), MessageSignatureData.fromBuf(buf));
        }

        public void write(PacketByteBuf buf) {
            buf.writeString(this.name, 16);
            MessageSignatureData.write(buf, this.signature);
        }
    }

    @FunctionalInterface
    public static interface ArgumentSigner {
        @Nullable
        public MessageSignatureData sign(String var1);
    }
}

