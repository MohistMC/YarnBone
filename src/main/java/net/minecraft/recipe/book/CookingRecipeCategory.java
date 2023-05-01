/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe.book;

import net.minecraft.util.StringIdentifiable;

public enum CookingRecipeCategory implements StringIdentifiable
{
    FOOD("food"),
    BLOCKS("blocks"),
    MISC("misc");

    public static final StringIdentifiable.Codec<CookingRecipeCategory> CODEC;
    private final String id;

    private CookingRecipeCategory(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        CODEC = StringIdentifiable.createCodec(CookingRecipeCategory::values);
    }
}

