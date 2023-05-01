/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.network.message;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.SignedMessage;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class MessageSignatureStorage {
    public static final int MISSING = -1;
    private static final int MAX_ENTRIES = 128;
    private final MessageSignatureData[] signatures;

    public MessageSignatureStorage(int maxEntries) {
        this.signatures = new MessageSignatureData[maxEntries];
    }

    public static MessageSignatureStorage create() {
        return new MessageSignatureStorage(128);
    }

    public int indexOf(MessageSignatureData signature) {
        for (int i = 0; i < this.signatures.length; ++i) {
            if (!signature.equals(this.signatures[i])) continue;
            return i;
        }
        return -1;
    }

    @Nullable
    public MessageSignatureData get(int index) {
        return this.signatures[index];
    }

    public void add(SignedMessage message) {
        List<MessageSignatureData> list = message.signedBody().lastSeenMessages().entries();
        ArrayDeque<MessageSignatureData> arrayDeque = new ArrayDeque<MessageSignatureData>(list.size() + 1);
        arrayDeque.addAll(list);
        MessageSignatureData lv = message.signature();
        if (lv != null) {
            arrayDeque.add(lv);
        }
        this.addFrom(arrayDeque);
    }

    @VisibleForTesting
    void addFrom(List<MessageSignatureData> signatures) {
        this.addFrom(new ArrayDeque<MessageSignatureData>(signatures));
    }

    private void addFrom(ArrayDeque<MessageSignatureData> deque) {
        ObjectOpenHashSet<MessageSignatureData> set = new ObjectOpenHashSet<MessageSignatureData>(deque);
        for (int i = 0; !deque.isEmpty() && i < this.signatures.length; ++i) {
            MessageSignatureData lv = this.signatures[i];
            this.signatures[i] = deque.removeLast();
            if (lv == null || set.contains(lv)) continue;
            deque.addFirst(lv);
        }
    }
}

