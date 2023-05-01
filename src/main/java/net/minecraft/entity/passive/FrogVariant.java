/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.passive;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public record FrogVariant(Identifier texture) {
    public static final FrogVariant TEMPERATE = FrogVariant.register("temperate", "textures/entity/frog/temperate_frog.png");
    public static final FrogVariant WARM = FrogVariant.register("warm", "textures/entity/frog/warm_frog.png");
    public static final FrogVariant COLD = FrogVariant.register("cold", "textures/entity/frog/cold_frog.png");

    private static FrogVariant register(String id, String textureId) {
        return Registry.register(Registries.FROG_VARIANT, id, new FrogVariant(new Identifier(textureId)));
    }
}

