/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.EntityTransformFix;

public abstract class EntitySimpleTransformFix
extends EntityTransformFix {
    public EntitySimpleTransformFix(String string, Schema schema, boolean bl) {
        super(string, schema, bl);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> typed) {
        Pair<String, Dynamic<?>> pair = this.transform(choice, typed.getOrCreate(DSL.remainderFinder()));
        return Pair.of(pair.getFirst(), typed.set(DSL.remainderFinder(), pair.getSecond()));
    }

    protected abstract Pair<String, Dynamic<?>> transform(String var1, Dynamic<?> var2);
}

