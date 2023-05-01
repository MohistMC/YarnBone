/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MessageChain {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private MessageLink link;

    public MessageChain(UUID sender, UUID sessionId) {
        this.link = MessageLink.of(sender, sessionId);
    }

    public Packer getPacker(Signer signer) {
        return body -> {
            MessageLink lv = this.nextLink();
            if (lv == null) {
                return null;
            }
            return new MessageSignatureData(signer.sign(updatable -> SignedMessage.update(updatable, lv, body)));
        };
    }

    public Unpacker getUnpacker(PlayerPublicKey playerPublicKey) {
        SignatureVerifier lv = playerPublicKey.createSignatureInstance();
        return (signature, body) -> {
            MessageLink lv = this.nextLink();
            if (lv == null) {
                throw new MessageChainException((Text)Text.translatable("chat.disabled.chain_broken"), false);
            }
            if (playerPublicKey.data().isExpired()) {
                throw new MessageChainException((Text)Text.translatable("chat.disabled.expiredProfileKey"), false);
            }
            SignedMessage lv2 = new SignedMessage(lv, signature, body, null, FilterMask.PASS_THROUGH);
            if (!lv2.verify(lv)) {
                throw new MessageChainException((Text)Text.translatable("multiplayer.disconnect.unsigned_chat"), true);
            }
            if (lv2.isExpiredOnServer(Instant.now())) {
                LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)body.content());
            }
            return lv2;
        };
    }

    @Nullable
    private MessageLink nextLink() {
        MessageLink lv = this.link;
        if (lv != null) {
            this.link = lv.next();
        }
        return lv;
    }

    @FunctionalInterface
    public static interface Packer {
        public static final Packer NONE = body -> null;

        @Nullable
        public MessageSignatureData pack(MessageBody var1);
    }

    @FunctionalInterface
    public static interface Unpacker {
        public static final Unpacker NOT_INITIALIZED = (signature, body) -> {
            throw new MessageChainException((Text)Text.translatable("chat.disabled.missingProfileKey"), false);
        };

        public static Unpacker unsigned(UUID uuid) {
            return (signature, body) -> SignedMessage.ofUnsigned(uuid, body.content());
        }

        public SignedMessage unpack(@Nullable MessageSignatureData var1, MessageBody var2) throws MessageChainException;
    }

    public static class MessageChainException
    extends TextifiedException {
        private final boolean shouldDisconnect;

        public MessageChainException(Text message, boolean shouldDisconnect) {
            super(message);
            this.shouldDisconnect = shouldDisconnect;
        }

        public boolean shouldDisconnect() {
            return this.shouldDisconnect;
        }
    }
}

