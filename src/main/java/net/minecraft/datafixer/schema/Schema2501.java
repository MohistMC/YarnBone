/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema2501
extends IdentifierNormalizingSchema {
    public Schema2501(int i, Schema schema) {
        super(i, schema);
    }

    private static void registerFurnace(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "RecipesUsed", DSL.compoundList(TypeReferences.RECIPE.in(schema), DSL.constType(DSL.intType()))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        Schema2501.registerFurnace(schema, map, "minecraft:furnace");
        Schema2501.registerFurnace(schema, map, "minecraft:smoker");
        Schema2501.registerFurnace(schema, map, "minecraft:blast_furnace");
        return map;
    }
}

