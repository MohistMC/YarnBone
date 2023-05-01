/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.thread;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

@FunctionalInterface
public interface FutureQueue {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static FutureQueue immediate(Executor executor) {
        return future -> future.submit(executor).exceptionally(throwable -> {
            LOGGER.error("Task failed", (Throwable)throwable);
            return null;
        });
    }

    public void append(FutureSupplier var1);

    public static interface FutureSupplier {
        public CompletableFuture<?> submit(Executor var1);
    }
}

