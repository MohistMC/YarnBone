/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum StructureTerrainAdaptation implements StringIdentifiable
{
    NONE("none"),
    BURY("bury"),
    BEARD_THIN("beard_thin"),
    BEARD_BOX("beard_box");

    public static final Codec<StructureTerrainAdaptation> CODEC;
    private final String name;

    private StructureTerrainAdaptation(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    static {
        CODEC = StringIdentifiable.createCodec(StructureTerrainAdaptation::values);
    }
}

