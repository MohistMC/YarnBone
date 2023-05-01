/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextArgumentType
implements ArgumentType<Text> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType INVALID_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(text -> Text.translatable("argument.component.invalid", text));

    private TextArgumentType() {
    }

    public static Text getTextArgument(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Text.class);
    }

    public static TextArgumentType text() {
        return new TextArgumentType();
    }

    @Override
    public Text parse(StringReader stringReader) throws CommandSyntaxException {
        try {
            MutableText lv = Text.Serializer.fromJson(stringReader);
            if (lv == null) {
                throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, "empty");
            }
            return lv;
        }
        catch (Exception exception) {
            String string = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
            throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, string);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

