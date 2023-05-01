/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class CommandFunctionManager {
    private static final Text NO_TRACE_IN_FUNCTION_TEXT = Text.translatable("commands.debug.function.noRecursion");
    private static final Identifier TICK_TAG_ID = new Identifier("tick");
    private static final Identifier LOAD_TAG_ID = new Identifier("load");
    final MinecraftServer server;
    @Nullable
    private Execution execution;
    private List<CommandFunction> tickFunctions = ImmutableList.of();
    private boolean justLoaded;
    private FunctionLoader loader;

    public CommandFunctionManager(MinecraftServer server, FunctionLoader loader) {
        this.server = server;
        this.loader = loader;
        this.load(loader);
    }

    public int getMaxCommandChainLength() {
        return this.server.getGameRules().getInt(GameRules.MAX_COMMAND_CHAIN_LENGTH);
    }

    public CommandDispatcher<ServerCommandSource> getDispatcher() {
        return this.server.getCommandManager().getDispatcher();
    }

    public void tick() {
        if (this.justLoaded) {
            this.justLoaded = false;
            Collection<CommandFunction> collection = this.loader.getTagOrEmpty(LOAD_TAG_ID);
            this.executeAll(collection, LOAD_TAG_ID);
        }
        this.executeAll(this.tickFunctions, TICK_TAG_ID);
    }

    private void executeAll(Collection<CommandFunction> functions, Identifier label) {
        this.server.getProfiler().push(label::toString);
        for (CommandFunction lv : functions) {
            this.execute(lv, this.getScheduledCommandSource());
        }
        this.server.getProfiler().pop();
    }

    public int execute(CommandFunction function, ServerCommandSource source) {
        return this.execute(function, source, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int execute(CommandFunction function, ServerCommandSource source, @Nullable Tracer tracer) {
        if (this.execution != null) {
            if (tracer != null) {
                this.execution.reportError(NO_TRACE_IN_FUNCTION_TEXT.getString());
                return 0;
            }
            this.execution.recursiveRun(function, source);
            return 0;
        }
        try {
            this.execution = new Execution(tracer);
            int n = this.execution.run(function, source);
            return n;
        }
        finally {
            this.execution = null;
        }
    }

    public void setFunctions(FunctionLoader loader) {
        this.loader = loader;
        this.load(loader);
    }

    private void load(FunctionLoader loader) {
        this.tickFunctions = ImmutableList.copyOf(loader.getTagOrEmpty(TICK_TAG_ID));
        this.justLoaded = true;
    }

    public ServerCommandSource getScheduledCommandSource() {
        return this.server.getCommandSource().withLevel(2).withSilent();
    }

    public Optional<CommandFunction> getFunction(Identifier id) {
        return this.loader.get(id);
    }

    public Collection<CommandFunction> getTag(Identifier id) {
        return this.loader.getTagOrEmpty(id);
    }

    public Iterable<Identifier> getAllFunctions() {
        return this.loader.getFunctions().keySet();
    }

    public Iterable<Identifier> getFunctionTags() {
        return this.loader.getTags();
    }

    public static interface Tracer {
        public void traceCommandStart(int var1, String var2);

        public void traceCommandEnd(int var1, String var2, int var3);

        public void traceError(int var1, String var2);

        public void traceFunctionCall(int var1, Identifier var2, int var3);
    }

    class Execution {
        private int depth;
        @Nullable
        private final Tracer tracer;
        private final Deque<Entry> queue = Queues.newArrayDeque();
        private final List<Entry> waitlist = Lists.newArrayList();

        Execution(Tracer tracer) {
            this.tracer = tracer;
        }

        void recursiveRun(CommandFunction function, ServerCommandSource source) {
            int i = CommandFunctionManager.this.getMaxCommandChainLength();
            if (this.queue.size() + this.waitlist.size() < i) {
                this.waitlist.add(new Entry(source, this.depth, new CommandFunction.FunctionElement(function)));
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        int run(CommandFunction function, ServerCommandSource source) {
            int i = CommandFunctionManager.this.getMaxCommandChainLength();
            int j = 0;
            CommandFunction.Element[] lvs = function.getElements();
            for (int k = lvs.length - 1; k >= 0; --k) {
                this.queue.push(new Entry(source, 0, lvs[k]));
            }
            while (!this.queue.isEmpty()) {
                try {
                    Entry lv = this.queue.removeFirst();
                    CommandFunctionManager.this.server.getProfiler().push(lv::toString);
                    this.depth = lv.depth;
                    lv.execute(CommandFunctionManager.this, this.queue, i, this.tracer);
                    if (!this.waitlist.isEmpty()) {
                        Lists.reverse(this.waitlist).forEach(this.queue::addFirst);
                        this.waitlist.clear();
                    }
                }
                finally {
                    CommandFunctionManager.this.server.getProfiler().pop();
                }
                if (++j < i) continue;
                return j;
            }
            return j;
        }

        public void reportError(String message) {
            if (this.tracer != null) {
                this.tracer.traceError(this.depth, message);
            }
        }
    }

    public static class Entry {
        private final ServerCommandSource source;
        final int depth;
        private final CommandFunction.Element element;

        public Entry(ServerCommandSource source, int depth, CommandFunction.Element element) {
            this.source = source;
            this.depth = depth;
            this.element = element;
        }

        public void execute(CommandFunctionManager manager, Deque<Entry> entries, int maxChainLength, @Nullable Tracer tracer) {
            block4: {
                try {
                    this.element.execute(manager, this.source, entries, maxChainLength, this.depth, tracer);
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    if (tracer != null) {
                        tracer.traceError(this.depth, commandSyntaxException.getRawMessage().getString());
                    }
                }
                catch (Exception exception) {
                    if (tracer == null) break block4;
                    tracer.traceError(this.depth, exception.getMessage());
                }
            }
        }

        public String toString() {
            return this.element.toString();
        }
    }
}

