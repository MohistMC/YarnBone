/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry.tag;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class FluidTags {
    public static final TagKey<Fluid> WATER = FluidTags.of("water");
    public static final TagKey<Fluid> LAVA = FluidTags.of("lava");

    private FluidTags() {
    }

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, new Identifier(id));
    }
}

