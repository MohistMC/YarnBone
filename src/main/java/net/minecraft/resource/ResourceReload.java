/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;

public interface ResourceReload {
    public CompletableFuture<?> whenComplete();

    public float getProgress();

    default public boolean isComplete() {
        return this.whenComplete().isDone();
    }

    default public void throwException() {
        CompletableFuture<?> completableFuture = this.whenComplete();
        if (completableFuture.isCompletedExceptionally()) {
            completableFuture.join();
        }
    }
}

