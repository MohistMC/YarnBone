/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class OptionsForceVBOFix
extends DataFix {
    public OptionsForceVBOFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsForceVBOFix", this.getInputSchema().getType(TypeReferences.OPTIONS), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.set("useVbo", dynamic.createString("true"))));
    }
}

