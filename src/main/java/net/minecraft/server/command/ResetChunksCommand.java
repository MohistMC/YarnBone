/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;

public class ResetChunksCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("resetchunks").requires(source -> source.hasPermissionLevel(2))).executes(context -> ResetChunksCommand.executeResetChunks((ServerCommandSource)context.getSource(), 0, true))).then(((RequiredArgumentBuilder)CommandManager.argument("range", IntegerArgumentType.integer(0, 5)).executes(context -> ResetChunksCommand.executeResetChunks((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "range"), true))).then(CommandManager.argument("skipOldChunks", BoolArgumentType.bool()).executes(context -> ResetChunksCommand.executeResetChunks((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "range"), BoolArgumentType.getBool(context, "skipOldChunks"))))));
    }

    private static int executeResetChunks(ServerCommandSource source, int radius, boolean skipOldChunks) {
        ServerWorld lv = source.getWorld();
        ServerChunkManager lv2 = lv.getChunkManager();
        lv2.threadedAnvilChunkStorage.verifyChunkGenerator();
        Vec3d lv3 = source.getPosition();
        ChunkPos lv4 = new ChunkPos(BlockPos.ofFloored(lv3));
        int j = lv4.z - radius;
        int k = lv4.z + radius;
        int l = lv4.x - radius;
        int m = lv4.x + radius;
        for (int n = j; n <= k; ++n) {
            for (int o = l; o <= m; ++o) {
                ChunkPos lv5 = new ChunkPos(o, n);
                WorldChunk lv6 = lv2.getWorldChunk(o, n, false);
                if (lv6 == null || skipOldChunks && lv6.usesOldNoise()) continue;
                for (BlockPos lv7 : BlockPos.iterate(lv5.getStartX(), lv.getBottomY(), lv5.getStartZ(), lv5.getEndX(), lv.getTopY() - 1, lv5.getEndZ())) {
                    lv.setBlockState(lv7, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
                }
            }
        }
        TaskExecutor<Runnable> lv8 = TaskExecutor.create(Util.getMainWorkerExecutor(), "worldgen-resetchunks");
        long p = System.currentTimeMillis();
        int q = (radius * 2 + 1) * (radius * 2 + 1);
        for (ChunkStatus lv9 : ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES)) {
            long r = System.currentTimeMillis();
            CompletionStage<Unit> completableFuture = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, lv8::send);
            for (int s = lv4.z - radius; s <= lv4.z + radius; ++s) {
                for (int t = lv4.x - radius; t <= lv4.x + radius; ++t) {
                    ChunkPos lv10 = new ChunkPos(t, s);
                    WorldChunk lv11 = lv2.getWorldChunk(t, s, false);
                    if (lv11 == null || skipOldChunks && lv11.usesOldNoise()) continue;
                    ArrayList<Chunk> list = Lists.newArrayList();
                    int u = Math.max(1, lv9.getTaskMargin());
                    for (int v = lv10.z - u; v <= lv10.z + u; ++v) {
                        for (int w = lv10.x - u; w <= lv10.x + u; ++w) {
                            Chunk lv12 = lv2.getChunk(w, v, lv9.getPrevious(), true);
                            Chunk lv13 = lv12 instanceof ReadOnlyChunk ? new ReadOnlyChunk(((ReadOnlyChunk)lv12).getWrappedChunk(), true) : (lv12 instanceof WorldChunk ? new ReadOnlyChunk((WorldChunk)lv12, true) : lv12);
                            list.add(lv13);
                        }
                    }
                    completableFuture = completableFuture.thenComposeAsync(unit -> lv9.runGenerationTask(lv8::send, lv, lv2.getChunkGenerator(), lv.getStructureTemplateManager(), lv2.getLightingProvider(), chunk -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                    }, list, true).thenApply(either -> {
                        if (lv9 == ChunkStatus.NOISE) {
                            either.left().ifPresent(chunk -> Heightmap.populateHeightmaps(chunk, ChunkStatus.POST_CARVER_HEIGHTMAPS));
                        }
                        return Unit.INSTANCE;
                    }), lv8::send);
                }
            }
            source.getServer().runTasks(() -> completableFuture.isDone());
            LOGGER.debug(lv9.getId() + " took " + (System.currentTimeMillis() - r) + " ms");
        }
        long x = System.currentTimeMillis();
        for (int y = lv4.z - radius; y <= lv4.z + radius; ++y) {
            for (int z = lv4.x - radius; z <= lv4.x + radius; ++z) {
                ChunkPos lv14 = new ChunkPos(z, y);
                WorldChunk lv15 = lv2.getWorldChunk(z, y, false);
                if (lv15 == null || skipOldChunks && lv15.usesOldNoise()) continue;
                for (BlockPos lv16 : BlockPos.iterate(lv14.getStartX(), lv.getBottomY(), lv14.getStartZ(), lv14.getEndX(), lv.getTopY() - 1, lv14.getEndZ())) {
                    lv2.markForUpdate(lv16);
                }
            }
        }
        LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - x) + " ms");
        long r = System.currentTimeMillis() - p;
        source.sendFeedback(Text.literal(String.format(Locale.ROOT, "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", q, r, q, Float.valueOf((float)r / (float)q))), true);
        return 1;
    }
}

