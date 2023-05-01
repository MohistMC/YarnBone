/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ScoreHolderArgumentType
implements ArgumentType<ScoreHolder> {
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder2) -> {
        StringReader stringReader = new StringReader(builder2.getInput());
        stringReader.setCursor(builder2.getStart());
        EntitySelectorReader lv = new EntitySelectorReader(stringReader);
        try {
            lv.read();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return lv.listSuggestions(builder2, (SuggestionsBuilder builder) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getPlayerNames(), builder));
    };
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
    private static final SimpleCommandExceptionType EMPTY_SCORE_HOLDER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.scoreHolder.empty"));
    final boolean multiple;

    public ScoreHolderArgumentType(boolean multiple) {
        this.multiple = multiple;
    }

    public static String getScoreHolder(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return ScoreHolderArgumentType.getScoreHolders(context, name).iterator().next();
    }

    public static Collection<String> getScoreHolders(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return ScoreHolderArgumentType.getScoreHolders(context, name, Collections::emptyList);
    }

    public static Collection<String> getScoreboardScoreHolders(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return ScoreHolderArgumentType.getScoreHolders(context, name, context.getSource().getServer().getScoreboard()::getKnownPlayers);
    }

    public static Collection<String> getScoreHolders(CommandContext<ServerCommandSource> context, String name, Supplier<Collection<String>> players) throws CommandSyntaxException {
        Collection<String> collection = context.getArgument(name, ScoreHolder.class).getNames(context.getSource(), players);
        if (collection.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
        }
        return collection;
    }

    public static ScoreHolderArgumentType scoreHolder() {
        return new ScoreHolderArgumentType(false);
    }

    public static ScoreHolderArgumentType scoreHolders() {
        return new ScoreHolderArgumentType(true);
    }

    @Override
    public ScoreHolder parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '@') {
            EntitySelectorReader lv = new EntitySelectorReader(stringReader);
            EntitySelector lv2 = lv.read();
            if (!this.multiple && lv2.getLimit() > 1) {
                throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
            }
            return new SelectorScoreHolder(lv2);
        }
        int i = stringReader.getCursor();
        while (stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        if (string.equals("*")) {
            return (source, players) -> {
                Collection collection = (Collection)players.get();
                if (collection.isEmpty()) {
                    throw EMPTY_SCORE_HOLDER_EXCEPTION.create();
                }
                return collection;
            };
        }
        Set<String> collection = Collections.singleton(string);
        return (source, players) -> collection;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    @FunctionalInterface
    public static interface ScoreHolder {
        public Collection<String> getNames(ServerCommandSource var1, Supplier<Collection<String>> var2) throws CommandSyntaxException;
    }

    public static class SelectorScoreHolder
    implements ScoreHolder {
        private final EntitySelector selector;

        public SelectorScoreHolder(EntitySelector selector) {
            this.selector = selector;
        }

        @Override
        public Collection<String> getNames(ServerCommandSource arg, Supplier<Collection<String>> supplier) throws CommandSyntaxException {
            List<? extends Entity> list = this.selector.getEntities(arg);
            if (list.isEmpty()) {
                throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
            }
            ArrayList<String> list2 = Lists.newArrayList();
            for (Entity entity : list) {
                list2.add(entity.getEntityName());
            }
            return list2;
        }
    }

    public static class Serializer
    implements ArgumentSerializer<ScoreHolderArgumentType, Properties> {
        private static final byte MULTIPLE_FLAG = 1;

        @Override
        public void writePacket(Properties arg, PacketByteBuf arg2) {
            int i = 0;
            if (arg.multiple) {
                i |= 1;
            }
            arg2.writeByte(i);
        }

        @Override
        public Properties fromPacket(PacketByteBuf arg) {
            byte b = arg.readByte();
            boolean bl = (b & 1) != 0;
            return new Properties(bl);
        }

        @Override
        public void writeJson(Properties arg, JsonObject jsonObject) {
            jsonObject.addProperty("amount", arg.multiple ? "multiple" : "single");
        }

        @Override
        public Properties getArgumentTypeProperties(ScoreHolderArgumentType arg) {
            return new Properties(arg.multiple);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<ScoreHolderArgumentType> {
            final boolean multiple;

            Properties(boolean multiple) {
                this.multiple = multiple;
            }

            @Override
            public ScoreHolderArgumentType createType(CommandRegistryAccess arg) {
                return new ScoreHolderArgumentType(this.multiple);
            }

            @Override
            public ArgumentSerializer<ScoreHolderArgumentType, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

