/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
    public boolean test(char var1);

    default public CharPredicate and(CharPredicate predicate) {
        Objects.requireNonNull(predicate);
        return c -> this.test(c) && predicate.test(c);
    }

    default public CharPredicate negate() {
        return c -> !this.test(c);
    }

    default public CharPredicate or(CharPredicate predicate) {
        Objects.requireNonNull(predicate);
        return c -> this.test(c) || predicate.test(c);
    }
}

