/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class StriderGravityFix
extends ChoiceFix {
    public StriderGravityFix(Schema schema, boolean bl) {
        super(schema, bl, "StriderGravityFix", TypeReferences.ENTITY, "minecraft:strider");
    }

    public Dynamic<?> updateNoGravityNbt(Dynamic<?> dynamic) {
        if (dynamic.get("NoGravity").asBoolean(false)) {
            return dynamic.set("NoGravity", dynamic.createBoolean(false));
        }
        return dynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::updateNoGravityNbt);
    }
}

