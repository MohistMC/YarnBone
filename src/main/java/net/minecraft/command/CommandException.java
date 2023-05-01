/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.text.Text;

public class CommandException
extends RuntimeException {
    private final Text message;

    public CommandException(Text message) {
        super(message.getString(), null, CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES, CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES);
        this.message = message;
    }

    public Text getTextMessage() {
        return this.message;
    }
}

