/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.message;

import net.minecraft.network.message.MessageSignatureData;

public record AcknowledgedMessage(MessageSignatureData signature, boolean pending) {
    public AcknowledgedMessage unmarkAsPending() {
        return this.pending ? new AcknowledgedMessage(this.signature, false) : this;
    }
}

