/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.encryption;

import java.security.SignatureException;

@FunctionalInterface
public interface SignatureUpdatable {
    public void update(SignatureUpdater var1) throws SignatureException;

    @FunctionalInterface
    public static interface SignatureUpdater {
        public void update(byte[] var1) throws SignatureException;
    }
}

