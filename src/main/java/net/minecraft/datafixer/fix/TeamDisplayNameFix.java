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
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.text.Text;

public class TeamDisplayNameFix
extends DataFix {
    public TeamDisplayNameFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(TypeReferences.TEAM.typeName(), DSL.remainderType());
        if (!Objects.equals(type, this.getInputSchema().getType(TypeReferences.TEAM))) {
            throw new IllegalStateException("Team type is not what was expected.");
        }
        return this.fixTypeEverywhere("TeamDisplayNameFix", type, dynamicOps -> pair -> pair.mapSecond(dynamic -> dynamic.update("DisplayName", dynamic2 -> DataFixUtils.orElse(dynamic2.asString().map(string -> Text.Serializer.toJson(Text.literal(string))).map(dynamic::createString).result(), dynamic2))));
    }
}

