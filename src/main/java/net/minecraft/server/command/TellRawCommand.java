/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Texts;

public class TellRawCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("tellraw").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("targets", EntityArgumentType.players()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("message", TextArgumentType.text()).executes(context -> {
            int i = 0;
            for (ServerPlayerEntity lv : EntityArgumentType.getPlayers(context, "targets")) {
                lv.sendMessageToClient(Texts.parse((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "message"), (Entity)lv, 0), false);
                ++i;
            }
            return i;
        }))));
    }
}

