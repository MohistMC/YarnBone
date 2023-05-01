/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.SignedMessage;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MessageVerifier {
    public static final MessageVerifier NO_SIGNATURE = message -> !message.hasSignature();
    public static final MessageVerifier UNVERIFIED = message -> false;

    public boolean isVerified(SignedMessage var1);

    public static class Impl
    implements MessageVerifier {
        private final SignatureVerifier signatureVerifier;
        @Nullable
        private SignedMessage lastVerifiedMessage;
        private boolean lastMessageVerified = true;

        public Impl(SignatureVerifier signatureVerifier) {
            this.signatureVerifier = signatureVerifier;
        }

        private boolean verifyPrecedingSignature(SignedMessage message) {
            if (message.equals(this.lastVerifiedMessage)) {
                return true;
            }
            return this.lastVerifiedMessage == null || message.link().linksTo(this.lastVerifiedMessage.link());
        }

        @Override
        public boolean isVerified(SignedMessage message) {
            boolean bl = this.lastMessageVerified = this.lastMessageVerified && message.verify(this.signatureVerifier) && this.verifyPrecedingSignature(message);
            if (!this.lastMessageVerified) {
                return false;
            }
            this.lastVerifiedMessage = message;
            return true;
        }
    }
}

