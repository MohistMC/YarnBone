/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.entry;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.CombinedEntry;
import net.minecraft.loot.entry.EntryCombiner;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import org.apache.commons.lang3.ArrayUtils;

public class AlternativeEntry
extends CombinedEntry {
    AlternativeEntry(LootPoolEntry[] args, LootCondition[] args2) {
        super(args, args2);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.ALTERNATIVES;
    }

    @Override
    protected EntryCombiner combine(EntryCombiner[] children) {
        switch (children.length) {
            case 0: {
                return ALWAYS_FALSE;
            }
            case 1: {
                return children[0];
            }
            case 2: {
                return children[0].or(children[1]);
            }
        }
        return (context, lootChoiceExpander) -> {
            for (EntryCombiner lv : children) {
                if (!lv.expand(context, lootChoiceExpander)) continue;
                return true;
            }
            return false;
        };
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        for (int i = 0; i < this.children.length - 1; ++i) {
            if (!ArrayUtils.isEmpty(this.children[i].conditions)) continue;
            reporter.report("Unreachable entry!");
        }
    }

    public static Builder builder(LootPoolEntry.Builder<?> ... children) {
        return new Builder(children);
    }

    public static <E> Builder builder(Collection<E> children, Function<E, LootPoolEntry.Builder<?>> toBuilderFunction) {
        return new Builder((LootPoolEntry.Builder[])children.stream().map(toBuilderFunction::apply).toArray(LootPoolEntry.Builder[]::new));
    }

    public static class Builder
    extends LootPoolEntry.Builder<Builder> {
        private final List<LootPoolEntry> children = Lists.newArrayList();

        public Builder(LootPoolEntry.Builder<?> ... children) {
            for (LootPoolEntry.Builder<?> lv : children) {
                this.children.add(lv.build());
            }
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public Builder alternatively(LootPoolEntry.Builder<?> builder) {
            this.children.add(builder.build());
            return this;
        }

        @Override
        public LootPoolEntry build() {
            return new AlternativeEntry(this.children.toArray(new LootPoolEntry[0]), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntry.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

