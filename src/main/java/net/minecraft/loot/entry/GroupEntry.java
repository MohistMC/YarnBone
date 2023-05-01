/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.entry;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.CombinedEntry;
import net.minecraft.loot.entry.EntryCombiner;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;

public class GroupEntry
extends CombinedEntry {
    GroupEntry(LootPoolEntry[] args, LootCondition[] args2) {
        super(args, args2);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.GROUP;
    }

    @Override
    protected EntryCombiner combine(EntryCombiner[] children) {
        switch (children.length) {
            case 0: {
                return ALWAYS_TRUE;
            }
            case 1: {
                return children[0];
            }
            case 2: {
                EntryCombiner lv = children[0];
                EntryCombiner lv2 = children[1];
                return (context, choiceConsumer) -> {
                    lv.expand(context, choiceConsumer);
                    lv2.expand(context, choiceConsumer);
                    return true;
                };
            }
        }
        return (context, lootChoiceExpander) -> {
            for (EntryCombiner lv : children) {
                lv.expand(context, lootChoiceExpander);
            }
            return true;
        };
    }

    public static Builder create(LootPoolEntry.Builder<?> ... entries) {
        return new Builder(entries);
    }

    public static class Builder
    extends LootPoolEntry.Builder<Builder> {
        private final List<LootPoolEntry> entries = Lists.newArrayList();

        public Builder(LootPoolEntry.Builder<?> ... entries) {
            for (LootPoolEntry.Builder<?> lv : entries) {
                this.entries.add(lv.build());
            }
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public Builder sequenceEntry(LootPoolEntry.Builder<?> entry) {
            this.entries.add(entry.build());
            return this;
        }

        @Override
        public LootPoolEntry build() {
            return new GroupEntry(this.entries.toArray(new LootPoolEntry[0]), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntry.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

