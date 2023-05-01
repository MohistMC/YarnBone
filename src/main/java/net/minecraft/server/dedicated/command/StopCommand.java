/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class StopCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("stop").requires(source -> source.hasPermissionLevel(4))).executes(context -> {
            ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.stop.stopping"), true);
            ((ServerCommandSource)context.getSource()).getServer().stop(false);
            return 1;
        }));
    }
}

