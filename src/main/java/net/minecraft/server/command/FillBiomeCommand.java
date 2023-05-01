/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.function.Predicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
    public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maximum, specified) -> Text.translatable("commands.fillbiome.toobig", maximum, specified));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fillbiome").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("to", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("biome", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.BIOME)).executes(context -> FillBiomeCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to"), RegistryEntryArgumentType.getRegistryEntry(context, "biome", RegistryKeys.BIOME), arg -> true))).then(CommandManager.literal("replace").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("filter", RegistryEntryPredicateArgumentType.registryEntryPredicate(commandRegistryAccess, RegistryKeys.BIOME)).executes(context -> FillBiomeCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to"), RegistryEntryArgumentType.getRegistryEntry(context, "biome", RegistryKeys.BIOME), RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "filter", RegistryKeys.BIOME)::test))))))));
    }

    private static int convertCoordinate(int coordinate) {
        return BiomeCoords.toBlock(BiomeCoords.fromBlock(coordinate));
    }

    private static BlockPos convertPos(BlockPos pos) {
        return new BlockPos(FillBiomeCommand.convertCoordinate(pos.getX()), FillBiomeCommand.convertCoordinate(pos.getY()), FillBiomeCommand.convertCoordinate(pos.getZ()));
    }

    private static BiomeSupplier createBiomeSupplier(MutableInt counter, Chunk chunk, BlockBox box, RegistryEntry<Biome> biome, Predicate<RegistryEntry<Biome>> filter) {
        return (x, y, z, noise) -> {
            int l = BiomeCoords.toBlock(x);
            int m = BiomeCoords.toBlock(y);
            int n = BiomeCoords.toBlock(z);
            RegistryEntry<Biome> lv = chunk.getBiomeForNoiseGen(x, y, z);
            if (box.contains(l, m, n) && filter.test(lv)) {
                counter.increment();
                return biome;
            }
            return lv;
        };
    }

    private static int execute(ServerCommandSource source, BlockPos from, BlockPos to, RegistryEntry.Reference<Biome> biome, Predicate<RegistryEntry<Biome>> filter) throws CommandSyntaxException {
        int j;
        BlockPos lv2;
        BlockPos lv = FillBiomeCommand.convertPos(from);
        BlockBox lv3 = BlockBox.create(lv, lv2 = FillBiomeCommand.convertPos(to));
        int i = lv3.getBlockCountX() * lv3.getBlockCountY() * lv3.getBlockCountZ();
        if (i > (j = source.getWorld().getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            throw TOO_BIG_EXCEPTION.create(j, i);
        }
        ServerWorld lv4 = source.getWorld();
        ArrayList<Chunk> list = new ArrayList<Chunk>();
        for (int k = ChunkSectionPos.getSectionCoord(lv3.getMinZ()); k <= ChunkSectionPos.getSectionCoord(lv3.getMaxZ()); ++k) {
            for (int l = ChunkSectionPos.getSectionCoord(lv3.getMinX()); l <= ChunkSectionPos.getSectionCoord(lv3.getMaxX()); ++l) {
                Chunk lv5 = lv4.getChunk(l, k, ChunkStatus.FULL, false);
                if (lv5 == null) {
                    throw UNLOADED_EXCEPTION.create();
                }
                list.add(lv5);
            }
        }
        MutableInt mutableInt = new MutableInt(0);
        for (Chunk lv5 : list) {
            lv5.populateBiomes(FillBiomeCommand.createBiomeSupplier(mutableInt, lv5, lv3, biome, filter), lv4.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
            lv5.setNeedsSaving(true);
        }
        lv4.getChunkManager().threadedAnvilChunkStorage.sendChunkBiomePackets(list);
        source.sendFeedback(Text.translatable("commands.fillbiome.success.count", mutableInt.getValue(), lv3.getMinX(), lv3.getMinY(), lv3.getMinZ(), lv3.getMaxX(), lv3.getMaxY(), lv3.getMaxZ()), true);
        return mutableInt.getValue();
    }
}

