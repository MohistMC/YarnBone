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

public class EntityWolfColorFix
extends ChoiceFix {
    public EntityWolfColorFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityWolfColorFix", TypeReferences.ENTITY, "minecraft:wolf");
    }

    public Dynamic<?> fixCollarColor(Dynamic<?> dynamic2) {
        return dynamic2.update("CollarColor", dynamic -> dynamic.createByte((byte)(15 - dynamic.asInt(0))));
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::fixCollarColor);
    }
}

