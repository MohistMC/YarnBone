/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.HeightmapArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.SwizzleArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.BossBarCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class ExecuteCommand {
    private static final int MAX_BLOCKS = 32768;
    private static final Dynamic2CommandExceptionType BLOCKS_TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.translatable("commands.execute.blocks.toobig", maxCount, count));
    private static final SimpleCommandExceptionType CONDITIONAL_FAIL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.execute.conditional.fail"));
    private static final DynamicCommandExceptionType CONDITIONAL_FAIL_COUNT_EXCEPTION = new DynamicCommandExceptionType(count -> Text.translatable("commands.execute.conditional.fail_count", count));
    private static final BinaryOperator<ResultConsumer<ServerCommandSource>> BINARY_RESULT_CONSUMER = (consumer, consumer2) -> (context, success, result) -> {
        consumer.onCommandComplete(context, success, result);
        consumer2.onCommandComplete(context, success, result);
    };
    private static final SuggestionProvider<ServerCommandSource> LOOT_CONDITIONS = (context, builder) -> {
        LootConditionManager lv = ((ServerCommandSource)context.getSource()).getServer().getPredicateManager();
        return CommandSource.suggestIdentifiers(lv.getIds(), builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("execute").requires(source -> source.hasPermissionLevel(2)));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("execute").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.literal("run").redirect(dispatcher.getRoot()))).then(ExecuteCommand.addConditionArguments(literalCommandNode, CommandManager.literal("if"), true, commandRegistryAccess))).then(ExecuteCommand.addConditionArguments(literalCommandNode, CommandManager.literal("unless"), false, commandRegistryAccess))).then(CommandManager.literal("as").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, context -> {
            ArrayList<ServerCommandSource> list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities(context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withEntity(entity));
            }
            return list;
        })))).then(CommandManager.literal("at").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, context -> {
            ArrayList<ServerCommandSource> list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities(context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withWorld((ServerWorld)entity.world).withPosition(entity.getPos()).withRotation(entity.getRotationClient()));
            }
            return list;
        })))).then(((LiteralArgumentBuilder)CommandManager.literal("store").then(ExecuteCommand.addStoreArguments(literalCommandNode, CommandManager.literal("result"), true))).then(ExecuteCommand.addStoreArguments(literalCommandNode, CommandManager.literal("success"), false)))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("positioned").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect(literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withPosition(Vec3ArgumentType.getVec3(context, "pos")).withEntityAnchor(EntityAnchorArgumentType.EntityAnchor.FEET)))).then(CommandManager.literal("as").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, context -> {
            ArrayList<ServerCommandSource> list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities(context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withPosition(entity.getPos()));
            }
            return list;
        })))).then(CommandManager.literal("over").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("heightmap", HeightmapArgumentType.heightmap()).redirect(literalCommandNode, context -> {
            Vec3d lv = ((ServerCommandSource)context.getSource()).getPosition();
            ServerWorld lv2 = ((ServerCommandSource)context.getSource()).getWorld();
            double d = lv.getX();
            double e = lv.getZ();
            if (!lv2.isChunkLoaded(ChunkSectionPos.getSectionCoordFloored(d), ChunkSectionPos.getSectionCoordFloored(e))) {
                throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
            }
            int i = lv2.getTopY(HeightmapArgumentType.getHeightmap(context, "heightmap"), MathHelper.floor(d), MathHelper.floor(e));
            return ((ServerCommandSource)context.getSource()).withPosition(new Vec3d(d, i, e));
        }))))).then(((LiteralArgumentBuilder)CommandManager.literal("rotated").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("rot", RotationArgumentType.rotation()).redirect(literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withRotation(RotationArgumentType.getRotation(context, "rot").toAbsoluteRotation((ServerCommandSource)context.getSource()))))).then(CommandManager.literal("as").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, context -> {
            ArrayList<ServerCommandSource> list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities(context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withRotation(entity.getRotationClient()));
            }
            return list;
        }))))).then(((LiteralArgumentBuilder)CommandManager.literal("facing").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).fork(literalCommandNode, context -> {
            ArrayList<ServerCommandSource> list = Lists.newArrayList();
            EntityAnchorArgumentType.EntityAnchor lv = EntityAnchorArgumentType.getEntityAnchor(context, "anchor");
            for (Entity entity : EntityArgumentType.getOptionalEntities(context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withLookingAt(entity, lv));
            }
            return list;
        }))))).then(CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect(literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withLookingAt(Vec3ArgumentType.getVec3(context, "pos")))))).then(CommandManager.literal("align").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("axes", SwizzleArgumentType.swizzle()).redirect(literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withPosition(((ServerCommandSource)context.getSource()).getPosition().floorAlongAxes(SwizzleArgumentType.getSwizzle(context, "axes"))))))).then(CommandManager.literal("anchored").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).redirect(literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withEntityAnchor(EntityAnchorArgumentType.getEntityAnchor(context, "anchor")))))).then(CommandManager.literal("in").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("dimension", DimensionArgumentType.dimension()).redirect(literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withWorld(DimensionArgumentType.getDimensionArgument(context, "dimension")))))).then(CommandManager.literal("summon").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("entity", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).redirect(literalCommandNode, context -> ExecuteCommand.summon((ServerCommandSource)context.getSource(), RegistryEntryArgumentType.getSummonableEntityType(context, "entity")))))).then(ExecuteCommand.addOnArguments(literalCommandNode, CommandManager.literal("on"))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addStoreArguments(LiteralCommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder2, boolean requestResult) {
        builder2.then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("score").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).redirect(node, context -> ExecuteCommand.executeStoreScore((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getObjective(context, "objective"), requestResult)))));
        builder2.then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("bossbar").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(BossBarCommand.SUGGESTION_PROVIDER).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("value").redirect(node, context -> ExecuteCommand.executeStoreBossbar((ServerCommandSource)context.getSource(), BossBarCommand.getBossBar(context), true, requestResult)))).then(CommandManager.literal("max").redirect(node, context -> ExecuteCommand.executeStoreBossbar((ServerCommandSource)context.getSource(), BossBarCommand.getBossBar(context), false, requestResult)))));
        for (DataCommand.ObjectType lv : DataCommand.TARGET_OBJECT_TYPES) {
            lv.addArgumentsToBuilder(builder2, builder -> builder.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("int").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), result -> NbtInt.of((int)((double)result * DoubleArgumentType.getDouble(context, "scale"))), requestResult))))).then(CommandManager.literal("float").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), result -> NbtFloat.of((float)((double)result * DoubleArgumentType.getDouble(context, "scale"))), requestResult))))).then(CommandManager.literal("short").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), result -> NbtShort.of((short)((double)result * DoubleArgumentType.getDouble(context, "scale"))), requestResult))))).then(CommandManager.literal("long").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), result -> NbtLong.of((long)((double)result * DoubleArgumentType.getDouble(context, "scale"))), requestResult))))).then(CommandManager.literal("double").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), result -> NbtDouble.of((double)result * DoubleArgumentType.getDouble(context, "scale")), requestResult))))).then(CommandManager.literal("byte").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), result -> NbtByte.of((byte)((double)result * DoubleArgumentType.getDouble(context, "scale"))), requestResult))))));
        }
        return builder2;
    }

    private static ServerCommandSource executeStoreScore(ServerCommandSource source, Collection<String> targets, ScoreboardObjective objective, boolean requestResult) {
        ServerScoreboard lv = source.getServer().getScoreboard();
        return source.mergeConsumers((context, success, result) -> {
            for (String string : targets) {
                ScoreboardPlayerScore lv = lv.getPlayerScore(string, objective);
                int j = requestResult ? result : (success ? 1 : 0);
                lv.setScore(j);
            }
        }, BINARY_RESULT_CONSUMER);
    }

    private static ServerCommandSource executeStoreBossbar(ServerCommandSource source, CommandBossBar bossBar, boolean storeInValue, boolean requestResult) {
        return source.mergeConsumers((context, success, result) -> {
            int j;
            int n = requestResult ? result : (j = success ? 1 : 0);
            if (storeInValue) {
                bossBar.setValue(j);
            } else {
                bossBar.setMaxValue(j);
            }
        }, BINARY_RESULT_CONSUMER);
    }

    private static ServerCommandSource executeStoreData(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, IntFunction<NbtElement> nbtSetter, boolean requestResult) {
        return source.mergeConsumers((context, success, result) -> {
            try {
                NbtCompound lv = object.getNbt();
                int j = requestResult ? result : (success ? 1 : 0);
                path.put(lv, (NbtElement)nbtSetter.apply(j));
                object.setNbt(lv);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }, BINARY_RESULT_CONSUMER);
    }

    private static boolean isLoaded(ServerWorld world, BlockPos pos) {
        int i = ChunkSectionPos.getSectionCoord(pos.getX());
        int j = ChunkSectionPos.getSectionCoord(pos.getZ());
        WorldChunk lv = world.getChunkManager().getWorldChunk(i, j);
        if (lv != null) {
            return lv.getLevelType() == ChunkHolder.LevelType.ENTITY_TICKING;
        }
        return false;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addConditionArguments(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive, CommandRegistryAccess commandRegistryAccess) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)argumentBuilder.then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("block").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("block", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)), positive, context -> BlockPredicateArgumentType.getBlockPredicate(context, "block").test(new CachedBlockPosition(((ServerCommandSource)context.getSource()).getWorld(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), true))))))).then(CommandManager.literal("biome").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("biome", RegistryEntryPredicateArgumentType.registryEntryPredicate(commandRegistryAccess, RegistryKeys.BIOME)), positive, context -> RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "biome", RegistryKeys.BIOME).test(((ServerCommandSource)context.getSource()).getWorld().getBiome(BlockPosArgumentType.getLoadedBlockPos(context, "pos")))))))).then(CommandManager.literal("loaded").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("pos", BlockPosArgumentType.blockPos()), positive, commandContext -> ExecuteCommand.isLoaded(((ServerCommandSource)commandContext.getSource()).getWorld(), BlockPosArgumentType.getBlockPos(commandContext, "pos")))))).then(CommandManager.literal("dimension").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("dimension", DimensionArgumentType.dimension()), positive, context -> DimensionArgumentType.getDimensionArgument(context, "dimension") == ((ServerCommandSource)context.getSource()).getWorld())))).then(CommandManager.literal("score").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("target", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targetObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("=").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition(context, Integer::equals)))))).then(CommandManager.literal("<").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition(context, (a, b) -> a < b)))))).then(CommandManager.literal("<=").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition(context, (a, b) -> a <= b)))))).then(CommandManager.literal(">").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition(context, (a, b) -> a > b)))))).then(CommandManager.literal(">=").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition(context, (a, b) -> a >= b)))))).then(CommandManager.literal("matches").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("range", NumberRangeArgumentType.intRange()), positive, context -> ExecuteCommand.testScoreMatch(context, NumberRangeArgumentType.IntRangeArgumentType.getRangeArgument(context, "range"))))))))).then(CommandManager.literal("blocks").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("start", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("end", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).then(ExecuteCommand.addBlocksConditionLogic(root, CommandManager.literal("all"), positive, false))).then(ExecuteCommand.addBlocksConditionLogic(root, CommandManager.literal("masked"), positive, true))))))).then(CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("entities", EntityArgumentType.entities()).fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork(context, positive, !EntityArgumentType.getOptionalEntities(context, "entities").isEmpty()))).executes(ExecuteCommand.getExistsConditionExecute(positive, context -> EntityArgumentType.getOptionalEntities(context, "entities").size()))))).then(CommandManager.literal("predicate").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("predicate", IdentifierArgumentType.identifier()).suggests(LOOT_CONDITIONS), positive, context -> ExecuteCommand.testLootCondition((ServerCommandSource)context.getSource(), IdentifierArgumentType.getPredicateArgument(context, "predicate")))));
        for (DataCommand.ObjectType lv : DataCommand.SOURCE_OBJECT_TYPES) {
            argumentBuilder.then(lv.addArgumentsToBuilder(CommandManager.literal("data"), builder -> builder.then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork(context, positive, ExecuteCommand.countPathMatches(lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path")) > 0))).executes(ExecuteCommand.getExistsConditionExecute(positive, context -> ExecuteCommand.countPathMatches(lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path")))))));
        }
        return argumentBuilder;
    }

    private static Command<ServerCommandSource> getExistsConditionExecute(boolean positive, ExistsCondition condition) {
        if (positive) {
            return context -> {
                int i = condition.test(context);
                if (i > 0) {
                    ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass_count", i), false);
                    return i;
                }
                throw CONDITIONAL_FAIL_EXCEPTION.create();
            };
        }
        return context -> {
            int i = condition.test(context);
            if (i == 0) {
                ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create(i);
        };
    }

    private static int countPathMatches(DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        return path.count(object.getNbt());
    }

    private static boolean testScoreCondition(CommandContext<ServerCommandSource> context, BiPredicate<Integer, Integer> condition) throws CommandSyntaxException {
        String string = ScoreHolderArgumentType.getScoreHolder(context, "target");
        ScoreboardObjective lv = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
        String string2 = ScoreHolderArgumentType.getScoreHolder(context, "source");
        ScoreboardObjective lv2 = ScoreboardObjectiveArgumentType.getObjective(context, "sourceObjective");
        ServerScoreboard lv3 = context.getSource().getServer().getScoreboard();
        if (!lv3.playerHasObjective(string, lv) || !lv3.playerHasObjective(string2, lv2)) {
            return false;
        }
        ScoreboardPlayerScore lv4 = lv3.getPlayerScore(string, lv);
        ScoreboardPlayerScore lv5 = lv3.getPlayerScore(string2, lv2);
        return condition.test(lv4.getScore(), lv5.getScore());
    }

    private static boolean testScoreMatch(CommandContext<ServerCommandSource> context, NumberRange.IntRange range) throws CommandSyntaxException {
        String string = ScoreHolderArgumentType.getScoreHolder(context, "target");
        ScoreboardObjective lv = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
        ServerScoreboard lv2 = context.getSource().getServer().getScoreboard();
        if (!lv2.playerHasObjective(string, lv)) {
            return false;
        }
        return range.test(lv2.getPlayerScore(string, lv).getScore());
    }

    private static boolean testLootCondition(ServerCommandSource source, LootCondition condition) {
        ServerWorld lv = source.getWorld();
        LootContext.Builder lv2 = new LootContext.Builder(lv).parameter(LootContextParameters.ORIGIN, source.getPosition()).optionalParameter(LootContextParameters.THIS_ENTITY, source.getEntity());
        return condition.test(lv2.build(LootContextTypes.COMMAND));
    }

    private static Collection<ServerCommandSource> getSourceOrEmptyForConditionFork(CommandContext<ServerCommandSource> context, boolean positive, boolean value) {
        if (value == positive) {
            return Collections.singleton(context.getSource());
        }
        return Collections.emptyList();
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addConditionLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, Condition condition) {
        return ((ArgumentBuilder)builder.fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork(context, positive, condition.test(context)))).executes(context -> {
            if (positive == condition.test(context)) {
                ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw CONDITIONAL_FAIL_EXCEPTION.create();
        });
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addBlocksConditionLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, boolean masked) {
        return ((ArgumentBuilder)builder.fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork(context, positive, ExecuteCommand.testBlocksCondition(context, masked).isPresent()))).executes(positive ? context -> ExecuteCommand.executePositiveBlockCondition(context, masked) : context -> ExecuteCommand.executeNegativeBlockCondition(context, masked));
    }

    private static int executePositiveBlockCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.testBlocksCondition(context, masked);
        if (optionalInt.isPresent()) {
            context.getSource().sendFeedback(Text.translatable("commands.execute.conditional.pass_count", optionalInt.getAsInt()), false);
            return optionalInt.getAsInt();
        }
        throw CONDITIONAL_FAIL_EXCEPTION.create();
    }

    private static int executeNegativeBlockCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.testBlocksCondition(context, masked);
        if (optionalInt.isPresent()) {
            throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create(optionalInt.getAsInt());
        }
        context.getSource().sendFeedback(Text.translatable("commands.execute.conditional.pass"), false);
        return 1;
    }

    private static OptionalInt testBlocksCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
        return ExecuteCommand.testBlocksCondition(context.getSource().getWorld(), BlockPosArgumentType.getLoadedBlockPos(context, "start"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), masked);
    }

    private static OptionalInt testBlocksCondition(ServerWorld world, BlockPos start, BlockPos end, BlockPos destination, boolean masked) throws CommandSyntaxException {
        BlockBox lv = BlockBox.create(start, end);
        BlockBox lv2 = BlockBox.create(destination, destination.add(lv.getDimensions()));
        BlockPos lv3 = new BlockPos(lv2.getMinX() - lv.getMinX(), lv2.getMinY() - lv.getMinY(), lv2.getMinZ() - lv.getMinZ());
        int i = lv.getBlockCountX() * lv.getBlockCountY() * lv.getBlockCountZ();
        if (i > 32768) {
            throw BLOCKS_TOOBIG_EXCEPTION.create(32768, i);
        }
        int j = 0;
        for (int k = lv.getMinZ(); k <= lv.getMaxZ(); ++k) {
            for (int l = lv.getMinY(); l <= lv.getMaxY(); ++l) {
                for (int m = lv.getMinX(); m <= lv.getMaxX(); ++m) {
                    BlockPos lv4 = new BlockPos(m, l, k);
                    BlockPos lv5 = lv4.add(lv3);
                    BlockState lv6 = world.getBlockState(lv4);
                    if (masked && lv6.isOf(Blocks.AIR)) continue;
                    if (lv6 != world.getBlockState(lv5)) {
                        return OptionalInt.empty();
                    }
                    BlockEntity lv7 = world.getBlockEntity(lv4);
                    BlockEntity lv8 = world.getBlockEntity(lv5);
                    if (lv7 != null) {
                        NbtCompound lv10;
                        if (lv8 == null) {
                            return OptionalInt.empty();
                        }
                        if (lv8.getType() != lv7.getType()) {
                            return OptionalInt.empty();
                        }
                        NbtCompound lv9 = lv7.createNbt();
                        if (!lv9.equals(lv10 = lv8.createNbt())) {
                            return OptionalInt.empty();
                        }
                    }
                    ++j;
                }
            }
        }
        return OptionalInt.of(j);
    }

    private static RedirectModifier<ServerCommandSource> createEntityModifier(Function<Entity, Optional<Entity>> function) {
        return context -> {
            ServerCommandSource lv = (ServerCommandSource)context.getSource();
            Entity lv2 = lv.getEntity();
            if (lv2 == null) {
                return List.of();
            }
            return ((Optional)function.apply(lv2)).filter(entity -> !entity.isRemoved()).map(entity -> List.of(lv.withEntity((Entity)entity))).orElse(List.of());
        };
    }

    private static RedirectModifier<ServerCommandSource> createMultiEntityModifier(Function<Entity, Stream<Entity>> function) {
        return context -> {
            ServerCommandSource lv = (ServerCommandSource)context.getSource();
            Entity lv2 = lv.getEntity();
            if (lv2 == null) {
                return List.of();
            }
            return ((Stream)function.apply(lv2)).filter(entity -> !entity.isRemoved()).map(lv::withEntity).toList();
        };
    }

    private static LiteralArgumentBuilder<ServerCommandSource> addOnArguments(CommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("owner").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Tameable) {
                Tameable lv = (Tameable)((Object)entity);
                optional = Optional.ofNullable(lv.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("leasher").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof MobEntity) {
                MobEntity lv = (MobEntity)entity;
                optional = Optional.ofNullable(lv.getHoldingEntity());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("target").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Targeter) {
                Targeter lv = (Targeter)((Object)entity);
                optional = Optional.ofNullable(lv.getTarget());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("attacker").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Attackable) {
                Attackable lv = (Attackable)((Object)entity);
                optional = Optional.ofNullable(lv.getLastAttacker());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("vehicle").fork(node, ExecuteCommand.createEntityModifier(entity -> Optional.ofNullable(entity.getVehicle()))))).then(CommandManager.literal("controller").fork(node, ExecuteCommand.createEntityModifier(entity -> Optional.ofNullable(entity.getControllingPassenger()))))).then(CommandManager.literal("origin").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Ownable) {
                Ownable lv = (Ownable)((Object)entity);
                optional = Optional.ofNullable(lv.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("passengers").fork(node, ExecuteCommand.createMultiEntityModifier(entity -> entity.getPassengerList().stream())));
    }

    private static ServerCommandSource summon(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType) throws CommandSyntaxException {
        Entity lv = SummonCommand.summon(source, entityType, source.getPosition(), new NbtCompound(), true);
        return source.withEntity(lv);
    }

    @FunctionalInterface
    static interface Condition {
        public boolean test(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface ExistsCondition {
        public int test(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;
    }
}

