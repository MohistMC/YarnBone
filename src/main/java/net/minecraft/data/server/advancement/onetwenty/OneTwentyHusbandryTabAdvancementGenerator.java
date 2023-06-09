/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.data.server.advancement.onetwenty;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancement.Advancement;
import net.minecraft.data.server.advancement.AdvancementTabGenerator;
import net.minecraft.data.server.advancement.vanilla.VanillaHusbandryTabAdvancementGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryWrapper;

public class OneTwentyHusbandryTabAdvancementGenerator
implements AdvancementTabGenerator {
    @Override
    public void accept(RegistryWrapper.WrapperLookup lookup, Consumer<Advancement> exporter) {
        Advancement lv = AdvancementTabGenerator.createEmptyAdvancement("husbandry/breed_an_animal");
        Stream<EntityType<?>> stream = Stream.concat(VanillaHusbandryTabAdvancementGenerator.BREEDABLE_ANIMALS.stream(), Stream.of(EntityType.CAMEL, EntityType.SNIFFER));
        VanillaHusbandryTabAdvancementGenerator.createBreedAllAnimalsAdvancement(lv, exporter, stream, VanillaHusbandryTabAdvancementGenerator.EGG_LAYING_ANIMALS.stream());
    }
}

