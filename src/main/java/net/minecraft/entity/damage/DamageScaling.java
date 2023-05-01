/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.damage;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum DamageScaling implements StringIdentifiable
{
    NEVER("never"),
    WHEN_CAUSED_BY_LIVING_NON_PLAYER("when_caused_by_living_non_player"),
    ALWAYS("always");

    public static final Codec<DamageScaling> CODEC;
    private final String id;

    private DamageScaling(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        CODEC = StringIdentifiable.createCodec(DamageScaling::values);
    }
}

