/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.message;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.util.thread.FutureQueue;
import org.slf4j.Logger;

public class MessageChainTaskQueue
implements FutureQueue,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CompletableFuture<?> current = CompletableFuture.completedFuture(null);
    private final Executor executor = runnable -> {
        if (!this.closed) {
            executor.execute(runnable);
        }
    };
    private volatile boolean closed;

    public MessageChainTaskQueue(Executor executor) {
    }

    @Override
    public void append(FutureQueue.FutureSupplier arg) {
        this.current = ((CompletableFuture)this.current.thenComposeAsync(object -> arg.submit(this.executor), this.executor)).exceptionally(throwable -> {
            if (throwable instanceof CompletionException) {
                CompletionException completionException = (CompletionException)throwable;
                throwable = completionException.getCause();
            }
            if (throwable instanceof CancellationException) {
                CancellationException cancellationException = (CancellationException)throwable;
                throw cancellationException;
            }
            LOGGER.error("Chain link failed, continuing to next one", (Throwable)throwable);
            return null;
        });
    }

    @Override
    public void close() {
        this.closed = true;
    }
}

