/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.datafixer.fix.PointOfInterestFix;

public class PointOfInterestRemoveFix
extends PointOfInterestFix {
    private final Predicate<String> keepPredicate;

    public PointOfInterestRemoveFix(Schema schema, String name, Predicate<String> removePredicate) {
        super(schema, name);
        this.keepPredicate = removePredicate.negate();
    }

    @Override
    protected <T> Stream<Dynamic<T>> update(Stream<Dynamic<T>> dynamics) {
        return dynamics.filter(this::shouldKeepRecord);
    }

    private <T> boolean shouldKeepRecord(Dynamic<T> dynamic) {
        return dynamic.get("type").asString().result().filter(this.keepPredicate).isPresent();
    }
}

