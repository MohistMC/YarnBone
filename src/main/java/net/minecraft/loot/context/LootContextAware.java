/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.context;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContextParameter;

public interface LootContextAware {
    default public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of();
    }

    default public void validate(LootTableReporter reporter) {
        reporter.validateContext(this);
    }
}

