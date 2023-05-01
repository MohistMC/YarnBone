/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ClearCommand {
    private static final DynamicCommandExceptionType FAILED_SINGLE_EXCEPTION = new DynamicCommandExceptionType(playerName -> Text.translatable("clear.failed.single", playerName));
    private static final DynamicCommandExceptionType FAILED_MULTIPLE_EXCEPTION = new DynamicCommandExceptionType(playerCount -> Text.translatable("clear.failed.multiple", playerCount));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("clear").requires(source -> source.hasPermissionLevel(2))).executes(context -> ClearCommand.execute((ServerCommandSource)context.getSource(), Collections.singleton(((ServerCommandSource)context.getSource()).getPlayerOrThrow()), stack -> true, -1))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes(context -> ClearCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), stack -> true, -1))).then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemPredicateArgumentType.itemPredicate(commandRegistryAccess)).executes(context -> ClearCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), ItemPredicateArgumentType.getItemStackPredicate(context, "item"), -1))).then(CommandManager.argument("maxCount", IntegerArgumentType.integer(0)).executes(context -> ClearCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), ItemPredicateArgumentType.getItemStackPredicate(context, "item"), IntegerArgumentType.getInteger(context, "maxCount")))))));
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Predicate<ItemStack> item, int maxCount) throws CommandSyntaxException {
        int j = 0;
        for (ServerPlayerEntity lv : targets) {
            j += lv.getInventory().remove(item, maxCount, lv.playerScreenHandler.getCraftingInput());
            lv.currentScreenHandler.sendContentUpdates();
            lv.playerScreenHandler.onContentChanged(lv.getInventory());
        }
        if (j == 0) {
            if (targets.size() == 1) {
                throw FAILED_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
            }
            throw FAILED_MULTIPLE_EXCEPTION.create(targets.size());
        }
        if (maxCount == 0) {
            if (targets.size() == 1) {
                source.sendFeedback(Text.translatable("commands.clear.test.single", j, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendFeedback(Text.translatable("commands.clear.test.multiple", j, targets.size()), true);
            }
        } else if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.clear.success.single", j, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(Text.translatable("commands.clear.success.multiple", j, targets.size()), true);
        }
        return j;
    }
}

