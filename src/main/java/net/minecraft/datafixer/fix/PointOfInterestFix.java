/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public abstract class PointOfInterestFix
extends DataFix {
    private final String name;

    public PointOfInterestFix(Schema schema, String name) {
        super(schema, false);
        this.name = name;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(TypeReferences.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(type, this.getInputSchema().getType(TypeReferences.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        }
        return this.fixTypeEverywhere(this.name, type, ops -> pair -> pair.mapSecond(this::fixSections));
    }

    private <T> Dynamic<T> fixSections(Dynamic<T> dynamic) {
        return dynamic.update("Sections", sections -> sections.updateMapValues(pair -> pair.mapSecond(this::fixRecords)));
    }

    private Dynamic<?> fixRecords(Dynamic<?> dynamic) {
        return dynamic.update("Records", this::fixRecord);
    }

    private <T> Dynamic<T> fixRecord(Dynamic<T> dynamic) {
        return DataFixUtils.orElse(dynamic.asStreamOpt().result().map(dynamics -> dynamic.createList(this.update((Stream)dynamics))), dynamic);
    }

    protected abstract <T> Stream<Dynamic<T>> update(Stream<Dynamic<T>> var1);
}

