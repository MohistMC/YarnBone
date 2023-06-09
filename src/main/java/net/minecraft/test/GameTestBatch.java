/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.test;

import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestFunction;
import org.jetbrains.annotations.Nullable;

public class GameTestBatch {
    public static final String DEFAULT_BATCH = "defaultBatch";
    private final String id;
    private final Collection<TestFunction> testFunctions;
    @Nullable
    private final Consumer<ServerWorld> beforeBatchConsumer;
    @Nullable
    private final Consumer<ServerWorld> afterBatchConsumer;

    public GameTestBatch(String id, Collection<TestFunction> testFunctions, @Nullable Consumer<ServerWorld> beforeBatchConsumer, @Nullable Consumer<ServerWorld> afterBatchConsumer) {
        if (testFunctions.isEmpty()) {
            throw new IllegalArgumentException("A GameTestBatch must include at least one TestFunction!");
        }
        this.id = id;
        this.testFunctions = testFunctions;
        this.beforeBatchConsumer = beforeBatchConsumer;
        this.afterBatchConsumer = afterBatchConsumer;
    }

    public String getId() {
        return this.id;
    }

    public Collection<TestFunction> getTestFunctions() {
        return this.testFunctions;
    }

    public void startBatch(ServerWorld world) {
        if (this.beforeBatchConsumer != null) {
            this.beforeBatchConsumer.accept(world);
        }
    }

    public void finishBatch(ServerWorld world) {
        if (this.afterBatchConsumer != null) {
            this.afterBatchConsumer.accept(world);
        }
    }
}

