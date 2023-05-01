/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.SignedArgumentType;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class MessageArgumentType
implements SignedArgumentType<MessageFormat> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

    public static MessageArgumentType message() {
        return new MessageArgumentType();
    }

    public static Text getMessage(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        MessageFormat lv = context.getArgument(name, MessageFormat.class);
        return lv.format(context.getSource());
    }

    public static void getSignedMessage(CommandContext<ServerCommandSource> context, String name, Consumer<SignedMessage> callback) throws CommandSyntaxException {
        MessageFormat lv = context.getArgument(name, MessageFormat.class);
        ServerCommandSource lv2 = context.getSource();
        Text lv3 = lv.format(lv2);
        SignedCommandArguments lv4 = lv2.getSignedArguments();
        SignedMessage lv5 = lv4.getMessage(name);
        if (lv5 != null) {
            MessageArgumentType.chain(callback, lv2, lv5.withUnsignedContent(lv3));
        } else {
            MessageArgumentType.chainUnsigned(callback, lv2, SignedMessage.ofUnsigned(lv.contents).withUnsignedContent(lv3));
        }
    }

    private static void chain(Consumer<SignedMessage> callback, ServerCommandSource source, SignedMessage message) {
        MinecraftServer minecraftServer = source.getServer();
        CompletableFuture<FilteredMessage> completableFuture = MessageArgumentType.filterText(source, message);
        CompletableFuture<Text> completableFuture2 = minecraftServer.getMessageDecorator().decorate(source.getPlayer(), message.getContent());
        source.getMessageChainTaskQueue().append(executor -> CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync(void_ -> {
            SignedMessage lv = message.withUnsignedContent((Text)completableFuture2.join()).withFilterMask(((FilteredMessage)completableFuture.join()).mask());
            callback.accept(lv);
        }, executor));
    }

    private static void chainUnsigned(Consumer<SignedMessage> callback, ServerCommandSource source, SignedMessage message) {
        MinecraftServer minecraftServer = source.getServer();
        CompletableFuture<Text> completableFuture = minecraftServer.getMessageDecorator().decorate(source.getPlayer(), message.getContent());
        source.getMessageChainTaskQueue().append(executor -> completableFuture.thenAcceptAsync(content -> callback.accept(message.withUnsignedContent((Text)content)), executor));
    }

    private static CompletableFuture<FilteredMessage> filterText(ServerCommandSource source, SignedMessage message) {
        ServerPlayerEntity lv = source.getPlayer();
        if (lv != null && message.canVerifyFrom(lv.getUuid())) {
            return lv.getTextStream().filterText(message.getSignedContent());
        }
        return CompletableFuture.completedFuture(FilteredMessage.permitted(message.getSignedContent()));
    }

    @Override
    public MessageFormat parse(StringReader stringReader) throws CommandSyntaxException {
        return MessageFormat.parse(stringReader, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static class MessageFormat {
        final String contents;
        private final MessageSelector[] selectors;

        public MessageFormat(String contents, MessageSelector[] selectors) {
            this.contents = contents;
            this.selectors = selectors;
        }

        public String getContents() {
            return this.contents;
        }

        public MessageSelector[] getSelectors() {
            return this.selectors;
        }

        Text format(ServerCommandSource source) throws CommandSyntaxException {
            return this.format(source, source.hasPermissionLevel(2));
        }

        public Text format(ServerCommandSource source, boolean canUseSelectors) throws CommandSyntaxException {
            if (this.selectors.length == 0 || !canUseSelectors) {
                return Text.literal(this.contents);
            }
            MutableText lv = Text.literal(this.contents.substring(0, this.selectors[0].getStart()));
            int i = this.selectors[0].getStart();
            for (MessageSelector lv2 : this.selectors) {
                Text lv3 = lv2.format(source);
                if (i < lv2.getStart()) {
                    lv.append(this.contents.substring(i, lv2.getStart()));
                }
                if (lv3 != null) {
                    lv.append(lv3);
                }
                i = lv2.getEnd();
            }
            if (i < this.contents.length()) {
                lv.append(this.contents.substring(i));
            }
            return lv;
        }

        public static MessageFormat parse(StringReader reader, boolean canUseSelectors) throws CommandSyntaxException {
            String string = reader.getString().substring(reader.getCursor(), reader.getTotalLength());
            if (!canUseSelectors) {
                reader.setCursor(reader.getTotalLength());
                return new MessageFormat(string, new MessageSelector[0]);
            }
            ArrayList<MessageSelector> list = Lists.newArrayList();
            int i = reader.getCursor();
            while (reader.canRead()) {
                if (reader.peek() == '@') {
                    EntitySelector lv2;
                    int j = reader.getCursor();
                    try {
                        EntitySelectorReader lv = new EntitySelectorReader(reader);
                        lv2 = lv.read();
                    }
                    catch (CommandSyntaxException commandSyntaxException) {
                        if (commandSyntaxException.getType() == EntitySelectorReader.MISSING_EXCEPTION || commandSyntaxException.getType() == EntitySelectorReader.UNKNOWN_SELECTOR_EXCEPTION) {
                            reader.setCursor(j + 1);
                            continue;
                        }
                        throw commandSyntaxException;
                    }
                    list.add(new MessageSelector(j - i, reader.getCursor() - i, lv2));
                    continue;
                }
                reader.skip();
            }
            return new MessageFormat(string, list.toArray(new MessageSelector[0]));
        }
    }

    public static class MessageSelector {
        private final int start;
        private final int end;
        private final EntitySelector selector;

        public MessageSelector(int start, int end, EntitySelector selector) {
            this.start = start;
            this.end = end;
            this.selector = selector;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public EntitySelector getSelector() {
            return this.selector;
        }

        @Nullable
        public Text format(ServerCommandSource source) throws CommandSyntaxException {
            return EntitySelector.getNames(this.selector.getEntities(source));
        }
    }
}

