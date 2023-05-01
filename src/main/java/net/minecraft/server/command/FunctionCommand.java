/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.Text;

public class FunctionCommand {
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        CommandFunctionManager lv = ((ServerCommandSource)context.getSource()).getServer().getCommandFunctionManager();
        CommandSource.suggestIdentifiers(lv.getFunctionTags(), builder, "#");
        return CommandSource.suggestIdentifiers(lv.getAllFunctions(), builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("function").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("name", CommandFunctionArgumentType.commandFunction()).suggests(SUGGESTION_PROVIDER).executes(context -> FunctionCommand.execute((ServerCommandSource)context.getSource(), CommandFunctionArgumentType.getFunctions(context, "name")))));
    }

    private static int execute(ServerCommandSource source, Collection<CommandFunction> functions) {
        int i = 0;
        for (CommandFunction lv : functions) {
            i += source.getServer().getCommandFunctionManager().execute(lv, source.withSilent().withMaxLevel(2));
        }
        if (functions.size() == 1) {
            source.sendFeedback(Text.translatable("commands.function.success.single", i, functions.iterator().next().getId()), true);
        } else {
            source.sendFeedback(Text.translatable("commands.function.success.multiple", i, functions.size()), true);
        }
        return i;
    }
}

