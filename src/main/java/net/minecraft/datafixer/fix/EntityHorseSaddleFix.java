/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class EntityHorseSaddleFix
extends ChoiceFix {
    public EntityHorseSaddleFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityHorseSaddleFix", TypeReferences.ENTITY, "EntityHorse");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        Type<?> type = this.getInputSchema().getTypeRaw(TypeReferences.ITEM_STACK);
        OpticFinder<?> opticFinder2 = DSL.fieldFinder("SaddleItem", type);
        Optional<Typed<?>> optional = inputType.getOptionalTyped(opticFinder2);
        Dynamic<?> dynamic = inputType.get(DSL.remainderFinder());
        if (!optional.isPresent() && dynamic.get("Saddle").asBoolean(false)) {
            Typed<?> typed2 = type.pointTyped(inputType.getOps()).orElseThrow(IllegalStateException::new);
            typed2 = typed2.set(opticFinder, Pair.of(TypeReferences.ITEM_NAME.typeName(), "minecraft:saddle"));
            Dynamic dynamic2 = dynamic.emptyMap();
            dynamic2 = dynamic2.set("Count", dynamic2.createByte((byte)1));
            dynamic2 = dynamic2.set("Damage", dynamic2.createShort((short)0));
            typed2 = typed2.set(DSL.remainderFinder(), dynamic2);
            dynamic.remove("Saddle");
            return inputType.set(opticFinder2, typed2).set(DSL.remainderFinder(), dynamic);
        }
        return inputType;
    }
}

