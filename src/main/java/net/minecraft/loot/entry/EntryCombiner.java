/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.entry;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.context.LootContext;

@FunctionalInterface
interface EntryCombiner {
    public static final EntryCombiner ALWAYS_FALSE = (context, choiceConsumer) -> false;
    public static final EntryCombiner ALWAYS_TRUE = (context, choiceConsumer) -> true;

    public boolean expand(LootContext var1, Consumer<LootChoice> var2);

    default public EntryCombiner and(EntryCombiner other) {
        Objects.requireNonNull(other);
        return (context, lootChoiceExpander) -> this.expand(context, lootChoiceExpander) && other.expand(context, lootChoiceExpander);
    }

    default public EntryCombiner or(EntryCombiner other) {
        Objects.requireNonNull(other);
        return (context, lootChoiceExpander) -> this.expand(context, lootChoiceExpander) || other.expand(context, lootChoiceExpander);
    }
}

